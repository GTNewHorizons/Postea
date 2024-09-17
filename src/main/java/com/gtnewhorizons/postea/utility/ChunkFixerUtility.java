package com.gtnewhorizons.postea.utility;

import static com.gtnewhorizons.postea.api.BlockReplacementManager.blockNotConvertible;
import static com.gtnewhorizons.postea.utility.PosteaUtilities.getModListHash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.gtnewhorizons.neid.mixins.interfaces.IExtendedBlockStorageMixin;
import com.gtnewhorizons.postea.api.BlockReplacementManager;
import com.gtnewhorizons.postea.api.TileEntityReplacementManager;

import akka.japi.Pair;
import cpw.mods.fml.common.registry.GameRegistry;

public class ChunkFixerUtility {

    // This will not change between runs, unless a mod is updated or added.
    public static final int POSTEA_UPDATE_CODE = getModListHash();

    private static final int AIR_ID = 0;
    private static final HashMap<Block, String> loadedBlocks = new HashMap<>();

    public static void transformNormalBlocks(Chunk chunk, ExtendedBlockStorage ebs, World world) {

        int chunkXPos = chunk.xPosition * 16;
        int chunkZPos = chunk.zPosition * 16;

        int sectionY = ebs.getYLocation();

        IExtendedBlockStorageMixin ebsMixin = (IExtendedBlockStorageMixin) ebs;
        short[] blockArray = ebsMixin.getBlock16BArray();
        short[] metadataArray = ebsMixin.getBlock16BMetaArray();

        for (int index = 0; index < blockArray.length; index++) {
            int blockId = blockArray[index];
            int metadata = metadataArray[index];

            // Skip air.
            if (blockId == AIR_ID) continue;
            // If this block has no registered Postea conversion, skip it.
            if (blockNotConvertible(blockId)) continue;

            // Cache block names to improve performance, as findUniqueIdentifierFor is expensive.
            Block block = Block.getBlockById(blockId);
            String blockName = loadedBlocks.computeIfAbsent(
                block,
                b -> GameRegistry.findUniqueIdentifierFor(b)
                    .toString());

            BlockConversionInfo blockConversionInfo = new BlockConversionInfo();
            blockConversionInfo.blockName = blockName;
            blockConversionInfo.blockID = blockId;
            blockConversionInfo.metadata = (byte) metadata; // Updated

            blockConversionInfo.world = world;

            int x = index % 16;
            int y = (index / 256) + (sectionY * 16);
            int z = (index / 16) % 16;

            blockConversionInfo.x = x + chunkXPos + 1;
            blockConversionInfo.y = y;
            blockConversionInfo.z = z + chunkZPos + 1;

            BlockConversionInfo output = BlockReplacementManager.getBlockReplacement(blockConversionInfo, world);

            if (output != null) {
                blockArray[index] = (short) output.blockID;
                metadataArray[index] = (short) output.metadata;
            }
        }
    }

    public static void transformTileEntities(NBTTagCompound levelCompoundTag, World world) {

        Pair<List<ConversionInfo>, NBTTagList> output = adjustTileEntities(
            levelCompoundTag.getTagList("TileEntities", 10),
            world);
        NBTTagList tileEntities = output.second();

        if (tileEntities.tagCount() > 0) {
            levelCompoundTag.setTag("TileEntities", tileEntities);
        }
    }

    private static Pair<List<ConversionInfo>, NBTTagList> adjustTileEntities(NBTTagList tileEntities, World world) {
        List<ConversionInfo> conversionInfo = new ArrayList<>();

        NBTTagList tileEntitiesCopy = new NBTTagList();

        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityId = tileEntity.getString("id");

            // Check if we have a transformer registered for this tile entity ID
            BiFunction<NBTTagCompound, World, BlockInfo> transformationFunction = TileEntityReplacementManager
                .getTileEntityToNormalBlockTransformerFunction(tileEntityId);

            if (transformationFunction != null) {
                int x = tileEntity.getInteger("x");
                int y = tileEntity.getInteger("y");
                int z = tileEntity.getInteger("z");

                BlockInfo blockInfo = transformationFunction.apply(tileEntity, world);
                if (blockInfo == null) {
                    // Do nothing.
                    tileEntitiesCopy.appendTag(tileEntity);
                    continue;
                }

                if (blockInfo.tileTransformer != null) {
                    tileEntitiesCopy.appendTag(blockInfo.tileTransformer.apply(tileEntity));
                } // Otherwise they are removed, therefore not appended.

                conversionInfo.add(new ConversionInfo(x, y, z, blockInfo));
            } else {
                // Do nothing.
                tileEntitiesCopy.appendTag(tileEntity);
            }
        }

        return new Pair<>(conversionInfo, tileEntitiesCopy);
    }

    private static class ConversionInfo {

        public final int x;
        public final int y;
        public final int z;
        public final BlockInfo blockInfo;

        public ConversionInfo(int x, int y, int z, BlockInfo blockInfo) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockInfo = blockInfo;
        }
    }
}
