package com.colen.postea.API;

import java.util.Objects;

import net.minecraft.item.Item;

public class ItemStackKey {

    private final Item item;
    private final int meta;

    public ItemStackKey(Item item, int meta) {
        this.item = item;
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof ItemStackKey that) {
            return (this.meta == that.meta) && (this.item == that.item);
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, meta);
    }

    public int getDamage() {
        return meta;
    }

    public Item getItem() {
        return item;
    }
}
