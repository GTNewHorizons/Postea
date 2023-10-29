package com.colen.postea.mixins.early;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.colen.postea.API.ChunkFixerUtility.processChunkNBT;

@Mixin(AnvilChunkLoader.class)
@SuppressWarnings("unused")
public abstract class MixinAnvilChunkLoader {

//    @Unique
//    private static Class<?> PendingChunkClass;
//
//    static {
//        try {
//            for (Class<?> declaredClass : AnvilChunkLoader.class.getDeclaredClasses()) {
//                if (declaredClass.getSimpleName().equals("PendingChunk")) {
//                    PendingChunkClass = declaredClass;
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * @author Colen
//     * @reason Reasons.
//     */
//    @Unique
//    @Overwrite(remap = false)
//    public Object[] loadChunk__Async(World p_75815_1_, int p_75815_2_, int p_75815_3_) throws IOException
//    {
//        NBTTagCompound nbttagcompound = null;
//        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(p_75815_2_, p_75815_3_);
//        Object object = this.syncLockObject;
//
//        synchronized (this.syncLockObject)
//        {
//            if (this.pendingAnvilChunksCoordinates.contains(chunkcoordintpair)) {
//                Iterator iter = this.chunksToRemove.iterator();
//                while (iter.hasNext()) {
//                    Object potentialPendingChunk = iter.next();
//                    if (PendingChunkClass.isInstance(potentialPendingChunk)) {
//                        try {
//                            Field chunkCoordinateField = PendingChunkClass.getDeclaredField("chunkCoordinate");
//                            chunkCoordinateField.setAccessible(true);
//                            ChunkCoordIntPair chunkCoordinate = (ChunkCoordIntPair) chunkCoordinateField.get(potentialPendingChunk);
//
//                            Field nbtTagsField = PendingChunkClass.getDeclaredField("nbtTags");
//                            nbtTagsField.setAccessible(true);
//                            NBTTagCompound nbtTags = (NBTTagCompound) nbtTagsField.get(potentialPendingChunk);
//
//                            if (chunkCoordinate.equals(chunkcoordintpair)) {
//                                nbttagcompound = nbtTags;
//                                break;
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }
//
//        if (nbttagcompound == null)
//        {
//            DataInputStream datainputstream = RegionFileCache.getChunkInputStream(this.chunkSaveLocation, p_75815_2_, p_75815_3_);
//
//            if (datainputstream == null)
//            {
//                return null;
//            }
//
//            nbttagcompound = CompressedStreamTools.read(datainputstream);
//        }
//
//        return this.checkedReadChunkFromNBT__Async(p_75815_1_, p_75815_2_, p_75815_3_, nbttagcompound);
//    }
//
//    @Unique
//    protected native Object[] checkedReadChunkFromNBT__Async(World p_75822_1_, int p_75822_2_, int p_75822_3_, NBTTagCompound p_75822_4_);

    @Inject(method = "checkedReadChunkFromNBT__Async", at = @At("HEAD"), remap = false)
    private void onCheckedReadChunkFromNBT__Async(World world, int x, int z, NBTTagCompound compound, CallbackInfoReturnable<Object[]> cir) {
        // Your injection code here
        processChunkNBT(compound);
    }

}
