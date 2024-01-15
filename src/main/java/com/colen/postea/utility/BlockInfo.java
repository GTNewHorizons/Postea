package com.colen.postea.utility;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

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
