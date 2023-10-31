package com.colen.postea.Utility;

import net.minecraft.nbt.NBTTagCompound;

public abstract class PosteaUtilities {

    @SuppressWarnings("unused")
    public static NBTTagCompound cleanseNBT(String newTileEntityID, NBTTagCompound tag) {
        // Cleans the NBT, so that it replaces the ID and adds the x, y, z coordinates.
        NBTTagCompound tagCompound = new NBTTagCompound();

        tagCompound.setString("id", newTileEntityID);
        tagCompound.setInteger("x", tag.getInteger("x"));
        tagCompound.setInteger("y", tag.getInteger("y"));
        tagCompound.setInteger("z", tag.getInteger("z"));

        return tagCompound;
    }
}
