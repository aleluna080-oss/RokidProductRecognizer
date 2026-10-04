package com.alexluna.rokidproduct;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Maps common ML Kit English labels to concise Spanish HUD names. */
public final class ProductNameMapper {
    private static final Map<String, String> MAP = new LinkedHashMap<>();

    static {
        MAP.put("water bottle", "Botella de agua");
        MAP.put("bottle", "Botella");
        MAP.put("soft drink", "Refresco");
        MAP.put("drink", "Bebida");
        MAP.put("beverage", "Bebida");
        MAP.put("tin can", "Lata");
        MAP.put("can", "Lata");
        MAP.put("snack", "Botana / snack");
        MAP.put("food", "Alimento");
        MAP.put("fruit", "Fruta");
        MAP.put("vegetable", "Verdura");
        MAP.put("packaging", "Empaque");
        MAP.put("package", "Paquete");
        MAP.put("box", "Caja");
        MAP.put("cosmetics", "Cosmético");
        MAP.put("shoe", "Calzado");
        MAP.put("clothing", "Ropa");
        MAP.put("toy", "Juguete");
        MAP.put("mobile phone", "Teléfono");
        MAP.put("phone", "Teléfono");
        MAP.put("laptop", "Laptop");
        MAP.put("computer", "Computadora");
        MAP.put("electronics", "Electrónico");
        MAP.put("tool", "Herramienta");
    }

    private ProductNameMapper() {}

    public static String toDisplayName(String rawLabel) {
        if (rawLabel == null || rawLabel.trim().isEmpty()) return "Objeto";
        String normalized = rawLabel.trim().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> e : MAP.entrySet()) {
            if (normalized.equals(e.getKey())) return e.getValue();
        }
        // For labels we have not translated yet, keep the model output visible.
        return rawLabel;
    }
}
