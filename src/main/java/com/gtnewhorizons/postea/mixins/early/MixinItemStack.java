package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.postea.utility.TransformerRegistry;

@Mixin(ItemStack.class)
@SuppressWarnings("unused")
public abstract class MixinItemStack {

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"))
    @SuppressWarnings("unused")
    private void onReadFromNBTReturn(NBTTagCompound tag, CallbackInfo ci) {
        TransformerRegistry.transformItem(tag);
    }

}
