package com.gtnewhorizons.postea.api;

/// An [IVersionedTransformer] that changes nothing, at a version the test sets.
public final class StubTransformer implements IVersionedTransformer {

    private final String key;
    public int version;

    public StubTransformer(String key, int version) {
        this.key = key;
        this.version = version;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public int currentVersion() {
        return version;
    }
}
