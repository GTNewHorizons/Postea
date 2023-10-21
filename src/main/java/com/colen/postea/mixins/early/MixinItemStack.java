package com.colen.postea.mixins.early;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.colen.postea.API.ItemStackKey;
import com.colen.postea.API.ReplacementManager;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    private int itemDamage; // Metadata
    @Shadow
    private Item field_151002_e;

    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"))
    private void onConstructorReturn(Item item, int size, int metadata, CallbackInfo ci) {
        ItemStackKey replacement = ReplacementManager.getReplacement(item, metadata);

        if (replacement == null) return;

        // Edit private variables here.
        itemDamage = replacement.getDamage();
        func_150996_a(replacement.getItem());
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void onReadFromNBTReturn(NBTTagCompound p_77963_1_, CallbackInfo ci) {
        ItemStackKey replacement = ReplacementManager.getReplacement(field_151002_e, itemDamage);

        if (replacement == null) return;

        func_150996_a(replacement.getItem());
        itemDamage = replacement.getDamage();
    }

    @Unique
    public native void func_150996_a(Item p_150996_1_);
}
