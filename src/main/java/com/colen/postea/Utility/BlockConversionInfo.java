package com.colen.postea.Utility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class BlockConversionInfo {

    public int x;
    public int y;
    public int z;

    public int blockID;
    public int metadata;

    public World world;
    public NBTTagCompound tileEntityNBT;

    public String blockName;
}
