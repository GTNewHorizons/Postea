package com.gtnewhorizons.postea.compat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.oredict.OreDictionary;

import com.falsepattern.endlessids.mixin.helpers.SubChunkBlockHook;
import com.gtnewhorizons.neid.mixins.interfaces.IExtendedBlockStorageMixin;

import codechicken.nei.api.API;

public class Compat {

    private static Boolean endlessids = null;
    private static Boolean neid = null;
    private static Boolean nei = null;

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

    public static boolean neiPresent() {
        if (nei == null) {
            boolean present = false;
            try {
                present = Launch.classLoader.getClassBytes("codechicken.nei.api.API") != null;
            } catch (Throwable ignored) {}
            nei = present;
        }
        return nei;
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

    public static void hideItemFromNEI(Item item) {
        if (!neiPresent()) return;
        NEICompat.hideItem(item);
    }

    private static class NEICompat {

        public static void hideItem(Item item) {
            API.hideItem(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
        }
    }

    private static int toIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }
}
