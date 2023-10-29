package com.colen.postea.mixins.early;

import com.colen.postea.API.ChunkFixerUtility;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkProviderServer.class)
@SuppressWarnings("unused")
public class MixinChunkProviderServer {

//    @Inject(
//        method = "originalLoadChunk(II)Lnet/minecraft/world/chunk/Chunk;",
//        at = @At("RETURN"),
//        remap=false,
//        cancellable = true
//    )
//    @SuppressWarnings("unused")
//    public void onChunkCreation(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir) {
//        Chunk chunk = cir.getReturnValue();
//
//        Chunk fixedChunk = ChunkFixerUtility.fixChunk(chunk);
//
//        cir.setReturnValue(fixedChunk);
//    }
}
