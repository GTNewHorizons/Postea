package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

class NbtListsTest {

    @Test
    void anAbsentListIsCreatedAndAttachedSoAppendedTagsAreKept() {
        NBTTagCompound level = new NBTTagCompound();

        NbtLists.compoundList(level, "TileEntities")
            .appendTag(new NBTTagCompound());

        assertEquals(
            1,
            level.getTagList("TileEntities", 10)
                .tagCount());
    }

    @Test
    void anExistingListIsReturnedAsIs() {
        NBTTagCompound level = new NBTTagCompound();
        NBTTagList existing = new NBTTagList();
        existing.appendTag(new NBTTagCompound());
        level.setTag("Entities", existing);

        assertSame(existing, NbtLists.compoundList(level, "Entities"));
    }
}
