package com.gtnewhorizons.postea.mixins.late;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import com.gtnewhorizons.postea.utility.ChunkFixerUtility;

@Mixin(value = DataRegistryImpl.class, remap = false)
public abstract class MixinDataRegistryImpl {

    @Inject(
        method = "readChunkFromNBT(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/nbt/NBTTagCompound;)V",
        at = @At("RETURN"),
        require = 1)
    @SuppressWarnings("unused")
    private static void Postea$chunkReadHook(Chunk chunk, NBTTagCompound chunkNBT, CallbackInfo ci) {
        ChunkFixerUtility.onChunkRead(chunk, chunk.worldObj, chunkNBT);
    }
}
