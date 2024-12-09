package com.gtnewhorizons.postea.api;

import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;
import static net.minecraftforge.common.util.Constants.NBT.TAG_SHORT;

import net.minecraft.nbt.NBTTagCompound;

public class IDExtenderCompat {

    public static int getItemStackID(NBTTagCompound tag) {
        if (tag.hasKey("idExt", TAG_INT)) return tag.getInteger("idExt");
        else if (tag.hasKey("id", TAG_INT)) return tag.getInteger("id");
        else if (tag.hasKey("id", TAG_SHORT)) return tag.getShort("id");
        else return 0;
    }

    public static void setItemStackID(NBTTagCompound tag, int id) {
        if (id < 32000) {
            tag.setShort("id", (short) id);
        } else {
            tag.setShort("id", (short) 0);
            tag.setInteger("idExt", id);
        }
    }
}
