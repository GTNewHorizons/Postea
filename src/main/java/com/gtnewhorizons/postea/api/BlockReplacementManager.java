package com.gtnewhorizons.postea.api;

import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

public class BlockReplacementManager {

    /**
     * Adds a custom transformer for a given id.
     *
     * @param blockNameIn The id of the block to transform.
     * @param transformer The transformer to apply.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        TransformerRegistry.addBlockReplacement(blockNameIn, transformer);
    }
}
