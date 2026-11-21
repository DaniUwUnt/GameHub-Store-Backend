package cl.gamehub.comun.semilla;

import cl.gamehub.comun.semilla.DatosDeEjemplo.CategoriaEjemplo;
import cl.gamehub.comun.semilla.DatosDeEjemplo.ProductoEjemplo;
import java.util.List;

/** Datos iniciales migrados desde el catálogo React de GameHub Store EP2. */
public final class CatalogoEquipo {

    public record Ficha(ProductoEjemplo producto, String marca, String descripcion, String especificacion) {}

    public static final List<CategoriaEjemplo> CATEGORIAS = List.of(
            new CategoriaEjemplo(1, "Notebooks"),
            new CategoriaEjemplo(2, "Periféricos"),
            new CategoriaEjemplo(3, "Componentes"),
            new CategoriaEjemplo(4, "Monitores"),
            new CategoriaEjemplo(5, "Consolas"),
            new CategoriaEjemplo(6, "Computadores"));

    public static final List<Ficha> PRODUCTOS = List.of(
            new Ficha(new ProductoEjemplo(1, "NB-ASUS-001", "Notebook ASUS TUF Gaming", "/imagenes/notebook_asus_tuf.png", 1, 967991, 1099990, true, 6), "ASUS", "Notebook gamer con gráficos dedicados y teclado retroiluminado.", "Gráficos dedicados y teclado retroiluminado"),
            new Ficha(new ProductoEjemplo(2, "PER-HP-002", "Mouse Gamer HP", "/imagenes/mouse_hp.png", 2, 24990, null, true, 18), "HP", "Mouse con cable, rueda central y botones laterales.", "Con cable y botones laterales"),
            new Ficha(new ProductoEjemplo(3, "GPU-MSI-003", "MSI GeForce RTX 5060", "/imagenes/rtx_5060.png", 3, 395591, 429990, true, 4), "MSI", "Tarjeta gráfica MSI GeForce RTX 5060 con 8 GB GDDR7.", "8 GB GDDR7"),
            new Ficha(new ProductoEjemplo(4, "PER-RED-004", "Teclado mecánico RGB Pro", "/imagenes/teclado_rgb.png", 2, 64990, null, true, 14), "Redragon", "Teclado mecánico de tamaño completo con iluminación RGB.", "Formato completo con iluminación RGB"),
            new Ficha(new ProductoEjemplo(5, "MON-MG-005", "Monitor Master-G 27 pulgadas", "/imagenes/monitor_masterg.png", 4, 219990, null, true, 8), "Master-G", "Monitor gamer Full HD de 27 pulgadas y 180 Hz.", "27 pulgadas, Full HD, 180 Hz"),
            new Ficha(new ProductoEjemplo(6, "CON-NIN-006", "Nintendo Switch 2", "/imagenes/nintendo_switch_2.png", 5, 599990, null, true, 5), "Nintendo", "Consola híbrida con controles Joy-Con.", "Consola híbrida con Joy-Con"),
            new Ficha(new ProductoEjemplo(7, "CON-SONY-007", "PlayStation 5", "/imagenes/playstation_5.png", 5, 649900, null, true, 7), "Sony", "Consola PlayStation 5 con control DualSense.", "Consola con control DualSense"),
            new Ficha(new ProductoEjemplo(8, "PC-GIG-008", "PC Gamer AORUS", "/imagenes/pc_gamer_aorus.png", 6, 2499990, null, true, 2), "Gigabyte", "Computador gamer AORUS de alto rendimiento.", "Equipo de escritorio AORUS"),
            new Ficha(new ProductoEjemplo(9, "PER-JBL-009", "Audífonos Gamer JBL", "/imagenes/audifonos_jbl.png", 2, 76492, 89990, true, 10), "JBL", "Audífonos JBL con micrófono integrado.", "Micrófono integrado"));

    private CatalogoEquipo() {}
}
