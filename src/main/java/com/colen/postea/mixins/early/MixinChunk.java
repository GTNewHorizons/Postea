package com.colen.postea.mixins.early;

import com.colen.postea.API.BlockKey;
import com.colen.postea.API.BlockReplacementManager;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinChunk {

    @Inject(
        method = "readChunkFromNBT(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void modifyChunkData(World world, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();

        // Iterate over each ExtendedBlockStorage. Kinda ugly but does the job...
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage != null) { // Always check if storage isn't null before using it
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) { // This is not a mistake, we do this on purpose because of how minecraft stores stuff.
                        for (int z = 0; z < 16; z++) {

                            // Get the current block and metadata at the position
                            Block currentBlock = storage.getBlockByExtId(x, y, z);
                            int currentMeta = storage.getExtBlockMetadata(x, y, z);

                            // Check if there's a replacement for this block+metadata combination
                            BlockKey replacement = BlockReplacementManager.getBlockReplacement(currentBlock, currentMeta);

                            // If there's a replacement, set the new block and metadata
                            if (replacement != null) {
                                storage.func_150818_a(x, y, z, replacement.getBlock());
                                storage.setExtBlockMetadata(x, y, z, replacement.getDamage());
                            }
                        }
                    }
                }
            }
        }

        cir.setReturnValue(chunk);
    }


}
