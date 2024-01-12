package com.colen.postea.mixins.early;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.colen.postea.mixins.interfaces.IChunkMixin;

@Mixin(Chunk.class)
public class MixinChunk implements IChunkMixin {

    // This new field will store the hash of mod name + versions. We use this to check
    // if we need to reprocess this chunk.
    @Unique
    private long postea_GTNH$posteaID;

    @Unique
    public long Postea$getPosteaCode() {
        return postea_GTNH$posteaID;
    }

    @Unique
    public void Postea$setPosteaCode(long POSTEA_ID) {
        this.postea_GTNH$posteaID = POSTEA_ID;
    }
}
