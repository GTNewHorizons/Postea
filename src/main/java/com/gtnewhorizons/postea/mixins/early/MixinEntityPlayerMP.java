package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.postea.mixins.interfaces.IPlayerStampsMixin;
import com.gtnewhorizons.postea.utility.PlayerDataFixerUtility;
import com.mojang.authlib.GameProfile;

/// Transforms a server player's saved data on read and stamps it on write.
///
/// The hooks override `Entity.readFromNBT` and `writeToNBT` so the transform also reaches `ForgeData` and the extended
/// properties, which Forge reads before `readEntityFromNBT`. The class extends [EntityPlayer] so the overrides
/// reobfuscate to `Entity`'s srg names.
@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements IPlayerStampsMixin {

    /// The stamps the player's data was read with. [MixinEntityPlayer] carries them across respawns.
    @Unique
    private NBTTagCompound postea$versionStamps;

    private MixinEntityPlayerMP(World world, GameProfile profile) {
        super(world, profile);
    }

    @Unique
    public NBTTagCompound Postea$getVersionStamps() {
        return postea$versionStamps;
    }

    @Unique
    public void Postea$setVersionStamps(NBTTagCompound stamps) {
        this.postea$versionStamps = stamps;
    }

    @Intrinsic
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Intrinsic
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }

    @Dynamic("Merged into EntityPlayerMP by readFromNBT above")
    @SuppressWarnings("target")
    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"), require = 1)
    private void postea$transformPlayerData(NBTTagCompound tag, CallbackInfo ci) {
        postea$versionStamps = PlayerDataFixerUtility.onPlayerRead((EntityPlayerMP) (Object) this, tag);
    }

    @Dynamic("Merged into EntityPlayerMP by writeToNBT above")
    @SuppressWarnings("target")
    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"), require = 1)
    private void postea$stampPlayerData(NBTTagCompound tag, CallbackInfo ci) {
        PlayerDataFixerUtility.onPlayerWrite(tag, postea$versionStamps);
    }
}
