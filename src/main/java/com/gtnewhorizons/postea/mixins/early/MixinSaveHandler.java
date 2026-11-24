package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.postea.utility.IDRegistry;

import cpw.mods.fml.common.FMLCommonHandler;

@Mixin(SaveHandler.class)
public abstract class MixinSaveHandler {

    /// Returns come after FML's `handleWorldDataLoad` applies any saved id map, or when the world has no readable
    /// `level.dat`. FML applies the map only on the server thread.
    @Inject(method = "loadWorldInfo", at = @At("RETURN"), require = 1)
    private void postea$markWorldInfoLoaded(CallbackInfoReturnable<WorldInfo> cir) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide()
            .isServer()) IDRegistry.onWorldInfoLoaded();
    }
}
