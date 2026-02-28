package com.gtnewhorizons.postea.utility;

import net.minecraft.world.World;

public final class BlockConversionInfo {

    /**
     * The namespaced ID of the block being converted.
     */
    public final String blockName;
    /**
     * The numeric ID of the block currently being replaced, or of the block you want it replaced with.
     */
    public int blockID;
    /**
     * The meta value of the block currently being replaced, or value you want it replaced with.
     */
    public int metadata;
    /**
     * The x coordinate of the block being replaced.
     */
    public final int x;
    /**
     * The y coordinate of the block being replaced.
     */
    public final int y;
    /**
     * The z coordinate of the block being replaced.
     */
    public final int z;

    /**
     * The world containing the block being replaced. It won't be loaded fully when a block transformer is executed, so
     * any call to something like world.getBlock() will throw an error.
     */
    public final World world;

    public BlockConversionInfo(String blockName, int blockID, int metadata, int x, int y, int z, World world) {
        this.blockName = blockName;
        this.blockID = blockID;
        this.metadata = metadata;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
}
