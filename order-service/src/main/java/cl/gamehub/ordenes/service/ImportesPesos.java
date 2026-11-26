package cl.gamehub.ordenes.service;

import cl.gamehub.comun.error.ApiException;

/** El contrato del pago de la demo opera con pesos enteros sin signo negativo. */
final class ImportesPesos {
    private ImportesPesos() {}

    static int multiplicar(int a, int b) {
        return comprobar((long) a * b);
    }

    static int sumar(int a, int b) {
        return comprobar((long) a + b);
    }

    private static int comprobar(long total) {
        if (total < 0 || total > Integer.MAX_VALUE) {
            throw ApiException.regla("El total supera la capacidad técnica del pago simulado en pesos.");
        }
        return (int) total;
    }
}
