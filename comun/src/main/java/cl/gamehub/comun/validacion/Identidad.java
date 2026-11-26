package cl.gamehub.comun.validacion;

import java.util.Locale;

/** Reglas compartidas por el registro, el acceso y el buzón público. */
public final class Identidad {
    public static final String CORREO = "(?i)^[^\\s@]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$";
    private Identidad() { }
    public static boolean runValido(String valor) {
        if (valor == null) return false;
        String run = valor.toUpperCase(Locale.ROOT);
        if (!run.matches("[1-9][0-9]{5,7}[0-9K]")) return false;
        int suma = 0;
        int peso = 2;
        for (int i = run.length() - 2; i >= 0; i--) {
            suma += (run.charAt(i) - '0') * peso;
            peso = peso == 7 ? 2 : peso + 1;
        }
        int resto = 11 - suma % 11;
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return run.charAt(run.length()-1) == esperado;
    }
}
