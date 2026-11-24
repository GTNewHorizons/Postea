package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.integrated.IntegratedPlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Keeps the singleplayer host's saved player in `level.dat` when the host's login fails before the player is read.
///
/// A host whose read fails is logged out with a player that was never loaded. `IntegratedPlayerList` records host data
/// before the `playerNetServerHandler` check in `ServerConfigurationManager.writePlayerData`, and `level.dat` is saved
/// without a player while no host data exists.
@Mixin(IntegratedPlayerList.class)
public abstract class MixinIntegratedPlayerList {

    /// Skips a player that never finished logging in, as the superclass does.
    @Inject(
        method = "writePlayerData(Lnet/minecraft/entity/player/EntityPlayerMP;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void postea$skipUnloadedPlayer(EntityPlayerMP player, CallbackInfo ci) {
        if (player.playerNetServerHandler == null) ci.cancel();
    }

    /// Returns the player read from `level.dat` while no host data has been written.
    @Inject(
        method = "getHostPlayerData()Lnet/minecraft/nbt/NBTTagCompound;",
        at = @At("RETURN"),
        cancellable = true,
        require = 1)
    private void postea$keepReadHostPlayer(CallbackInfoReturnable<NBTTagCompound> cir) {
        if (cir.getReturnValue() != null) return;
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld != null) cir.setReturnValue(
            overworld.getWorldInfo()
                .getPlayerNBTTagCompound());
    }
}
