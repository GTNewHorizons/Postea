package com.gtnewhorizons.postea.compat;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.falsepattern.endlessids.mixin.helpers.SubChunkBlockHook;
import com.gtnewhorizons.neid.mixins.interfaces.IExtendedBlockStorageMixin;

public class Compat {

    private static Boolean chunkapi = null;
    private static Boolean endlessids = null;
    private static Boolean neid = null;

    public static boolean chunkapiPresent() {
        if (chunkapi == null) {
            boolean present = false;
            try {
                present = Launch.classLoader.getClassBytes("com.falsepattern.chunk.internal.core.CoreLoadingPlugin")
                    != null;
            } catch (Throwable ignored) {}
            chunkapi = present;
        }
        return chunkapi;
    }

    public static boolean endlessidsPresent() {
        if (endlessids == null) {
            boolean present = false;
            try {
                present = Launch.classLoader.getClassBytes("com.falsepattern.endlessids.asm.EndlessIDsCore") != null;
            } catch (Throwable ignored) {}
            endlessids = present;
        }
        return endlessids;
    }

    public static boolean neidPresent() {
        if (neid == null) {
            boolean present = false;
            try {
                present = Launch.classLoader.getClassBytes("com.gtnewhorizons.neid.core.NEIDCore") != null;
            } catch (Throwable ignored) {}
            neid = present;
        }
        return neid;
    }

    public static SubChunkAccess getSubChunkAccess(ExtendedBlockStorage subChunk) {
        if (endlessidsPresent()) {
            return EndlessIDSCompat.getSubChunkAccess(subChunk);
        } else if (neidPresent()) {
            return NEIDCompat.getSubChunkAccess(subChunk);
        } else {
            return VanillaCompat.getSubChunkAccess(subChunk);
        }
    }

    private static class VanillaCompat {

        public static SubChunkAccess getSubChunkAccess(ExtendedBlockStorage subChunk) {
            final byte[] lsb = subChunk.getBlockLSBArray();
            return new SubChunkAccess() {

                private NibbleArray msb = subChunk.getBlockMSBArray();

                @Override
                public int getBlockId(int x, int y, int z) {
                    int i = toIndex(x, y, z);
                    int id = lsb[i] & 0xFF;
                    if (msb != null) {
                        id |= msb.get(x, y, z) << 8;
                    }
                    return id;
                }

                @Override
                public void setBlockId(int x, int y, int z, int id) {
                    int i = toIndex(x, y, z);
                    int l = id & 0xFF;
                    int h = (id >>> 8) & 0xF;
                    lsb[i] = (byte) l;
                    if (h == 0 && msb == null) return;
                    if (msb == null) {
                        msb = new NibbleArray(lsb.length, 4);
                        subChunk.setBlockMSBArray(msb);
                    }
                    msb.set(x, y, z, h);
                }

                @Override
                public int getMeta(int x, int y, int z) {
                    return subChunk.getExtBlockMetadata(x, y, z);
                }

                @Override
                public void setMeta(int x, int y, int z, int meta) {
                    subChunk.setExtBlockMetadata(x, y, z, meta);
                }
            };
        }
    }

    private static class EndlessIDSCompat {

        public static SubChunkAccess getSubChunkAccess(ExtendedBlockStorage subChunk) {
            final SubChunkBlockHook bh = (SubChunkBlockHook) subChunk;
            return new SubChunkAccess() {

                @Override
                public int getBlockId(int x, int y, int z) {
                    return bh.eid$getID(x, y, z);
                }

                @Override
                public void setBlockId(int x, int y, int z, int id) {
                    bh.eid$setID(x, y, z, id);
                }

                @Override
                public int getMeta(int x, int y, int z) {
                    return bh.eid$getMetadata(x, y, z);
                }

                @Override
                public void setMeta(int x, int y, int z, int meta) {
                    bh.eid$setMetadata(x, y, z, meta);
                }
            };
        }
    }

    private static class NEIDCompat {

        public static SubChunkAccess getSubChunkAccess(ExtendedBlockStorage subChunk) {
            final IExtendedBlockStorageMixin bh = (IExtendedBlockStorageMixin) subChunk;
            final short[] blockArray = bh.getBlock16BArray();
            final short[] metaArray = bh.getBlock16BMetaArray();
            return new SubChunkAccess() {

                @Override
                public int getBlockId(int x, int y, int z) {
                    return blockArray[toIndex(x, y, z)];
                }

                @Override
                public void setBlockId(int x, int y, int z, int id) {
                    blockArray[toIndex(x, y, z)] = (short) id;
                }

                @Override
                public int getMeta(int x, int y, int z) {
                    return metaArray[toIndex(x, y, z)];
                }

                @Override
                public void setMeta(int x, int y, int z, int meta) {
                    metaArray[toIndex(x, y, z)] = (short) meta;
                }
            };
        }
    }

    private static int toIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }
}
