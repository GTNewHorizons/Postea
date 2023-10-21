package com.myname.mymodid;


import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.RegistryDelegate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemStackKey {
    private final Item item;
    private final int meta;

    public ItemStackKey(Item item, int meta) {
        this.item = item;
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackKey that = (ItemStackKey) o;
        return (this.meta == that.meta) && Objects.equals(this.item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, meta);
    }

    public int getDamage() {
        return meta;
    }

    public RegistryDelegate<Item> getItem() {
        return item.delegate;
    }
}

