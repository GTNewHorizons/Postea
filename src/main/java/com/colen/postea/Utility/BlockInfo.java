package com.colen.postea.Utility;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Function;

public final class BlockInfo {
    public final Block block;
    public final int metadata;
    public final Function<NBTTagCompound, NBTTagCompound> tileTransformer;

    public BlockInfo(Block block, int metadata, Function<NBTTagCompound, NBTTagCompound> tileTransformer) {
        this.block = block;
        this.metadata = metadata;
        this.tileTransformer = tileTransformer;
    }

    public BlockInfo(Block block, int metadata) {
        this.block = block;
        this.metadata = metadata;
        this.tileTransformer = null;
    }
}
