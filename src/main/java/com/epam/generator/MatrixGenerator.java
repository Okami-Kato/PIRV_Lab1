package com.epam.generator;

import java.util.function.Supplier;

public class MatrixGenerator {
    private final Supplier<Integer> supplier;

    public MatrixGenerator(Supplier<Integer> supplier) {
        this.supplier = supplier;
    }

    public int[] generate(int m, int n) {
        int[] result = new int[n * m];
        for (int i = 0; i < n * m; i++) {
            result[i] = supplier.get();
        }
        return result;
    }
}
