package com.colen.postea.API;

import java.util.HashMap;
import java.util.Map;

public class TileEntityReplacementManager {

    private static final Map<String, TileTransformer> replacementMap = new HashMap<>();

    @SuppressWarnings("unused")
    public static void addItemReplacement(String tileName , TileTransformer tileTransformer) {
        replacementMap.put(tileName, tileTransformer);
    }

    @SuppressWarnings("unused")
    public static TileTransformer getTileReplacementTransformer(String tileName) {
        return replacementMap.getOrDefault(tileName, null);
    }

}
