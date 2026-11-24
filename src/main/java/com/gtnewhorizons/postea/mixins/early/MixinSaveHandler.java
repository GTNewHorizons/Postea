package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.postea.utility.IDRegistry;

import cpw.mods.fml.common.FMLCommonHandler;

/// Opens [IDRegistry]'s mapping gate when `loadWorldInfo` returns on the server thread, after FML has applied any saved
/// id map.
@Mixin(SaveHandler.class)
public abstract class MixinSaveHandler {

    @Inject(method = "loadWorldInfo", at = @At("RETURN"), require = 1)
    private void postea$markWorldInfoLoaded(CallbackInfoReturnable<WorldInfo> cir) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide()
            .isServer()) IDRegistry.onWorldInfoLoaded();
    }
}
