package com.gtnewhorizons.postea.utility;

import net.minecraft.world.World;

public final class BlockConversionInfo {

    public final String blockName;
    public int blockID;
    public int metadata;
    public final int x;
    public final int y;
    public final int z;
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
