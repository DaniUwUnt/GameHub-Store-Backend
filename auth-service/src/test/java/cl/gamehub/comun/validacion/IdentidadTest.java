package cl.gamehub.comun.validacion;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class IdentidadTest {
    @Test void verificaCuerpoYDigito() {
        assertThat(Identidad.runValido("123456785")).isTrue();
        assertThat(Identidad.runValido("123456789")).isFalse();
        assertThat(Identidad.runValido("12.345.678-5")).isFalse();
        assertThat(Identidad.runValido("00000000")).isFalse();
    }
    @Test void restringeCorreosAlDominioCompleto() {
        assertThat("prueba@profesor.duoc.cl".matches(Identidad.CORREO)).isTrue();
        assertThat("prueba@duoc.cl.ejemplo.com".matches(Identidad.CORREO)).isFalse();
    }
}
