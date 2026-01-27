package com.gtnewhorizons.postea.utility;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public final class BlockInfo {

    public final Block block;
    public final int metadata;
    public final Function<NBTTagCompound, NBTTagCompound> tileTransformer;

    /**
     * Used to notify postea that you wish to update a tile entity, and the block situated at that same location.
     *
     * @param block           The block that should replace the block currently stored at the tile entity's location.
     * @param metadata        The meta value of the block being placed where the tile entity used to exist.
     * @param tileTransformer A transformer function for the NBT data of the tile entity. (you can just edit the one
     *                        passed to the transformer instead, it's executed right after you return this block info,
     *                        just make sure to provide a pass though function otherwise we won't know you want to keep
     *                        the tile entity alive.)
     */
    public BlockInfo(Block block, int metadata, Function<NBTTagCompound, NBTTagCompound> tileTransformer) {
        this.block = block;
        this.metadata = metadata;
        this.tileTransformer = tileTransformer;
    }

    /**
     * Used to replace a tile entity with a block that does not need any tile entity data. Any existing data will be
     * erased when returning a block info created with this constructor.
     *
     * @param block    The block that should replace the block currently stored at the tile entity's location.
     * @param metadata The meta value of the block being placed where the tile entity used to exist.
     */
    public BlockInfo(Block block, int metadata) {
        this.block = block;
        this.metadata = metadata;
        this.tileTransformer = null;
    }
}
