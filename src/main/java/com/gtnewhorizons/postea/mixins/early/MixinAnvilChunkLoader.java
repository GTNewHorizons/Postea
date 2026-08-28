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

    // Forge-added funnel for sync and async loads; runs after relocation fix-ups and after ChunkAPI's own
    // readChunkFromNBT replacement, so one hook covers vanilla, NEID, EndlessIDs, and ChunkAPI.
    @Inject(method = "checkedReadChunkFromNBT__Async", at = @At("RETURN"), remap = false, require = 1)
    private void postea$chunkHook(CallbackInfoReturnable<Object[]> cir) {
        Object[] data = cir.getReturnValue();
        if (data == null) return;
        Chunk chunk = (Chunk) data[0];
        ChunkFixerUtility.onChunkRead(chunk, chunk.worldObj, ((NBTTagCompound) data[1]).getCompoundTag("Level"));
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
