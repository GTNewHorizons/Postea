package com.gtnewhorizons.postea.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraftforge.oredict.OreDictionary;

public class SimpleTransformationMap<T> {

    private final Map<String, Map<Integer, T>> registry = new HashMap<>();

    /**
     * Puts a mapping in this registry.
     *
     * @param originalId   The original id of the item/block to replace.
     * @param originalMeta The metadata of the item/block to replace. OreDictionary.WILDCARD_VALUE can be used as a
     *                     wildcard.
     */
    public void put(String originalId, int originalMeta, T data) {
        if (originalMeta == -1) originalMeta = OreDictionary.WILDCARD_VALUE;
        this.registry.computeIfAbsent(originalId, k -> new HashMap<>())
            .put(originalMeta, data);
    }

    /**
     * Gets the mapping from the registry.
     *
     * @param id   The id of the item/block to replace.
     * @param meta The meta of the item/block to replace.
     * @return The new id and meta for the item/block if found.
     */
    public @Nullable T get(String id, int meta) {
        Map<Integer, T> metaMap = this.registry.get(id);
        if (metaMap == null) return null;
        T mapping = metaMap.get(meta);
        if (mapping != null) return mapping;
        return metaMap.get(OreDictionary.WILDCARD_VALUE);
    }

    public static <T> @Nullable T getFromSubmap(Map<Integer, T> metaMap, int meta) {
        T mapping = metaMap.get(meta);
        if (mapping != null) return mapping;
        return metaMap.get(OreDictionary.WILDCARD_VALUE);
    }

    /**
     * @return the set of all registered item ids.
     */
    public Set<String> keySet() {
        return this.registry.keySet();
    }

    /**
     * @return the set of all registered item ids.
     */
    public Set<Map.Entry<String, Map<Integer, T>>> entrySet() {
        return this.registry.entrySet();
    }
}
