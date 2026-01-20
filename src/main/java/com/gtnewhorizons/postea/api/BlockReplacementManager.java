package com.gtnewhorizons.postea.api;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;
import com.gtnewhorizons.postea.utility.MissingMappingHandler;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

public class BlockReplacementManager {

    /**
     * Adds a custom transformer for a given id.
     *
     * @param originalId  The id of the block to transform.
     * @param transformer The transformer to apply.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId,
        @Nonnull BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (transformer == null) throw new IllegalArgumentException("transformer is null");
        TransformerRegistry.addBlockReplacement(originalId, transformer);
    }

    /**
     * Adds a mapping to remap stacks and block instances of a specific removed block to the given block whilst, whilst
     * maintaining its meta value.
     * <p>
     * <b><u>IMPORTANT: This only works if the world does not have an existing id set for the target block. If you need
     * to transform a missing block into something that was previously given an id (e.g.: Transforming a removed block
     * into dirt), use {@link #addBlockReplacement(String, Block)} with both meta parameters set to
     * OreDictionary.WILDCARD_VALUE.</u></b>
     *
     * @param originalId The id of the block to remap.
     * @param block      The block to remap to.
     */
    @SuppressWarnings("unused")
    public static void replaceMissingBlockWithNewBlock(@Nonnull String originalId, @Nonnull Block block) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (block == null) throw new IllegalArgumentException("block is null");
        MissingMappingHandler.addSimpleReplacement(originalId, block);
    }

    /**
     * Suppresses any missing mapping warning for the given id.
     * @param originalId The id of the block or item to suppress the warning for.
     */
    @SuppressWarnings("unused")
    public static void ignoreMissingMapping(@Nonnull String originalId) {
        MissingMappingHandler.addIgnore(originalId);
    }

    // block -> block

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block, whilst maintaining its
     * meta value. This also adds a matching item stack replacement.
     *
     * @apiNote Use this if you are replacing a block with something that was already registered to a world's id
     *          mappings,
     *          (e.g.: Transforming a removed block into dirt)
     *
     * @param originalId The id of the item to remap.
     * @param block      The block to remap to.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId, @Nonnull Block block) {
        addBlockReplacement(originalId, OreDictionary.WILDCARD_VALUE, block, OreDictionary.WILDCARD_VALUE, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block, whilst maintaining its
     * meta value.
     *
     * @apiNote Use this if you are replacing an item with something that was already registered to a world's id
     *          mappings,
     *          (e.g.: Transforming a removed block into dirt)
     *
     * @param originalId     The id of the item to remap.
     * @param block          The block to remap to.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId, @Nonnull Block block, boolean skipStackRemap) {
        addBlockReplacement(
            originalId,
            OreDictionary.WILDCARD_VALUE,
            block,
            OreDictionary.WILDCARD_VALUE,
            skipStackRemap);
    }

    // block -> block+meta

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block with a given damage/meta
     * value. This also adds a matching item stack replacement.
     *
     * @param originalId The id of the block to remap.
     * @param block      The block to remap to.
     * @param newMeta    The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing value.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId, @Nonnull Block block, int newMeta) {
        addBlockReplacement(originalId, OreDictionary.WILDCARD_VALUE, block, newMeta, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block with a given damage/meta
     * value.
     *
     * @param originalId     The id of the block to remap.
     * @param block          The block to remap to.
     * @param newMeta        The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing
     *                       value.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId, @Nonnull Block block, int newMeta,
        boolean skipStackRemap) {
        addBlockReplacement(originalId, OreDictionary.WILDCARD_VALUE, block, newMeta, skipStackRemap);
    }

    // block+meta -> block+meta

    /**
     * Adds a mapping to remap all stacks and block instances of a given block and damage/meta value to another block
     * with a given damage/meta value. This also adds a matching item stack replacement.
     *
     * @param originalId   The id of the block to remap.
     * @param originalMeta The meta of the block to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param block        The block to remap to.
     * @param newMeta      The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing value.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Block block,
        int newMeta) {
        addBlockReplacement(originalId, originalMeta, block, newMeta, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block and damage/meta value to another block
     * with a given damage/meta value.
     *
     * @param originalId     The id of the block to remap.
     * @param originalMeta   The meta of the block to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param block          The block to remap to.
     * @param newMeta        The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing
     *                       value.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addBlockReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Block block,
        int newMeta, boolean skipStackRemap) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (block == null) throw new IllegalArgumentException("block is null");
        TransformerRegistry.addSimpleTransformer(originalId, originalMeta, block, newMeta, skipStackRemap);
    }
}
