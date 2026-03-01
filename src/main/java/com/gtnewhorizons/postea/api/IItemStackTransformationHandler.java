package com.gtnewhorizons.postea.api;

import net.minecraft.nbt.NBTTagCompound;

@FunctionalInterface
public interface IItemStackTransformationHandler {

    boolean apply(String originalId, NBTTagCompound stack);
}
