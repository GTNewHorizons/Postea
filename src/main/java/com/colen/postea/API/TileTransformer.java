package com.colen.postea.API;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.function.Function;

public class TileTransformer {


    private final Function<NBTTagCompound, NBTTagCompound> NBTModifierFunction;
    private final String tileName;

    public TileTransformer(String tileName, Function<NBTTagCompound, NBTTagCompound> NBTModifierFunction) {
        this.tileName = tileName;
        this.NBTModifierFunction = NBTModifierFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof TileTransformer that) {
            return (this.tileName.equals(that.tileName));
        } else return false;
    }

    public void modifyNBT(NBTTagCompound nbt) {
        NBTModifierFunction.apply(nbt);
    }

    public String getTileName() {
        return tileName;
    }

}
