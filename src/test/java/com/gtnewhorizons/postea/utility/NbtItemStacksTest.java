package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

class NbtItemStacksTest {

    private static NBTTagCompound stack(int id, int damage) {
        NBTTagCompound stack = new NBTTagCompound();
        stack.setShort("id", (short) id);
        stack.setByte("Count", (byte) 1);
        stack.setShort("Damage", (short) damage);
        return stack;
    }

    private static List<NBTTagCompound> visited(NBTTagCompound root) {
        List<NBTTagCompound> stacks = new ArrayList<>();
        NbtItemStacks.forEach(root, stacks::add);
        return stacks;
    }

    @Test
    void visitsStacksInInventoryListsAndStacksNestedInsideAStacksTag() {
        NBTTagCompound nested = stack(2, 0);
        NBTTagCompound backpack = stack(1, 0);
        NBTTagCompound backpackTag = new NBTTagCompound();
        NBTTagList contents = new NBTTagList();
        contents.appendTag(nested);
        backpackTag.setTag("Items", contents);
        backpack.setTag("tag", backpackTag);
        NBTTagList items = new NBTTagList();
        items.appendTag(backpack);
        NBTTagCompound tileEntity = new NBTTagCompound();
        tileEntity.setString("id", "Chest");
        tileEntity.setTag("Items", items);
        NBTTagList tileEntities = new NBTTagList();
        tileEntities.appendTag(tileEntity);
        NBTTagCompound level = new NBTTagCompound();
        level.setTag("TileEntities", tileEntities);

        assertEquals(Arrays.asList(backpack, nested), visited(level));
    }

    @Test
    void ignoresCompoundsThatAreNotStacks() {
        NBTTagCompound enchantment = new NBTTagCompound();
        enchantment.setShort("id", (short) 3);
        enchantment.setShort("lvl", (short) 1);
        NBTTagCompound tileEntity = new NBTTagCompound();
        tileEntity.setString("id", "Chest");
        tileEntity.setInteger("Count", 1);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("ench", enchantment);
        root.setTag("te", tileEntity);

        assertEquals(Collections.emptyList(), visited(root));
    }

    @Test
    void recognisesExtendedIdStacksWithoutADamageKey() {
        NBTTagCompound extended = new NBTTagCompound();
        extended.setShort("id", (short) 0);
        extended.setInteger("idExt", 40000);
        extended.setByte("Count", (byte) 3);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("Item", extended);

        assertEquals(Collections.singletonList(extended), visited(root));
    }

    @Test
    void aVisitorMayInvalidateTheStackItIsGiven() {
        NBTTagCompound first = stack(5, 1);
        NBTTagCompound second = stack(5, 2);
        NBTTagList items = new NBTTagList();
        items.appendTag(first);
        items.appendTag(second);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("Items", items);
        List<NBTTagCompound> seen = new ArrayList<>();

        NbtItemStacks.forEach(root, tag -> {
            seen.add(tag);
            tag.removeTag("id");
        });

        assertEquals(Arrays.asList(first, second), seen);
    }
}
