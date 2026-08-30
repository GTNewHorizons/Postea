package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.gtnewhorizons.postea.compat.Compat;
import com.gtnewhorizons.postea.compat.SubChunkAccess;
import com.gtnewhorizons.postea.utility.NbtItemStacks;
import com.gtnewhorizons.postea.utility.NbtLists;
import com.gtnewhorizons.postea.utility.VersionStamps;

/**
 * A chunk as handed to {@link IVersionedTransformer#transformChunk}: its block storage and the raw {@code Level}
 * tag as read from disk, before tile entities and entities are parsed. Coordinates are chunk-local
 * ({@code 0..15}, {@code y 0..255}); block ids and metadata are the world's numeric values at full EndlessIDs
 * width. The world is not loaded yet: block access through {@link #world()} is not possible. When a transformer
 * changed a block id, Postea recomputes the height map and flags the chunk for a relight once all transformers ran.
 */
public final class ChunkTransformContext {

    public static final int UNSTAMPED = VersionStamps.UNSTAMPED;

    private final Chunk chunk;
    private final World world;
    private final NBTTagCompound levelTag;
    private final int storedVersion;
    private final int currentVersion;
    private final SubChunkAccess[] sections = new SubChunkAccess[16];
    private boolean blockIdsChanged;

    public ChunkTransformContext(Chunk chunk, World world, NBTTagCompound levelTag, int storedVersion,
        int currentVersion) {
        this.chunk = chunk;
        this.world = world;
        this.levelTag = levelTag;
        this.storedVersion = storedVersion;
        this.currentVersion = currentVersion;
    }

    public Chunk chunk() {
        return chunk;
    }

    public World world() {
        return world;
    }

    public int chunkX() {
        return chunk.xPosition;
    }

    public int chunkZ() {
        return chunk.zPosition;
    }

    /**
     * The chunk's {@code Level} tag. Changes to its {@code TileEntities} and {@code Entities} lists are loaded by
     * the game.
     */
    public NBTTagCompound levelTag() {
        return levelTag;
    }

    public NBTTagList tileEntityTags() {
        return NbtLists.compoundList(levelTag, "TileEntities");
    }

    public NBTTagList entityTags() {
        return NbtLists.compoundList(levelTag, "Entities");
    }

    /**
     * The version this transformer's key was stamped with when the chunk was saved, or {@link #UNSTAMPED}.
     */
    public int storedVersion() {
        return storedVersion;
    }

    public int currentVersion() {
        return currentVersion;
    }

    /**
     * The block id at a chunk-local position; 0 for a missing section.
     */
    public int getBlockId(int x, int y, int z) {
        SubChunkAccess access = section(y >> 4, false);
        return access == null ? 0 : access.getBlockId(x, y & 15, z);
    }

    public int getMetadata(int x, int y, int z) {
        SubChunkAccess access = section(y >> 4, false);
        return access == null ? 0 : access.getMeta(x, y & 15, z);
    }

    /**
     * Sets the block at a chunk-local position, creating the section when a non-air block goes into a missing one.
     */
    public void setBlock(int x, int y, int z, int blockId, int metadata) {
        SubChunkAccess access = section(y >> 4, blockId != 0);
        if (access == null) return;
        if (access.getBlockId(x, y & 15, z) != blockId) blockIdsChanged = true;
        access.setBlockId(x, y & 15, z, blockId);
        access.setMeta(x, y & 15, z, metadata);
    }

    /**
     * Visits every non-air block of every present section.
     */
    public void forEachBlock(BlockVisitor visitor) {
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        for (int index = 0; index < storage.length; index++) {
            if (storage[index] == null) continue;
            SubChunkAccess access = section(index, false);
            int base = index << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int id = access.getBlockId(x, y, z);
                        if (id != 0) visitor.visit(x, base + y, z, id, access.getMeta(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Visits every serialized item stack held by the chunk's tile entities and entities; see
     * {@link NbtItemStacks#forEach}.
     */
    public void forEachItemStackTag(Consumer<NBTTagCompound> visitor) {
        NbtItemStacks.forEach(tileEntityTags(), visitor);
        NbtItemStacks.forEach(entityTags(), visitor);
    }

    /**
     * Whether a block id (not only metadata) was rewritten.
     */
    public boolean blockIdsChanged() {
        return blockIdsChanged;
    }

    private SubChunkAccess section(int index, boolean create) {
        if (index < 0 || index >= sections.length) return null;
        if (sections[index] == null) {
            ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
            if (storage[index] == null) {
                if (!create) return null;
                storage[index] = new ExtendedBlockStorage(index << 4, !world.provider.hasNoSky);
            }
            sections[index] = Compat.getSubChunkAccess(storage[index]);
        }
        return sections[index];
    }

    @FunctionalInterface
    public interface BlockVisitor {

        void visit(int x, int y, int z, int blockId, int metadata);
    }
}
