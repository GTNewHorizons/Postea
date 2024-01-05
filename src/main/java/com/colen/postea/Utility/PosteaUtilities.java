package com.colen.postea.Utility;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public abstract class PosteaUtilities {

    @SuppressWarnings("unused")
    public static NBTTagCompound cleanseNBT(String newTileEntityID, NBTTagCompound tag) {
        // Cleans the NBT, so that it replaces the ID and adds the x, y, z coordinates.
        NBTTagCompound tagCompound = new NBTTagCompound();

        tagCompound.setString("id", newTileEntityID);

        // This part is for tile entities only.
        if (tag.hasKey("x") && tag.hasKey("y") && tag.hasKey("z")) {
            tagCompound.setInteger("x", tag.getInteger("x"));
            tagCompound.setInteger("y", tag.getInteger("y"));
            tagCompound.setInteger("z", tag.getInteger("z"));
        }

        return tagCompound;
    }


    public static int getModListHash() {

        // We iterate over every mod, get its version and put it into a giant list.
        // This is done so we can see if our mods have actually changed, if so, we
        // will recheck every chunk for Postea adjustments.

        ArrayList<String> modList = new ArrayList<>();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            modList.add(mod.getModId() + ":" + mod.getVersion());
        }

        // Sort this first.
        Collections.sort(modList);

        // Step 2: Append all elements into one string
        StringBuilder combinedModStringSorted = new StringBuilder();
        for (String element : modList) {
            combinedModStringSorted.append(element);
        }

        return Objects.hash(combinedModStringSorted);
    }

}
