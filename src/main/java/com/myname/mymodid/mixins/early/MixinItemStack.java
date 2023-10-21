package com.myname.mymodid.mixins.early;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack{

    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"))
    private void onConstructorHead(Item item, int stackSize, int metadata, CallbackInfo ci) {
        System.out.println(item.getUnlocalizedName() + " " + metadata + " " + stackSize);
    }

}
