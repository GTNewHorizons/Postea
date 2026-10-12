package com.gtnewhorizons.postea.utility;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_LIST;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class NbtLists {

    private NbtLists() {}

    /**
     * The list of compounds stored under {@code key}, attached to {@code parent} first when absent so that tags
     * appended to it are part of {@code parent}.
     */
    public static NBTTagList compoundList(NBTTagCompound parent, String key) {
        if (!parent.hasKey(key, TAG_LIST)) parent.setTag(key, new NBTTagList());
        return parent.getTagList(key, TAG_COMPOUND);
    }
}
