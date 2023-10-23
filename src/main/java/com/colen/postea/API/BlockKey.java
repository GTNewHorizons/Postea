package com.colen.postea.API;

import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BlockKey {

    private final Block block;
    private final int meta;

    public BlockKey(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof BlockKey that) {
            return (this.meta == that.meta) && (this.block == that.block);
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, meta);
    }

    public int getDamage() {
        return meta;
    }

    public Block getBlock() {
        return block;
    }
}
