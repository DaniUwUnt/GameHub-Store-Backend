package cl.gamehub.auth.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class ZonasChile {
    private static final Set<String> ZONAS = cargar();
    private ZonasChile() { }
    private static Set<String> cargar() {
        var recurso = Objects.requireNonNull(ZonasChile.class.getResourceAsStream("/comunas-eft.tsv"));
        try (var lector = new BufferedReader(new InputStreamReader(recurso, StandardCharsets.UTF_8))) {
            return lector.lines().collect(Collectors.toUnmodifiableSet());
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo leer la lista de comunas.", e); }
    }
    static boolean contiene(String region, String comuna) {
        return ZONAS.contains(region + "\t" + comuna);
    }
}
