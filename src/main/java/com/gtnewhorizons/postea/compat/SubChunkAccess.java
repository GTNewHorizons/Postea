package com.gtnewhorizons.postea.compat;

public interface SubChunkAccess {

    int getBlockId(int x, int y, int z);

    void setBlockId(int x, int y, int z, int id);

    int getMeta(int x, int y, int z);

    void setMeta(int x, int y, int z, int meta);
}
