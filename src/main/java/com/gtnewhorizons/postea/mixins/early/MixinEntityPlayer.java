package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.postea.mixins.interfaces.IPlayerStampsMixin;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    // A respawned player is a fresh entity built from the old one without going through NBT.
    @Inject(method = "clonePlayer", at = @At("RETURN"), require = 1)
    private void postea$carryVersionStamps(EntityPlayer oldPlayer, boolean respawn, CallbackInfo ci) {
        ((IPlayerStampsMixin) this).Postea$setVersionStamps(((IPlayerStampsMixin) oldPlayer).Postea$getVersionStamps());
    }
}
