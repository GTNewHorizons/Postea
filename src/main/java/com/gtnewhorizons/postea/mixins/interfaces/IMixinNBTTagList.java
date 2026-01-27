package com.gtnewhorizons.postea.mixins.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface IMixinNBTTagList {

    void Postea$replaceCompoundTagAt(int index, NBTTagCompound tag);
}
