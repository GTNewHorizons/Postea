package com.gtnewhorizons.postea.mixins.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface IChunkMixin {

    long Postea$getPosteaCode();

    void Postea$setPosteaCode(long posteaID);

    NBTTagCompound Postea$getVersionStamps();

    void Postea$setVersionStamps(NBTTagCompound stamps);

    boolean Postea$isVersionedDirty();

    void Postea$setVersionedDirty(boolean dirty);
}
