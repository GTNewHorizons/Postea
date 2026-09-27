package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.postea.utility.ChunkFixerUtility;

@Mixin(value = AnvilChunkLoader.class, priority = 1100)
public abstract class MixinAnvilChunkLoader {

    // This hook is compatible with Vanilla/NEID/EIDs
    @Inject(method = "readChunkFromNBT", at = @At("RETURN"), require = 1)
    private void postea$chunkHook(World world, NBTTagCompound tag, CallbackInfoReturnable<Chunk> cir) {
        ChunkFixerUtility.onChunkRead(cir.getReturnValue(), world, tag);
    }

    @Inject(method = "loadChunk", at = @At("RETURN"))
    private void postea$updatePosteaChunkCode(World world, int i, int j, CallbackInfoReturnable<Chunk> cir) {
        ChunkFixerUtility.onChunkLoaded(cir.getReturnValue());
    }

    @Inject(method = "writeChunkToNBT", at = @At("HEAD"), order = 900)
    private void postea$writePosteaChunkCode(Chunk chunk, World world, NBTTagCompound nbtTagCompound, CallbackInfo ci) {
        ChunkFixerUtility.onChunkWrite(chunk, nbtTagCompound);
    }

}
