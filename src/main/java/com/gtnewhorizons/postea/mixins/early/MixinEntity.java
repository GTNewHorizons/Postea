package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.postea.mixins.interfaces.IPlayerStampsMixin;
import com.gtnewhorizons.postea.utility.PlayerDataFixerUtility;

@Mixin(Entity.class)
public abstract class MixinEntity implements IPlayerStampsMixin {

    // The stamps a server player's data was read with; carried across respawns by MixinEntityPlayer.
    @Unique
    private NBTTagCompound postea$versionStamps;

    @Unique
    public NBTTagCompound Postea$getVersionStamps() {
        return postea$versionStamps;
    }

    @Unique
    public void Postea$setVersionStamps(NBTTagCompound stamps) {
        this.postea$versionStamps = stamps;
    }

    @Inject(method = "readFromNBT", at = @At("HEAD"), require = 1)
    private void postea$transformPlayerData(NBTTagCompound tag, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerMP player) {
            postea$versionStamps = PlayerDataFixerUtility.onPlayerRead(player, tag);
        }
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"), require = 1)
    private void postea$stampPlayerData(NBTTagCompound tag, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerMP) {
            PlayerDataFixerUtility.onPlayerWrite(tag, postea$versionStamps);
        }
    }
}
