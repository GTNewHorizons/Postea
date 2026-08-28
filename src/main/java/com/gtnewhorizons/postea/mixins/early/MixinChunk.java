package com.gtnewhorizons.postea.mixins.early;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.postea.mixins.interfaces.IChunkMixin;

@Mixin(Chunk.class)
public class MixinChunk implements IChunkMixin {

    // This new field will store the hash of mod name + versions. We use this to check
    // if we need to reprocess this chunk.
    @Unique
    private long postea_GTNH$posteaID;

    // The stamps the chunk was read with; null for a chunk generated this session.
    @Unique
    private NBTTagCompound postea$versionStamps;

    @Unique
    private boolean postea$versionedDirty;

    @Unique
    public long Postea$getPosteaCode() {
        return postea_GTNH$posteaID;
    }

    @Unique
    public void Postea$setPosteaCode(long POSTEA_ID) {
        this.postea_GTNH$posteaID = POSTEA_ID;
    }

    @Unique
    public NBTTagCompound Postea$getVersionStamps() {
        return postea$versionStamps;
    }

    @Unique
    public void Postea$setVersionStamps(NBTTagCompound stamps) {
        this.postea$versionStamps = stamps;
    }

    @Unique
    public boolean Postea$isVersionedDirty() {
        return postea$versionedDirty;
    }

    @Unique
    public void Postea$setVersionedDirty(boolean dirty) {
        this.postea$versionedDirty = dirty;
    }
}
