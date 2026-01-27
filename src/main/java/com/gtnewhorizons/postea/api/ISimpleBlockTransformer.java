package com.gtnewhorizons.postea.api;

import net.minecraft.block.Block;

public interface ISimpleBlockTransformer {

    Block getTargetBlock();

    int getTargetBlockMeta();
}
