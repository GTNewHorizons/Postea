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
import com.colen.postea.API.ItemStackReplacementManager;

@Mixin(ItemStack.class)
@SuppressWarnings("unused")
public abstract class MixinItemStack {

//    @Shadow
//    private int itemDamage; // Metadata
//    @Shadow
//    private Item field_151002_e;
//
//    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"))
//    @SuppressWarnings("unused")
//    private void onConstructorReturn(Item item, int size, int metadata, CallbackInfo ci) {
//        ItemStackKey replacement = ItemStackReplacementManager.getItemReplacement(item, metadata);
//
//        if (replacement == null) return;
//
//        // Edit private variables here.
//        itemDamage = replacement.getDamage();
//        func_150996_a(replacement.getItem());
//    }
//
//    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
//    @SuppressWarnings("unused")
//    private void onReadFromNBTReturn(NBTTagCompound p_77963_1_, CallbackInfo ci) {
//        ItemStackKey replacement = ItemStackReplacementManager.getItemReplacement(field_151002_e, itemDamage);
//
//        if (replacement == null) return;
//
//        itemDamage = replacement.getDamage();
//        func_150996_a(replacement.getItem());
//    }
//
//    @Unique
//    public native void func_150996_a(Item p_150996_1_);
}
