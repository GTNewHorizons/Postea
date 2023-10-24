package com.colen.postea.mixins.early;

import com.colen.postea.API.ChunkFixerUtility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilChunkLoader.class)
@SuppressWarnings("unused")
public abstract class MixinAnvilChunkLoader {

    @Inject(
        method = "readChunkFromNBT(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;",
        at = @At("RETURN"),
        cancellable = true
    )
    @SuppressWarnings("unused")
    private void onChunkReloadFromWorldSaveData(World world, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();

        Chunk fixedChunk = ChunkFixerUtility.fixChunk(chunk);

        cir.setReturnValue(fixedChunk);
    }

}
