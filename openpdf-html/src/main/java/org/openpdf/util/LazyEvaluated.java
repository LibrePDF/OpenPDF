package org.openpdf.util;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class LazyEvaluated<T> {
    @Nullable
    private volatile T value;
    private final Supplier<T> supplier;

    private LazyEvaluated(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return requireNonNull(value);
    }

    public static <T> LazyEvaluated<T> lazy(Supplier<T> supplier) {
        return new LazyEvaluated<T>(supplier);
    }
}
