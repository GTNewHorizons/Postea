package com.gtnewhorizons.postea.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.gtnewhorizons.postea.compat.Compat;
import com.gtnewhorizons.postea.compat.SubChunkAccess;

/**
 * Public API for accessing information related to a block located at the location of a tile entity inside a
 * tile entity transformer registered via
 * {@link TileEntityReplacementManager#tileEntityTransformer(String, TriFunction)}
 */
@SuppressWarnings("unused")
public abstract class BlockAccessCompat {

    /**
     * Fetches the id of the block that is (or used to be) at the position of the TE stored in the given tag.
     *
     * @apiNote This method is intended for use withing TileEntityTransformers declared via
     *          {@link TileEntityReplacementManager#tileEntityTransformer(String, TriFunction)}
     *
     * @param tag   The tag for the tile entity
     * @param chunk The chunk containing the tile entity
     * @return The id of the block that exists (or used to exist) at that location.
     */
    @SuppressWarnings("unused")
    public static int getBlockIDAtTE(NBTTagCompound tag, Chunk chunk) {
        // fetch the block accessor
        ExtendedBlockStorage[] ebsArray = chunk.getBlockStorageArray();
        // abort if y is oob
        int y = tag.getInteger("y");
        if (y >> 4 >= ebsArray.length) return 0;
        SubChunkAccess access = Compat.getSubChunkAccess(ebsArray[y >> 4]);

        // identify the block
        int x = tag.getInteger("x") & 0xf;
        y &= 0xf;
        int z = tag.getInteger("z") & 0xf;
        return access.getBlockId(x, y, z);
    }

    /**
     * Fetches the meta of the block that is (or used to be) at the position of the TE stored in the given tag.
     *
     * @apiNote This method is intended for use withing TileEntityTransformers declared via
     *          {@link TileEntityReplacementManager#tileEntityTransformer(String, TriFunction)}
     *
     * @param tag   The tag for the tile entity
     * @param chunk The chunk containing the tile entity
     * @return The id of the block that exists (or used to exist) at that location.
     */
    @SuppressWarnings("unused")
    public static int getBlockMetaAtTE(NBTTagCompound tag, Chunk chunk) {
        // fetch the block accessor
        ExtendedBlockStorage[] ebsArray = chunk.getBlockStorageArray();
        // abort if y is oob
        int y = tag.getInteger("y");
        if (y >> 4 >= ebsArray.length) return 0;
        SubChunkAccess access = Compat.getSubChunkAccess(ebsArray[y >> 4]);

        // identify the block
        int x = tag.getInteger("x") & 0xf;
        y &= 0xf;
        int z = tag.getInteger("z") & 0xf;
        return access.getMeta(x, y, z);
    }

    /**
     * Fetches the id and meta of the block that is (or used to be) at the position of the TE stored in the given tag.
     *
     * @apiNote This method is intended for use withing TileEntityTransformers declared via
     *          {@link TileEntityReplacementManager#tileEntityTransformer(String, TriFunction)}
     *
     * @param tag   The tag for the tile entity
     * @param chunk The chunk containing the tile entity
     * @return An array with th eid of the block that exists (or used to exist) at that location.
     */
    @SuppressWarnings("unused")
    public static int[] getBlockIDAndMetaAtTE(NBTTagCompound tag, Chunk chunk) {
        // fetch the block accessor
        ExtendedBlockStorage[] ebsArray = chunk.getBlockStorageArray();
        // abort if y is oob
        int y = tag.getInteger("y");
        if (y >> 4 >= ebsArray.length) return new int[] { 0, 0 };
        SubChunkAccess access = Compat.getSubChunkAccess(ebsArray[y >> 4]);

        // identify the block
        int x = tag.getInteger("x") & 0xf;
        y &= 0xf;
        int z = tag.getInteger("z") & 0xf;
        return new int[] { access.getBlockId(x, y, z), access.getMeta(x, y, z) };
    }
}
