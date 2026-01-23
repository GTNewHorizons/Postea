package com.gtnewhorizons.postea.utility;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.api.IDExtenderCompat;

import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class TransformerRegistry {

    private static final LinkedListMultimap<String, Function<NBTTagCompound, Boolean>> ITEM_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    public static final LinkedListMultimap<String, Function<BlockConversionInfo, Boolean>> BLOCK_REPLACEMENT_MAP = LinkedListMultimap
        .create();

    private static final Int2ObjectOpenHashMap<Pair<String, List<Function<NBTTagCompound, Boolean>>>> RUNTIME_ITEM_REPLACEMENT_MAP = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<Pair<String, List<Function<BlockConversionInfo, Boolean>>>> RUNTIME_BLOCK_REPLACEMENT_MAP = new Int2ObjectOpenHashMap<>();
    private static final IntOpenHashSet DUMMY_IDS = new IntOpenHashSet();
    /**
     * Map of IDs that were remapped by forge in the currently loaded save file. (usually happens due to missing
     * mappings)
     * Should be faster than asking block.getIdFromBlock() and Item.getIdFromItem()
     */
    public static final Int2IntOpenHashMap REMAPPED_IDS = new Int2IntOpenHashMap();

    /**
     * Stores simple item transformations (eg if old id is # with meta # then turn into item with id # with meta #.
     */
    public static final SimpleTransformationMap<Pair<Integer, Short>> SIMPLE_ITEM_TRANSFORMATION_MAP = new SimpleTransformationMap<>();
    /**
     * Stores simple block transformations (eg if old id is # with meta # then turn into item with id # with meta #.
     */
    public static final SimpleTransformationMap<Pair<Integer, Short>> SIMPLE_BLOCK_TRANSFORMATION_MAP = new SimpleTransformationMap<>();

    public static void addBlockReplacement(String originalId, Function<BlockConversionInfo, Boolean> transformer) {
        MissingMappingHandler.createDummyBlockIfNeeded(originalId);
        BLOCK_REPLACEMENT_MAP.put(originalId, transformer);
    }

    public static void addItemReplacement(String originalId, Function<NBTTagCompound, Boolean> transformer) {
        MissingMappingHandler.createDummyItemIfNeeded(originalId);
        ITEM_REPLACEMENT_MAP.put(originalId, transformer);
    }

    public static void addSimpleTransformer(String originalId, int originalMeta, Block newBlock, int newMeta,
        boolean skipStackRemap) {
        if (newMeta == -1) newMeta = OreDictionary.WILDCARD_VALUE;
        SIMPLE_BLOCK_TRANSFORMATION_MAP
            .put(originalId, originalMeta, Pair.of(Block.getIdFromBlock(newBlock), (short) newMeta));
        if (!skipStackRemap) {
            Item item = Item.getItemFromBlock(newBlock);
            if (item != null) {
                SIMPLE_ITEM_TRANSFORMATION_MAP
                    .put(originalId, originalMeta, Pair.of(Item.getIdFromItem(item), (short) newMeta));
            }
        }
    }

    public static void addSimpleTransformer(String originalId, int originalMeta, Item newItem, int newMeta,
        boolean skipBlockRemap) {
        if (newMeta == -1) newMeta = OreDictionary.WILDCARD_VALUE;
        if (!skipBlockRemap && newItem instanceof ItemBlock ib) {
            SIMPLE_BLOCK_TRANSFORMATION_MAP
                .put(originalId, originalMeta, Pair.of(Block.getIdFromBlock(ib.field_150939_a), (short) newMeta));
        }
        SIMPLE_ITEM_TRANSFORMATION_MAP
            .put(originalId, originalMeta, Pair.of(Item.getIdFromItem(newItem), (short) newMeta));
    }

    private static boolean simpleBlockTransformer(BlockConversionInfo info,
        Map<Integer, Pair<Integer, Short>> metaMap) {
        Pair<Integer, Short> mapping = SimpleTransformationMap.getFromSubmap(metaMap, info.metadata);
        if (mapping == null) return false;
        int key = mapping.getKey();
        info.blockID = REMAPPED_IDS.getOrDefault(key, key);
        if (mapping.getValue() != OreDictionary.WILDCARD_VALUE) {
            info.metadata = mapping.getValue();
        }
        return true;
    }

    private static boolean simpleItemStackTransformer(NBTTagCompound tag, Map<Integer, Pair<Integer, Short>> metaMap) {
        short meta = tag.getShort("Damage");
        Pair<Integer, Short> mapping = SimpleTransformationMap.getFromSubmap(metaMap, meta);
        if (mapping == null) return false;
        int key = mapping.getKey();
        IDExtenderCompat.setItemStackID(tag, REMAPPED_IDS.getOrDefault(key, key));
        if (mapping.getValue() != OreDictionary.WILDCARD_VALUE) {
            tag.setShort("Damage", mapping.getValue());
        }
        return true;
    }

    public static boolean isDummyId(int id) {
        return DUMMY_IDS.contains(id);
    }

    // region FML Life Cycle handlers
    public static void onLoadCompleted() {
        // add handlers for simple block remaps
        for (Map.Entry<String, Map<Integer, Pair<Integer, Short>>> kv : SIMPLE_BLOCK_TRANSFORMATION_MAP.entrySet()) {
            MissingMappingHandler.createDummyBlockIfNeeded(kv.getKey());
            BLOCK_REPLACEMENT_MAP.put(kv.getKey(), info -> simpleBlockTransformer(info, kv.getValue()));
        }
        // add handlers for simple item remaps
        for (Map.Entry<String, Map<Integer, Pair<Integer, Short>>> kv : SIMPLE_ITEM_TRANSFORMATION_MAP.entrySet()) {
            MissingMappingHandler.createDummyItemIfNeeded(kv.getKey());
            ITEM_REPLACEMENT_MAP.put(kv.getKey(), tag -> simpleItemStackTransformer(tag, kv.getValue()));
        }
    }

    public static void onIdMappingsChanged(FMLModIdMappingEvent event) {
        REMAPPED_IDS.clear();
        DUMMY_IDS.clear();
        RUNTIME_BLOCK_REPLACEMENT_MAP.clear();
        RUNTIME_ITEM_REPLACEMENT_MAP.clear();
        // Store the remapped ids since the simple transform pivot on ids since it's faster using the block/item.
        for (FMLModIdMappingEvent.ModRemapping remap : event.remappedIds) {
            // the remapping type doesn't actually matter, FML doesn't seem to handle it right anyway.
            REMAPPED_IDS.putIfAbsent(remap.oldId, remap.newId);
        }
        // create a lookup for source block ids.
        for (String key : BLOCK_REPLACEMENT_MAP.keys()) {
            String name = MissingMappingHandler.getDummyIdIfMissing(key);
            if (Block.blockRegistry.getObject(name) instanceof Block block) {
                int id = Block.getIdFromBlock(block);
                RUNTIME_BLOCK_REPLACEMENT_MAP.put(id, Pair.of(key, BLOCK_REPLACEMENT_MAP.get(key)));
                // not using .equals() on purpose, I'm looking for instance, not content equality
                if (key != name) DUMMY_IDS.add(id);
            }
        }
        // create a lookup for source block ids.
        for (String key : ITEM_REPLACEMENT_MAP.keys()) {
            String name = MissingMappingHandler.getDummyIdIfMissing(key);
            if (Item.itemRegistry.getObject(name) instanceof Item item) {
                int id = Item.getIdFromItem(item);
                RUNTIME_ITEM_REPLACEMENT_MAP.put(id, Pair.of(key, ITEM_REPLACEMENT_MAP.get(key)));
                // not using .equals() on purpose, I'm looking for instance, not content equality
                if (key != name) DUMMY_IDS.add(id);
            }
        }
    }

    public static void onServerStopping() {
        RUNTIME_BLOCK_REPLACEMENT_MAP.clear();
        RUNTIME_ITEM_REPLACEMENT_MAP.clear();
        REMAPPED_IDS.clear();
        DUMMY_IDS.clear();
    }
    // endregion FML Life Cycle handlers

    // region block replacement handler
    public static @Nullable BlockConversionInfo getBlockReplacement(int blockId, byte metadata, World world, int x,
        int y, int z) {
        Pair<String, List<Function<BlockConversionInfo, Boolean>>> data = RUNTIME_BLOCK_REPLACEMENT_MAP.get(blockId);
        if (data == null) return null;
        BlockConversionInfo blockConversionInfo = new BlockConversionInfo(
            // transparently maps the id to the original id if it's a dummy id
            data.getKey(),
            blockId,
            metadata,
            x,
            y,
            z,
            world);
        for (Function<BlockConversionInfo, Boolean> transformer : data.getValue()) {
            if (transformer.apply(blockConversionInfo)) {
                // Not sure if we want to keep this around since this would affect perf a bit, good for debugging
                // though.
                // if (DUMMY_IDS.contains(result.blockID)) FMLLog.warning("returned a success on a non-transformed
                // block!");
                return blockConversionInfo;
            }
        }
        // if we're transforming a dummy block from a missing mapping and it wasn't handled, delete the block.
        if (TransformerRegistry.isDummyId(blockId)) {
            blockConversionInfo.blockID = 0;
            blockConversionInfo.metadata = 0;
            return blockConversionInfo;
        }
        return null;
    }
    // endregion block replacement handler

    // region item replacement handler
    public static void transformItem(NBTTagCompound tag) {
        // abort early if tag is bad.
        if (tag.hasNoTags() || !tag.hasKey("id")) return;
        // get handler
        int id = IDExtenderCompat.getItemStackID(tag);
        Pair<String, List<Function<NBTTagCompound, Boolean>>> data = RUNTIME_ITEM_REPLACEMENT_MAP.get(id);
        // abort early if handler not found
        if (data == null || data.getValue()
            .isEmpty()) return;

        // apply handlers
        for (Function<NBTTagCompound, Boolean> transformer : data.getValue()) {
            if (transformer.apply(tag)) {
                // Not sure if we want to keep this around since this would affect perf a bit, good for debugging
                // though.
                // if (DUMMY_IDS.contains(IDExtenderCompat.getItemStackID(tag))) FMLLog.warning("returned a success on a
                // non-transformed block!");
                return;
            }
        }
        // if no handler overwrote anything on this stack, and it's a dummy, delete the stack.
        if (TransformerRegistry.isDummyId(id)) {
            tag.removeTag("id");
            tag.removeTag("idExt");
        }
    }

    /**
     * A method to go from tag -> name since that's not provided to handlers usually.
     * Could be useful if you have one handler function handling a lot of complex transforms.
     */
    public static @Nullable String getMappedItemName(int itemId) {
        Pair<String, List<Function<NBTTagCompound, Boolean>>> data = RUNTIME_ITEM_REPLACEMENT_MAP.get(itemId);
        if (data == null) return null;
        return data.getKey();
    }
    // endregion item replacement handler
}
