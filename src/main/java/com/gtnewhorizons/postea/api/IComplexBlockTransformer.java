package com.gtnewhorizons.postea.api;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;

@FunctionalInterface
public interface IComplexBlockTransformer {

    /**
     * A custom transformer for blocks.
     *
     * @param info The information about the block.
     * @return True if the block was replaced and no other block transformation handler should be executed for the
     *         associated original id;
     */
    boolean transformBlock(BlockConversionInfo info);
}
