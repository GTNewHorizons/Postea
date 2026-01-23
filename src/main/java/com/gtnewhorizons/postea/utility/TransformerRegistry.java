package com.gtnewhorizons.postea.utility;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class TransformerRegistry {

    private static final LinkedListMultimap<String, Function<NBTTagCompound, NBTTagCompound>> ITEM_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    public static final LinkedListMultimap<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> BLOCK_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    public static final IntOpenHashSet POSTEA_MARKED_IDS = new IntOpenHashSet();

    public static final SimpleTransformationMap<Pair<Integer, Short>> SIMPLE_ITEM_TRANSFORMATION_MAP = new SimpleTransformationMap<>();
    public static final SimpleTransformationMap<Pair<Integer, Short>> SIMPLE_BLOCK_TRANSFORMATION_MAP = new SimpleTransformationMap<>();

    public static void addBlockReplacement(String originalId,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        MissingMappingHandler.createDummyBlockIfNeeded(originalId);
        BLOCK_REPLACEMENT_MAP.put(originalId, transformer);
    }

    public static void addItemReplacement(String originalId, Function<NBTTagCompound, NBTTagCompound> transformer) {
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

    public static void onLoadCompleted() {
        for (Map.Entry<String, Map<Integer, Pair<Integer, Short>>> kv : SIMPLE_BLOCK_TRANSFORMATION_MAP.entrySet()) {
            MissingMappingHandler.createDummyBlockIfNeeded(kv.getKey());
            BLOCK_REPLACEMENT_MAP.put(kv.getKey(), (info, world) -> simpleBlockTransformer(info, world, kv.getValue()));
        }
        for (Map.Entry<String, Map<Integer, Pair<Integer, Short>>> kv : SIMPLE_ITEM_TRANSFORMATION_MAP.entrySet()) {
            MissingMappingHandler.createDummyItemIfNeeded(kv.getKey());
            ITEM_REPLACEMENT_MAP.put(kv.getKey(), tag -> simpleItemStackTransformer(tag, kv.getValue()));
        }
    }

    private static @Nullable BlockConversionInfo simpleBlockTransformer(BlockConversionInfo info, World world,
        Map<Integer, Pair<Integer, Short>> metaMap) {
        Pair<Integer, Short> mapping = SimpleTransformationMap.getFromSubmap(metaMap, info.metadata);
        if (mapping == null) return null;
        info.blockID = mapping.getKey();
        if (mapping.getValue() != OreDictionary.WILDCARD_VALUE) {
            info.metadata = mapping.getValue();
        }
        return info;
    }

    private static @Nullable NBTTagCompound simpleItemStackTransformer(NBTTagCompound tag,
        Map<Integer, Pair<Integer, Short>> metaMap) {
        short meta = tag.getShort("Damage");
        Pair<Integer, Short> mapping = SimpleTransformationMap.getFromSubmap(metaMap, meta);
        if (mapping == null) return null;
        IDExtenderCompat.setItemStackID(tag, mapping.getKey());
        if (mapping.getValue() != OreDictionary.WILDCARD_VALUE) {
            tag.setShort("Damage", mapping.getValue());
        }
        return tag;
    }

    public static void onIdMappingsChanged() {
        POSTEA_MARKED_IDS.clear();
        for (String name : BLOCK_REPLACEMENT_MAP.keySet()) {
            Block block = Block.getBlockFromName(name);
            if (block != null) {
                int id = Block.getIdFromBlock(block);
                POSTEA_MARKED_IDS.add(id);
            }
        }
    }

    public static void onServerStopping() {
        POSTEA_MARKED_IDS.clear();
    }

    public static Collection<Function<NBTTagCompound, NBTTagCompound>> getItemReplacement(String itemNameInternal) {
        return ITEM_REPLACEMENT_MAP.get(MissingMappingHandler.getOriginalIDIfIDIsDummyID(itemNameInternal));
    }

    public static BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo, World world) {
        // auto change the value in case a handler check it, we can't do anything about the block id
        // but hopefully that's never going to be an issue
        String originalName = MissingMappingHandler.getOriginalIDIfIDIsDummyID(blockConversionInfo.blockName);
        blockConversionInfo.blockName = originalName;
        for (BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer : BLOCK_REPLACEMENT_MAP
            .get(originalName)) {
            BlockConversionInfo result = transformer.apply(blockConversionInfo, world);
            if (result != null) return result;
        }
        return null;
    }

    // We need this to save reprocessing blocks.
    public static boolean blockNotConvertible(int blockID) {
        return !POSTEA_MARKED_IDS.contains(blockID);
    }
}
