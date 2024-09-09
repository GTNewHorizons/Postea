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

import com.gtnewhorizons.postea.mixins.interfaces.IChunkMixin;
import com.gtnewhorizons.postea.utility.ChunkFixerUtility;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {

    @Inject(
        method = "checkedReadChunkFromNBT__Async",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;readChunkFromNBT(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;"),
        remap = false)
    private void onCheckedReadChunkFromNBT__Async(World world, int x, int z, NBTTagCompound compound,
        CallbackInfoReturnable<Object[]> cir) {
        ChunkFixerUtility.processChunkNBT(compound, world);
    }

    @Inject(method = "readChunkFromNBT", at = @At("RETURN"))
    private Chunk onReadChunkFromNBT(World world, NBTTagCompound nbtTagCompound, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk instanceof IChunkMixin iChunkMixin) {
            if (nbtTagCompound.hasKey("POSTEA")) {
                long posteaCode = nbtTagCompound.getLong("POSTEA");
                iChunkMixin.Postea$setPosteaCode(posteaCode);

                // Chunk has been updated with Postea, so we will mark it to save later.
                if (posteaCode != ChunkFixerUtility.POSTEA_UPDATE_CODE) chunk.setChunkModified();
            }
        }

        return chunk;
    }

    @Inject(method = "writeChunkToNBT", at = @At("HEAD"))
    private void onWriteChunkToNBT(Chunk chunk, World world, NBTTagCompound nbtTagCompound, CallbackInfo ci) {
        if (chunk instanceof IChunkMixin iChunkMixin) {
            nbtTagCompound.setLong("POSTEA", iChunkMixin.Postea$getPosteaCode());
        }
    }

}
