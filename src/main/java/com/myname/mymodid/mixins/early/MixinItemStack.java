package com.myname.mymodid.mixins.early;


import com.myname.mymodid.API.ItemStackKey;
import com.myname.mymodid.API.ReplacementManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    private int itemDamage; // Metadata
    @Shadow
    private cpw.mods.fml.common.registry.RegistryDelegate<Item> delegate;

    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"))
    private void onConstructorReturn(Item item, int size, int metadata, CallbackInfo ci) {
        ItemStackKey replacement = ReplacementManager.getReplacement(item, metadata);

        // Edit private variables here.
        itemDamage = replacement.getDamage();
        delegate = replacement.getItem().delegate;
    }


/*    @Shadow
    private Item field_151002_e;*/
    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void onReadFromNBTReturn(NBTTagCompound p_77963_1_, CallbackInfo ci) {
        ItemStackKey replacement = ReplacementManager.getReplacement(delegate.get(), itemDamage);

        delegate = replacement.getItem().delegate;
        itemDamage = replacement.getDamage();
    }

}
