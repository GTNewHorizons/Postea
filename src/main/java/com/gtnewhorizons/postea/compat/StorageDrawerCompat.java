package com.gtnewhorizons.postea.compat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.gtnewhorizons.postea.api.IDExtenderCompat;
import com.gtnewhorizons.postea.api.TileEntityReplacementManager;
import com.gtnewhorizons.postea.utility.BlockInfo;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

import cpw.mods.fml.common.Loader;

public class StorageDrawerCompat {

    // vanilla nbt names
    private static final String V_DAMAGE_KEY = "Damage";
    private static final String V_COUNT_KEY = "Count";
    private static final String V_TAG_KEY = "tag";
    // storage drawers nbt-names
    private static final String SD_SLOTS_KEY = "Slots";
    private static final String SD_META_KEY = "Meta";
    private static final String SD_ITEM_KEY = "Item";
    private static final String SD_COUNT_KEY = "Count";
    private static final String SD_TAGS_KEY = "Tags";

    public static void onPostLoad() {
        if (!Loader.isModLoaded("StorageDrawers")) return;
        TileEntityReplacementManager.tileEntityTransformer(
            "StorageDrawers:tileDrawersStandard",
            StorageDrawerCompat::transformStorageDrawerItemNBT);
    }

    @SuppressWarnings("SameReturnValue")
    private static BlockInfo transformStorageDrawerItemNBT(NBTTagCompound tag, World world, Chunk chunk) {
        // abort of data type is unexpected
        if (!tag.hasKey(SD_SLOTS_KEY, 9)) return null;

        NBTTagList slots = tag.getTagList(SD_SLOTS_KEY, 10);
        for (int i = 0; i < slots.tagCount(); i++) {
            NBTTagCompound original = slots.getCompoundTagAt(i);
            // check if we got the right keys for an item stack
            if (!original.hasKey(SD_ITEM_KEY, 99) || !original.hasKey(SD_COUNT_KEY, 99)
                || !original.hasKey(SD_META_KEY, 99)) continue;

            // create a dummy tag to run the migration with
            NBTTagCompound dummyTag = new NBTTagCompound();
            IDExtenderCompat.setItemStackID(dummyTag, original.getShort(SD_ITEM_KEY));
            dummyTag.setInteger(V_COUNT_KEY, original.getInteger(SD_COUNT_KEY));
            dummyTag.setShort(V_DAMAGE_KEY, original.getShort(SD_META_KEY));
            if (original.hasKey(SD_TAGS_KEY, 10)) {
                dummyTag.setTag(V_TAG_KEY, original.getCompoundTag(SD_TAGS_KEY));
            }

            // apply the postea item transformer
            TransformerRegistry.transformItem(dummyTag);

            // update the original data with the updated data
            original.setShort(SD_ITEM_KEY, (short) IDExtenderCompat.getItemStackID(dummyTag));
            original.setShort(SD_META_KEY, dummyTag.getShort(V_DAMAGE_KEY));
            // update count if count still exists
            if (dummyTag.hasKey(V_COUNT_KEY, 99)) {
                original.setInteger(SD_COUNT_KEY, dummyTag.getInteger(V_COUNT_KEY));
            }
            // update nbt data
            if (!dummyTag.hasKey(V_TAG_KEY, 10)) {
                original.removeTag(SD_TAGS_KEY);
            } else {
                original.setTag(SD_TAGS_KEY, dummyTag.getCompoundTag(V_TAG_KEY));
            }
        }
        return null;
    }
}
