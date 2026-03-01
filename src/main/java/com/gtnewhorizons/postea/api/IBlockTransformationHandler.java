package com.gtnewhorizons.postea.api;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;

@FunctionalInterface
public interface IBlockTransformationHandler {

    boolean apply(BlockConversionInfo info);
}
