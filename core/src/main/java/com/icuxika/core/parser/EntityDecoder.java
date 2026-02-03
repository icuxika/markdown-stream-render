package com.icuxika.core.parser;

import java.util.HashMap;
import java.util.Map;

public class EntityDecoder {
    private static final Map<String, String> ENTITIES = new HashMap<>();

    static {
        ENTITIES.put("quot", "\"");
        ENTITIES.put("amp", "&");
        ENTITIES.put("lt", "<");
        ENTITIES.put("gt", ">");
        ENTITIES.put("apos", "'");
        ENTITIES.put("nbsp", "\u00A0");
        ENTITIES.put("copy", "\u00A9");
        ENTITIES.put("reg", "\u00AE");
        ENTITIES.put("AElig", "\u00C6");
        ENTITIES.put("Dcaron", "\u010E");
        ENTITIES.put("frac34", "\u00BE");
        ENTITIES.put("HilbertSpace", "\u210B");
        ENTITIES.put("DifferentialD", "\u2146");
        ENTITIES.put("ClockwiseContourIntegral", "\u2232");
        ENTITIES.put("ngE", "\u2267\u0338");
        ENTITIES.put("ouml", "\u00F6");
        ENTITIES.put("auml", "\u00E4");
    }

    public static String decode(String name) {
        return ENTITIES.get(name);
    }
}
