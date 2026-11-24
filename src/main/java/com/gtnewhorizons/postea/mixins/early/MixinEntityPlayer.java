package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.postea.mixins.interfaces.IPlayerStampsMixin;

/// Carries a player's version stamps to the entity `clonePlayer` builds on respawn, which is not read from NBT.
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    @Inject(method = "clonePlayer", at = @At("RETURN"), require = 1)
    private void postea$carryVersionStamps(EntityPlayer oldPlayer, boolean respawn, CallbackInfo ci) {
        if ((Object) this instanceof IPlayerStampsMixin player && oldPlayer instanceof IPlayerStampsMixin old) {
            player.Postea$setVersionStamps(old.Postea$getVersionStamps());
        }
    }
}
