package com.colen.postea.API;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.Objects;
import java.util.function.Function;

public class TileTransformer {

    private final Class<? extends TileEntity> tileClass;
    private final Function<NBTTagCompound, NBTTagCompound> NBTModifierFunction;

    public TileTransformer(Class<? extends TileEntity> tileClass, Function<NBTTagCompound, NBTTagCompound> NBTModifierFunction) {
        this.tileClass = tileClass;
        this.NBTModifierFunction = NBTModifierFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof TileTransformer that) {
            return (this.tileClass == that.tileClass);
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileClass);
    }

    public void modifyNBT(NBTTagCompound nbt) {
        NBTModifierFunction.apply(nbt);
    }

    public Class<? extends TileEntity> getTileClass() {
        return tileClass;
    }
}
