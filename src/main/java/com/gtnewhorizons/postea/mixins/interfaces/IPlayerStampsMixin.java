package com.gtnewhorizons.postea.mixins.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerStampsMixin {

    NBTTagCompound Postea$getVersionStamps();

    void Postea$setVersionStamps(NBTTagCompound stamps);
}
