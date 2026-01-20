package com.gtnewhorizons.postea.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.tuple.Pair;

public class SimpleTransformationMap {

    private final Map<String, Map<Integer, Pair<Integer, Short>>> registry = new HashMap<>();

    /**
     * Puts a mapping in this registry.
     *
     * @param originalId   The original id of the item/block to replace.
     * @param originalMeta The metadata of the item/block to replace. OreDictionary.WILDCARD_VALUE can be used as a
     *                     wildcard.
     * @param newId        The id of the replacement block/item.
     * @param newMeta      The new meta of the block to block/item.
     */
    public void put(String originalId, int originalMeta, int newId, int newMeta) {
        if (originalMeta == -1) originalMeta = OreDictionary.WILDCARD_VALUE;
        if (newMeta == -1) newMeta = OreDictionary.WILDCARD_VALUE;
        this.registry.computeIfAbsent(originalId, k -> new HashMap<>())
            .put(originalMeta, Pair.of(newId, (short) newMeta));
    }

    /**
     * Gets the mapping from the registry.
     *
     * @param id   The id of the item/block to replace.
     * @param meta The meta of the item/block to replace.
     * @return The new id and meta for the item/block if found.
     */
    public @Nullable Pair<Integer, Short> get(String id, int meta) {
        Map<Integer, Pair<Integer, Short>> metaMap = this.registry.get(id);
        if (metaMap == null) return null;
        Pair<Integer, Short> mapping = metaMap.get(meta);
        if (mapping != null) return mapping;
        return metaMap.get(OreDictionary.WILDCARD_VALUE);
    }

    /**
     * @return the set of all registered item ids.
     */
    public Set<String> keySet() {
        return this.registry.keySet();
    }
}
