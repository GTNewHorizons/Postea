package com.gtnewhorizons.postea.api;

import static com.gtnewhorizons.postea.Postea.LOG;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.world.World;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class BlockReplacementManager {

    public static final Map<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> blockReplacementMap = new HashMap<>();
    public static final IntOpenHashSet posteaMarkedIDs = new IntOpenHashSet();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockName,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        Objects.requireNonNull(blockName, "blockName must not be null");
        Objects.requireNonNull(transformer, "transformer must not be null");

        if (blockReplacementMap.putIfAbsent(blockName, transformer) != null) {
            throw new IllegalStateException("Postea block replacement already registered for '" + blockName + "'.");
        }

        LOG.info("Block replacement registered successfully for {}", blockName);
    }

    public static @Nullable BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo,
        World world) {

        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer = blockReplacementMap
            .getOrDefault(blockConversionInfo.blockName, null);

        if (transformer == null) {
            return null;
        } else {
            return transformer.apply(blockConversionInfo, world);
        }
    }

    // We need this to save reprocessing blocks.
    public static boolean blockNotConvertible(int blockID) {
        return !posteaMarkedIDs.contains(blockID);
    }

}
