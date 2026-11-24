package com.gtnewhorizons.postea.utility;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_LIST;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class NbtLists {

    private NbtLists() {}

    /// The list of compounds stored under `key`. Adds an empty list to `parent` unless a list is already stored under
    /// `key`, so appending to the result changes `parent`.
    public static NBTTagList compoundList(NBTTagCompound parent, String key) {
        if (!parent.hasKey(key, TAG_LIST)) parent.setTag(key, new NBTTagList());
        return parent.getTagList(key, TAG_COMPOUND);
    }
}
