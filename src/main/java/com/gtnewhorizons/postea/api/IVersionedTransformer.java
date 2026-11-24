package com.gtnewhorizons.postea.api;

import com.gtnewhorizons.postea.utility.VersionStamps;

/// Upgrades saved chunks, players and custom world storage written under another version of a mod's content.
///
/// Postea stamps everything it saves with [#currentVersion()] under [#key()]. On read, the transformer runs for every
/// stored stamp that differs from [#currentVersion()], older or newer. The transformer decides what a newer version
/// means and throws if it cannot handle it. Data saved before the transformer existed reads as [#UNSTAMPED].
///
/// Register with [VersionedReplacementManager#register(IVersionedTransformer)] by postInit. Transformers run in
/// registration order, before every other Postea pass. One whose output depends on another mod's versioned data
/// must register after that mod.
///
/// Chunk transformers may run on chunk I/O threads, several at once for different chunks. They must touch no world
/// state beyond the context, and their own state must be safe to read concurrently.
///
/// Custom world storage offered through [CustomDataReplacementManager] is stamped the same way and reaches
/// [#transformCustomData].
public interface IVersionedTransformer {

    int UNSTAMPED = VersionStamps.UNSTAMPED;

    /// The stamp key, unique across all registered transformers, e.g. `"examplemod:idList"`. Must return the same
    /// value for the transformer's lifetime.
    String key();

    /// The version the transformer's output is written under. Read on every load and save, and may differ between
    /// reads.
    int currentVersion();

    /// Brings a chunk stamped at [ChunkTransformContext#storedVersion()] to [#currentVersion()].
    default void transformChunk(ChunkTransformContext ctx) {}

    /// Brings a player's saved data stamped at [PlayerDataTransformContext#storedVersion()] to [#currentVersion()].
    default void transformPlayer(PlayerDataTransformContext ctx) {}

    /// Brings custom world storage stamped at [CustomDataTransformContext#storedVersion()] to [#currentVersion()].
    /// Stacks in such storage may carry a string `id` instead of a numeric one.
    default void transformCustomData(CustomDataTransformContext ctx) {}
}
