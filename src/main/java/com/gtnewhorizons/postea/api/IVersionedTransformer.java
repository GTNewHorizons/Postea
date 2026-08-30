package com.gtnewhorizons.postea.api;

/**
 * A transformer that brings saved data written under an older version of its owner's content up to the current
 * version. Postea stamps every chunk and every player it saves with {@link #currentVersion()} under {@link #key()}
 * and, when reading, invokes the transformer only for data whose stamp differs, so each chunk and player is seen
 * exactly once per version change no matter how many sessions passed in between. Data saved before the transformer
 * existed reads as {@link ChunkTransformContext#UNSTAMPED}; the transformer decides what that means.
 * <p>
 * Register through {@link VersionedReplacementManager#register(IVersionedTransformer)} during mod loading, postInit
 * at the latest. Transformers run in registration order and before every other Postea pass, so a transformer whose
 * output depends on another mod's versioned data (for example one that writes that mod's indices) must register
 * after that mod; FML's dependency ordering guarantees this when both register in the same lifecycle phase.
 * <p>
 * Chunk transformers may run on a chunk I/O thread, several concurrently for different chunks. They must not touch
 * world state beyond the context and their own state must be safe to read concurrently.
 */
public interface IVersionedTransformer {

    /**
     * The stamp key, unique across all registered transformers, e.g. {@code "examplemod:idList"}.
     */
    String key();

    /**
     * The version the transformer's output is written under. Sampled at every read and every save.
     */
    int currentVersion();

    /**
     * Brings a chunk stamped at {@link ChunkTransformContext#storedVersion()} to {@link #currentVersion()}.
     */
    default void transformChunk(ChunkTransformContext ctx) {}

    /**
     * Brings a player's saved data stamped at {@link PlayerDataTransformContext#storedVersion()} to
     * {@link #currentVersion()}.
     */
    default void transformPlayer(PlayerDataTransformContext ctx) {}
}
