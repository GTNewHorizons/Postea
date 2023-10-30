package com.colen.postea.API;

import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BlockKey {

    public final String blockName;
    public final int meta;

    public BlockKey(String blockName, int meta) {
        this.blockName = blockName;
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof BlockKey that) {
            return (this.meta == that.meta) && (this.blockName.equals(that.blockName));
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockName, meta);
    }

}
