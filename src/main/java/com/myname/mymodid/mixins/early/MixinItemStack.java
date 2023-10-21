package com.myname.mymodid.mixins.early;


import com.myname.mymodid.ItemStackKey;
import com.myname.mymodid.ReplacementManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    private cpw.mods.fml.common.registry.RegistryDelegate<Item> delegate;
    @Shadow
    private int itemDamage; // Metadata

    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"))
    private void onConstructorHead(Item item, int size, int metadata, CallbackInfo ci) {
        ItemStackKey replacement = ReplacementManager.getReplacement(item, metadata);

        // Edit private variables here.
        itemDamage = replacement.getDamage();
        delegate = replacement.getItem();
    }

}
