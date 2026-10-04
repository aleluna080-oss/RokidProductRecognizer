package com.alexluna.rokidproduct;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Traduce únicamente etiquetas conocidas del modelo genérico de ML Kit.
 * Devuelve null para etiquetas no mapeadas para impedir que aparezca inglés
 * o que una categoría desconocida se presente como una detección válida.
 */
public final class ProductNameMapper {
    private static final Map<String, String> MAP = new LinkedHashMap<>();

    static {
        // Bebidas y envases
        MAP.put("water bottle", "Botella de agua");
        MAP.put("bottle", "Botella");
        MAP.put("soft drink", "Refresco");
        MAP.put("drink", "Bebida");
        MAP.put("beverage", "Bebida");
        MAP.put("tin can", "Lata");
        MAP.put("can", "Lata");
        MAP.put("cup", "Vaso");
        MAP.put("mug", "Taza");

        // Alimentos
        MAP.put("snack", "Botana");
        MAP.put("food", "Alimento");
        MAP.put("fruit", "Fruta");
        MAP.put("vegetable", "Verdura");
        MAP.put("bread", "Pan");
        MAP.put("banana", "Plátano");
        MAP.put("apple", "Manzana");
        MAP.put("orange", "Naranja");

        // Empaques
        MAP.put("packaging", "Empaque");
        MAP.put("package", "Paquete");
        MAP.put("box", "Caja");
        MAP.put("carton", "Caja de cartón");
        MAP.put("bag", "Bolsa");

        // Objetos comunes
        MAP.put("shoe", "Calzado");
        MAP.put("clothing", "Ropa");
        MAP.put("toy", "Juguete");
        MAP.put("book", "Libro");
        MAP.put("pen", "Pluma");
        MAP.put("pencil", "Lápiz");
        MAP.put("chair", "Silla");
        MAP.put("table", "Mesa");

        // Electrónica
        MAP.put("mobile phone", "Teléfono");
        MAP.put("phone", "Teléfono");
        MAP.put("laptop", "Computadora portátil");
        MAP.put("computer", "Computadora");
        MAP.put("keyboard", "Teclado");
        MAP.put("mouse", "Ratón");
        MAP.put("electronics", "Dispositivo electrónico");

        // Herramientas / industria
        MAP.put("tool", "Herramienta");
        MAP.put("machine", "Máquina");
        MAP.put("metal", "Pieza metálica");
        MAP.put("equipment", "Equipo");

        // Otros
        MAP.put("cosmetics", "Cosmético");
        MAP.put("container", "Contenedor");
    }

    private ProductNameMapper() {}

    public static String toDisplayName(String rawLabel) {
        if (rawLabel == null || rawLabel.trim().isEmpty()) return null;
        String normalized = rawLabel.trim().toLowerCase(Locale.ROOT);
        return MAP.get(normalized);
    }
}
