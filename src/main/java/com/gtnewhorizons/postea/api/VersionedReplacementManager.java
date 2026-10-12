package com.gtnewhorizons.postea.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registration of {@link IVersionedTransformer}s. See that interface for the execution contract.
 */
public abstract class VersionedReplacementManager {

    private static final List<IVersionedTransformer> TRANSFORMERS = new CopyOnWriteArrayList<>();

    /**
     * Registers a transformer. Throws {@link IllegalArgumentException} when its key is empty or already registered.
     */
    public static void register(IVersionedTransformer transformer) {
        String key = Objects.requireNonNull(transformer.key(), "key must not be null");
        if (key.isEmpty()) throw new IllegalArgumentException("Versioned transformer key must not be empty");
        for (IVersionedTransformer existing : TRANSFORMERS) {
            if (existing.key()
                .equals(key)) {
                throw new IllegalArgumentException("A versioned transformer is already registered for " + key);
            }
        }
        TRANSFORMERS.add(transformer);
    }

    /**
     * The registered transformers in registration order.
     */
    public static List<IVersionedTransformer> transformers() {
        return Collections.unmodifiableList(TRANSFORMERS);
    }
}
