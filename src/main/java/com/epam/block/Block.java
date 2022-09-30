package com.epam.block;

public record Block(int m, int n, int[] value) {

    public Block(int m, int n) {
        this(m, n, new int[m * n]);
    }
}
