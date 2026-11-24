package com.gtnewhorizons.postea.api;

/// A block's namespaced id and meta value, as returned by [BlockReplacementManager#transformBlock(String, int)].
public final class BlockNameAndMeta {

    /// The namespaced id of the block.
    public final String name;
    /// The meta value of the block.
    public final int metadata;

    public BlockNameAndMeta(String name, int metadata) {
        this.name = name;
        this.metadata = metadata;
    }
}
