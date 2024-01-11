package com.colen.postea.mixins.early;

import static com.colen.postea.Utility.ChunkFixerUtility.processChunkNBT;

import com.colen.postea.mixins.interfaces.IChunkMixin;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {

    @Inject(method = "checkedReadChunkFromNBT__Async", at = @At("HEAD"), remap = false)
    private void onCheckedReadChunkFromNBT__Async(World world, int x, int z, NBTTagCompound compound,
        CallbackInfoReturnable<Object[]> cir) {
        processChunkNBT(compound, world);
    }

    @Inject(method = "readChunkFromNBT", at = @At("RETURN"))
    private Chunk onReadChunkFromNBT(World world, NBTTagCompound nbtTagCompound, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk instanceof IChunkMixin iChunkMixin) {

            if (nbtTagCompound.hasKey("POSTEA")) {
                iChunkMixin.Postea$setPosteaCode(nbtTagCompound.getLong("POSTEA"));
                System.out.println("On loading this chunk from NBT, it had a code of " + nbtTagCompound.getLong("POSTEA") + " at coordinates (" + chunk.xPosition + ", " + chunk.zPosition + ").");
            }

        }

        return chunk;
    }

    @Inject(method = "writeChunkToNBT", at = @At("HEAD"))
    private void onWriteChunkToNBT(Chunk chunk, World world, NBTTagCompound nbtTagCompound, CallbackInfo ci) {
        if (chunk instanceof IChunkMixin iChunkMixin) {
            System.out.println("On saving this chunk to NBT, it had a code of " + iChunkMixin.Postea$getPosteaCode() + " at coordinates (" + chunk.xPosition + ", " + chunk.zPosition + ").");
            nbtTagCompound.setLong("POSTEA", iChunkMixin.Postea$getPosteaCode());
        }
    }

}
