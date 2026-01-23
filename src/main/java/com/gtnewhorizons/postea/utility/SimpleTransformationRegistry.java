package com.gtnewhorizons.postea.utility;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizons.postea.api.IDExtenderCompat;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * A registry for simple item and block transformations. A common use case is mod removal, replacements or deprecations.
 * When replacing one mod with another in GTNH, we have to migrate most if not all the mod's items and this often means
 * having to migrate almost every block and item from the original mod.
 * <hr>
 * If a mod uses metas to differentiate items (eg: GT5u) that will end up being migrated to separate items, this would
 * usually result in potentially multiple handlers per item if they are destined to be split into separate mods,
 * which increases the execution time linearly. The simple transformations uses a better algorithm for these
 * which is executed in constant time regardless of how many transformations have been assigned to a given id. (mostly
 * at the cost of higher memory usage due to caching)
 */
public class SimpleTransformationRegistry {

    /**
     * Stores simple block transformations (eg: if old id is # with meta # then turn into item with id # with meta #.)
     */
    public static final SimpleTransformationMap<Pair<Block, Short>> SIMPLE_BLOCK_TRANSFORMATION_MAP = new SimpleTransformationMap<>();
    public static final Object2IntOpenHashMap<Block> RUNTIME_BLOCK_ID_LOOKUP = new Object2IntOpenHashMap<>();
    /**
     * Stores simple item transformations (eg: if old id is # with meta # then turn into item with id # with meta #.)
     */
    public static final SimpleTransformationMap<Pair<Item, Short>> SIMPLE_ITEM_TRANSFORMATION_MAP = new SimpleTransformationMap<>();
    public static final Object2IntOpenHashMap<Item> RUNTIME_ITEM_ID_LOOKUP = new Object2IntOpenHashMap<>();

    // region FML Life Cycle handlers
    /**
     * Registers 1 handler per targeted ID to minimize the number of handlers. The meta-map is captured to reduce
     * the execution time of the handlers.
     */
    public static void onLoadCompleted() {
        // add handlers for simple block remaps
        for (Map.Entry<String, Map<Integer, Pair<Block, Short>>> kv : SIMPLE_BLOCK_TRANSFORMATION_MAP.entrySet()) {
            Map<Integer, Pair<Block, Short>> subMap = kv.getValue();
            TransformerRegistry.addBlockReplacement(kv.getKey(), info -> simpleBlockTransformer(info, subMap));
        }
        // add handlers for simple item remaps
        for (Map.Entry<String, Map<Integer, Pair<Item, Short>>> kv : SIMPLE_ITEM_TRANSFORMATION_MAP.entrySet()) {
            Map<Integer, Pair<Item, Short>> subMap = kv.getValue();
            TransformerRegistry
                .addItemReplacement(kv.getKey(), (originalId, tag) -> simpleItemStackTransformer(tag, subMap));
        }
    }

    /**
     * Pre-caches updated block and item IDs for any simple transformation to speed-up the handlers.
     */
    public static void onIdMappingsChanged() {
        RUNTIME_ITEM_ID_LOOKUP.clear();
        RUNTIME_BLOCK_ID_LOOKUP.clear();
        for (Map.Entry<String, Map<Integer, Pair<Block, Short>>> kv : SIMPLE_BLOCK_TRANSFORMATION_MAP.entrySet()) {
            for (Pair<Block, Short> mapping : kv.getValue()
                .values()) {
                RUNTIME_BLOCK_ID_LOOKUP.computeIfAbsent(mapping.getKey(), Block::getIdFromBlock);
            }
        }
        for (Map.Entry<String, Map<Integer, Pair<Item, Short>>> kv : SIMPLE_ITEM_TRANSFORMATION_MAP.entrySet()) {
            for (Pair<Item, Short> mapping : kv.getValue()
                .values()) {
                RUNTIME_ITEM_ID_LOOKUP.computeIfAbsent(mapping.getKey(), Item::getIdFromItem);
            }
        }
    }
    // endregion FML Life Cycle handlers

    // region block transformations
    /**
     * Registers a simple Block transformation for a simple 1:1 transformation:
     * <ul>
     * <li>block -> block (with same meta)</li>
     * <li>block -> block with specific meta</li>
     * <li>block with meta -> block (with same meta)</li>
     * <li>block with meta -> block with specific meta</li>
     * </ul>
     *
     * @implNote With these simple transformers, it doesn't matter how many transformations are registered to a single
     *           id, only a single handler will be registered per id during the FMLLoadCompleted event. Replacements
     *           are by using hash-table lookups so the execution of the transformation should be constant-time
     *           regardless of how many handlers were registered to a single id.
     *
     * @param originalId     The original namespaced id of the block to replace.
     * @param originalMeta   The meta value of the block to replace. OreDictionary.WILDCARD_VALUE acts as a wildcard
     *                       and fallback if there isn't simple transformation registered for a given meta.
     * @param newBlock       What block to replace with.
     * @param newMeta        The meta value of the transformed block. OreDictionary.WILDCARD_VALUE preserves the
     *                       existing value.
     * @param skipStackRemap Set to true to skip registering an identical simple item transformation.
     */
    public static void addSimpleTransformer(String originalId, int originalMeta, Block newBlock, int newMeta,
        boolean skipStackRemap) {
        if (newMeta == -1) newMeta = OreDictionary.WILDCARD_VALUE;
        SIMPLE_BLOCK_TRANSFORMATION_MAP.put(originalId, originalMeta, Pair.of(newBlock, (short) newMeta));
        if (!skipStackRemap) {
            Item item = Item.getItemFromBlock(newBlock);
            if (item != null) {
                SIMPLE_ITEM_TRANSFORMATION_MAP.put(originalId, originalMeta, Pair.of(item, (short) newMeta));
            }
        }
    }

    /**
     * The handler for the simple block transformation.
     *
     * @implNote This implementation has constant-time runtime.
     */
    private static boolean simpleBlockTransformer(BlockConversionInfo info, Map<Integer, Pair<Block, Short>> metaMap) {
        Pair<Block, Short> mapping = SimpleTransformationMap.getFromSubmap(metaMap, info.metadata);
        if (mapping == null) return false;
        info.blockID = RUNTIME_BLOCK_ID_LOOKUP.computeIfAbsent(mapping.getKey(), Block::getIdFromBlock);
        if (mapping.getValue() != OreDictionary.WILDCARD_VALUE) {
            info.metadata = mapping.getValue();
        }
        return true;
    }
    // endregion block transformations

    // region item transformations
    /**
     * Registers a simple Block transformation for a simple 1:1 transformation:
     * <ul>
     * <li>item -> item (with same meta)</li>
     * <li>item -> item with specific meta</li>
     * <li>item with meta/damage -> item (with same meta/damage)</li>
     * <li>item with meta/damage -> item with specific meta/damage</li>
     * </ul>
     *
     * @implNote With these simple transformers, it doesn't matter how many transformations are registered to a single
     *           id, only a single handler will be registered per id during the FMLLoadCompleted event. Replacements
     *           are by using hash-table lookups so the execution of the transformation should be constant-time
     *           regardless of how many handlers were registered to a single id.
     *
     * @param originalId     The original namespaced id of the item to replace.
     * @param originalMeta   The meta/damage value of the item to replace. OreDictionary.WILDCARD_VALUE acts as a
     *                       wildcard
     *                       and fallback if there isn't simple transformation registered for a given meta.
     * @param newItem        What item to replace with.
     * @param newMeta        The meta/damage value of the transformed item. OreDictionary.WILDCARD_VALUE preserves the
     *                       existing
     *                       value.
     * @param skipBlockRemap Set to true to skip registering an identical block transformation if the item is an
     *                       ItemBlock.
     */
    public static void addSimpleTransformer(String originalId, int originalMeta, Item newItem, int newMeta,
        boolean skipBlockRemap) {
        if (newMeta == -1) newMeta = OreDictionary.WILDCARD_VALUE;
        if (!skipBlockRemap && newItem instanceof ItemBlock ib) {
            SIMPLE_BLOCK_TRANSFORMATION_MAP.put(originalId, originalMeta, Pair.of(ib.field_150939_a, (short) newMeta));
        }
        SIMPLE_ITEM_TRANSFORMATION_MAP.put(originalId, originalMeta, Pair.of(newItem, (short) newMeta));
    }

    /**
     * The handler for the simple item transformation.
     *
     * @implNote This implementation has constant-time runtime.
     */
    private static boolean simpleItemStackTransformer(NBTTagCompound tag, Map<Integer, Pair<Item, Short>> metaMap) {
        short meta = tag.getShort("Damage");
        Pair<Item, Short> mapping = SimpleTransformationMap.getFromSubmap(metaMap, meta);
        if (mapping == null) return false;
        IDExtenderCompat
            .setItemStackID(tag, RUNTIME_ITEM_ID_LOOKUP.computeIfAbsent(mapping.getKey(), Item::getIdFromItem));
        if (mapping.getValue() != OreDictionary.WILDCARD_VALUE) {
            tag.setShort("Damage", mapping.getValue());
        }
        return true;
    }
    // endregion item transformations
}
