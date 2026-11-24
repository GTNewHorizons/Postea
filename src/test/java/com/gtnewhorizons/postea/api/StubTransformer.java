package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

/// An [IVersionedTransformer] at a version the test sets, running the test's custom data transform.
public final class StubTransformer implements IVersionedTransformer {

    private final String key;
    public int version;
    public Consumer<CustomDataTransformContext> customData = ctx -> {};

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

    @Override
    public void transformCustomData(CustomDataTransformContext ctx) {
        customData.accept(ctx);
    }
}
