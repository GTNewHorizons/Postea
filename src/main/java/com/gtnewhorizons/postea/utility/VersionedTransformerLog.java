package com.gtnewhorizons.postea.utility;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.gtnewhorizons.postea.Postea;
import com.gtnewhorizons.postea.api.IVersionedTransformer;

/**
 * Announces each version span a transformer is bringing data across once per world, instead of once per chunk.
 */
public final class VersionedTransformerLog {

    private static final Set<String> ANNOUNCED = ConcurrentHashMap.newKeySet();

    private VersionedTransformerLog() {}

    public static void announce(IVersionedTransformer transformer, int stored, int current) {
        if (ANNOUNCED.add(transformer.key() + ' ' + stored + ' ' + current)) {
            Postea.LOG.info(
                "{} is bringing saved data from version {} to {}",
                transformer.key(),
                stored == VersionStamps.UNSTAMPED ? "unstamped" : String.valueOf(stored),
                current);
        }
    }

    public static void clear() {
        ANNOUNCED.clear();
    }
}
