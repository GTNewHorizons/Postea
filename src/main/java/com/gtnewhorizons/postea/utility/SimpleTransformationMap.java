package com.gtnewhorizons.postea.utility;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraftforge.oredict.OreDictionary;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A simple mapping registry for quick access to simple transformations.<br>
 * <br>
 * It's essentially a 2 layer map.
 * The first layer contains the namespaced ID of the thing that needs to be replaced.
 * The second layer contains mappings for each meta/damage value of the namespaced ID.<br>
 * <br>
 * When registering something to the second/meta layer, Using {@link OreDictionary#WILDCARD_VALUE}
 * will register a fallback for all meta/damage value of the namespaced ID.<br>
 * <br>
 * Here is an example of the typical structure of this map
 *
 * <pre>
 * {@code
 * ┌─ mod:name_one ─┬─ 1 -> {Stone, 0} : Transforms all mod:name_one with meta 1 into stone with meta 0
 * │                └─ wildcard -> {Plank, Wildcard} : Transforms all other mod:name_one to planks of the same meta.
 * │
 * ├─ mod:name_two ─── 2 -> {Clay, 0} : transforms all mod:name_two with meta 2 to clay with meta 0
 * │
 * └─ mod:name_three ─┬─ 0 -> {Air, 0} : transforms all mod:name_three with meta 0 to air with meta 0
 *                    ├─ 1 -> {Sand, 1} : transforms all mod:name_three with meta 1 to sand with meta 1
 *                    └─ 5 -> {SandStone, Wildcard} : transforms all mod:name_three to sandstone with the same meta.
 * }
 * </pre>
 *
 * @param <T> Either {@link net.minecraft.block.Block} or {@link net.minecraft.item.Item}
 */
class SimpleTransformationMap<T> {

    public static class Value<T> {

        /**
         * The target block or item for the simple transformation
         */
        public final T target;
        /**
         * The target meta/damage value for the transformation.
         * If the value is {@link OreDictionary#WILDCARD_VALUE}, It maintains the old meta/damage value.
         */
        public final int targetMeta;
        /**
         * The numeric id of the target block, updated during the FMLMappingUpdate event
         */
        public int targetRuntimeId;

        public Value(T target, int targetMeta) {
            this.target = target;
            this.targetMeta = targetMeta;
            this.targetRuntimeId = -1;
        }
    }

    private final Map<String, Map<Integer, Value<T>>> registry = new Object2ObjectOpenHashMap<>();

    /**
     * Puts a mapping in this registry.
     *
     * @param originalId   The namespaced ID of the item or block to replace.
     * @param originalMeta The meta or damage value of the item or block to replace.
     *                     {@link OreDictionary#WILDCARD_VALUE} can be used as a wildcard/fallback value.
     *                     When querying, the value will be replaced, which will
     *                     be used when ever no other specific meta match is found.
     */
    public void put(String originalId, int originalMeta, T target, int targetMeta) {
        if (originalMeta == -1) originalMeta = OreDictionary.WILDCARD_VALUE;
        this.registry.computeIfAbsent(originalId, k -> new Int2ObjectOpenHashMap<>())
            .put(originalMeta, new Value<>(target, targetMeta));
    }

    /**
     * Used to query sub-maps at runtime since we capture the relevant sub-map into the transformation handler that is
     * registered for simple transformations. This allows us to not re-query the first layer of this map while
     * performing simple transformations, which should hopefully increase performance.
     *
     * @param subMap The sub-map to query.
     * @param meta   The meta/damage value of the block/item being transformed.
     * @return The mapping if found, else null.
     * @param <T> The type of the sub-map,
     */
    public static <T> @Nullable Value<T> getFromSubMap(Map<Integer, Value<T>> subMap, int meta) {
        Value<T> mapping = subMap.get(meta);
        if (mapping != null) return mapping;
        return subMap.get(OreDictionary.WILDCARD_VALUE);
    }

    /**
     * Used to update runtime IDs during the MappingUpdate event.
     *
     * @return The set of all registered item ids
     */
    public Set<Map.Entry<String, Map<Integer, Value<T>>>> entrySet() {
        return this.registry.entrySet();
    }
}
