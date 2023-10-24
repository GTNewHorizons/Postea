package com.colen.postea.API;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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

    public void setBlock(NBTTagCompound nbt, World world) {
        int x = nbt.getInteger("x");
        int y = nbt.getInteger("y");
        int z = nbt.getInteger("z");

        Minecraft.getMinecraft().theWorld.setBlock(x, y, z, Blocks.chest);
    }
}
