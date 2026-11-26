package cl.gamehub.auth.service;

import cl.gamehub.auth.client.NuevoPerfilSolicitud;
import cl.gamehub.auth.client.UserClient;
import cl.gamehub.auth.dto.LoginSolicitud;
import cl.gamehub.auth.dto.RegistroSolicitud;
import cl.gamehub.auth.dto.SesionRespuesta;
import cl.gamehub.auth.dto.UsuarioAdminDto;
import cl.gamehub.auth.dto.UsuarioDto;
import cl.gamehub.auth.model.Credencial;
import cl.gamehub.auth.repository.CredencialRepository;
import cl.gamehub.comun.error.ApiException;
import cl.gamehub.comun.seguridad.JwtServicio;
import cl.gamehub.comun.seguridad.Rol;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {

    private static final Logger log = LoggerFactory.getLogger(AutenticacionService.class);
    private static final String CUENTA_DESACTIVADA =
            "Tu cuenta está desactivada. Escríbenos desde Ayuda para reactivarla.";

    private final CredencialRepository credenciales;
    private final PasswordEncoder codificador;
    private final JwtServicio jwtServicio;
    private final UserClient perfiles;

    public AutenticacionService(CredencialRepository credenciales, PasswordEncoder codificador,
                                JwtServicio jwtServicio, UserClient perfiles) {
        this.credenciales = credenciales;
        this.codificador = codificador;
        this.jwtServicio = jwtServicio;
        this.perfiles = perfiles;
    }

    /**
     * 401 si el correo o la contraseña no calzan (sin decir cuál de los dos, para no revelar qué correos
     * tienen cuenta); 403 si la cuenta existe pero está desactivada.
     */
    @Transactional(readOnly = true)
    public SesionRespuesta iniciarSesion(LoginSolicitud solicitud) {
        Credencial credencial = credenciales.findByEmailIgnoreCase(solicitud.email().trim()).orElse(null);
        if (credencial == null || !codificador.matches(solicitud.clave(), credencial.getClaveHash())) {
            throw ApiException.noAutorizado("Correo o contraseña incorrectos.");
        }
        if (!credencial.isActivo()) {
            throw ApiException.prohibido(CUENTA_DESACTIVADA);
        }
        log.info("Sesión iniciada: usuario {} ({})", credencial.getId(), credencial.getRol());
        return sesion(credencial);
    }

    /**
     * Registro repartido en dos servicios: la credencial se guarda aquí y el perfil, con el mismo id, en
     * user-service. Si user-service falla, la excepción sale de este método y @Transactional deshace el
     * INSERT de la credencial: no quedan cuentas sin perfil.
     */
    @Transactional
    public SesionRespuesta registrar(RegistroSolicitud solicitud) {
        return sesion(crearCuenta(solicitud, Rol.CLIENTE));
    }

    @Transactional
    public UsuarioAdminDto crearDesdeAdmin(Long actorId, RegistroSolicitud solicitud, Rol rol) {
        Credencial actor = bloquear(actorId);
        if (!actor.isActivo() || actor.getRol() != Rol.ADMIN) {
            throw ApiException.prohibido("Se requiere una cuenta administradora activa.");
        }
        return admin(crearCuenta(solicitud, rol));
    }

    private Credencial crearCuenta(RegistroSolicitud solicitud, Rol rol) {
        String email = solicitud.email().trim().toLowerCase(Locale.ROOT);
        if (credenciales.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo. Inicia sesión o usa otro.");
        }
        if (!cl.gamehub.comun.validacion.Identidad.runValido(solicitud.run())) {
            throw ApiException.regla("El dígito verificador del RUN no coincide.");
        }
        if (!ZonasChile.contiene(solicitud.region(), solicitud.comuna())) {
            throw ApiException.regla("Esa comuna no pertenece a la región elegida.");
        }
        String run = solicitud.run().toUpperCase(Locale.ROOT);
        if (credenciales.existsByRun(run)) throw ApiException.conflicto("Ya existe una cuenta con ese RUN.");
        String nombreCompleto = solicitud.nombre().trim() + " " + solicitud.apellidos().trim();
        Credencial nueva = new Credencial(nombreCompleto, email, codificador.encode(solicitud.clave()), rol);
        nueva.registrarIdentidad(run, solicitud.nacimiento());
        Credencial credencial = credenciales.save(nueva);
        // Con IDENTITY el INSERT ya se ejecutó: la credencial tiene id y user-service usa el mismo
        perfiles.crearPerfil(new NuevoPerfilSolicitud(credencial.getId(), credencial.getNombre(),
                credencial.getEmail(), solicitud.telefono(), solicitud.region(), solicitud.comuna(),
                solicitud.direccion()));
        log.info("Cuenta creada: usuario {} ({})", credencial.getId(), credencial.getRol());
        return credencial;
    }

    /** Entrega un token nuevo antes de que venza el actual, siempre que la cuenta siga activa. */
    @Transactional(readOnly = true)
    public SesionRespuesta renovar(Long id) {
        Credencial credencial = buscar(id);
        if (!credencial.isActivo()) {
            throw ApiException.prohibido(CUENTA_DESACTIVADA);
        }
        return sesion(credencial);
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminDto> listar() {
        return credenciales.findAllByOrderByRolAscNombreAsc().stream().map(AutenticacionService::admin).toList();
    }

    /** Un administrador no puede desactivarse a sí mismo: la tienda se quedaría sin quien la administre. */
    @Transactional
    public UsuarioAdminDto cambiarActivo(Long id, boolean activo, Long adminId) {
        if (id.equals(adminId) && !activo) {
            throw ApiException.regla("No puedes desactivar tu propia cuenta.");
        }
        Credencial credencial = cuentaParaEditar(id, adminId);
        credencial.setActivo(activo);
        log.info("Usuario {} {} por el administrador {}", id, activo ? "activado" : "desactivado", adminId);
        return admin(credencial);
    }

    @Transactional
    public UsuarioAdminDto cambiarRol(Long id, Rol nuevoRol, Long adminId) {
        if (id.equals(adminId)) {
            throw ApiException.regla("No puedes cambiar tu propio rol.");
        }
        Credencial cuenta = cuentaParaEditar(id, adminId);
        Rol anterior = cuenta.getRol();
        cuenta.setRol(nuevoRol);
        log.info("Administrador {} cambió el rol de {} de {} a {}", adminId, id, anterior, nuevoRol);
        return admin(cuenta);
    }

    // Bloquear siempre en orden de id evita que dos administradores se quiten los permisos entre sí.
    // Comprobar el rol guardado impide editar cuentas con un token de administrador anterior al cambio.
    private Credencial cuentaParaEditar(Long id, Long adminId) {
        Long primero = Math.min(id, adminId);
        Long segundo = Math.max(id, adminId);
        Credencial a = bloquear(primero);
        Credencial b = primero.equals(segundo) ? a : bloquear(segundo);
        Credencial actor = primero.equals(adminId) ? a : b;
        if (!actor.isActivo() || actor.getRol() != Rol.ADMIN) {
            throw ApiException.prohibido("Tu cuenta ya no puede administrar usuarios.");
        }
        return primero.equals(id) ? a : b;
    }

    private Credencial bloquear(Long id) {
        return credenciales.bloquearPorId(id)
                .orElseThrow(() -> ApiException.noEncontrado("El usuario no existe."));
    }

    private Credencial buscar(Long id) {
        return credenciales.findById(id).orElseThrow(() -> ApiException.noEncontrado("El usuario no existe."));
    }

    /** El token lleva id, correo, nombre y rol: los demás servicios no necesitan preguntarle a auth-service. */
    private SesionRespuesta sesion(Credencial c) {
        String token = jwtServicio.generar(c.getId(), c.getEmail(), c.getNombre(), c.getRol());
        return new SesionRespuesta(token, new UsuarioDto(c.getId(), c.getNombre(), c.getEmail(), c.getRol()));
    }

    private static UsuarioAdminDto admin(Credencial c) {
        return new UsuarioAdminDto(c.getId(), c.getNombre(), c.getEmail(), c.getRol(), c.isActivo(), c.getCreadoEn());
    }
}
