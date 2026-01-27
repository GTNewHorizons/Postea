package com.gtnewhorizons.postea.mixins.early;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.postea.mixins.interfaces.IMixinNBTTagList;

@Mixin(NBTTagList.class)
public class MixinNBTTagList implements IMixinNBTTagList {

    @Shadow
    @Final
    private List tagList = new ArrayList();

    @Override
    @Unique
    public void Postea$replaceCompoundTagAt(int index, NBTTagCompound tag) {
        tagList.set(index, tag);
    }
}
