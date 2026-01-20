package com.gtnewhorizons.postea.utility;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.google.common.collect.LinkedListMultimap;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class TransformerRegistry {

    private static final LinkedListMultimap<String, Function<NBTTagCompound, NBTTagCompound>> ITEM_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    public static final LinkedListMultimap<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> BLOCK_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    public static final IntOpenHashSet POSTEA_MARKED_IDS = new IntOpenHashSet();

    public static void addBlockReplacement(String blockNameIn,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        MissingMappingHandler.createDummyBlockIfNeeded(blockNameIn);
        BLOCK_REPLACEMENT_MAP.put(blockNameIn, transformer);
    }

    public static void addItemReplacement(String itemName, Function<NBTTagCompound, NBTTagCompound> transformer) {
        MissingMappingHandler.createDummyItemIfNeeded(itemName);
        ITEM_REPLACEMENT_MAP.put(itemName, transformer);
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
