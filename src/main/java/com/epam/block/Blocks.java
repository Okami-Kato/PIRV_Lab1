package com.epam.block;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.concurrent.*;

import static java.lang.Math.*;

public final class Blocks {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(16);

    public static Block[] toBlockMatrix(int[] matrix, int blockSize) {
        int n = (int) sqrt(matrix.length);
        if (blockSize > n) {
            throw new IllegalArgumentException("Block size can't be greater than matrix size");
        }
        int amountOfBlocksInRow = (int) ceil((double) n / blockSize);
        Block[] result = new Block[amountOfBlocksInRow * amountOfBlocksInRow];
        for (int i = 0; i < n; i += blockSize) {
            int blockI = i / blockSize;
            for (int j = 0; j < n; j += blockSize) {
                int blockJ = j / blockSize;
                int blockM = ((blockI + 1) * blockSize <= n) ? blockSize : n % blockSize;
                int blockN = ((blockJ + 1) * blockSize <= n) ? blockSize : n % blockSize;
                result[blockI * amountOfBlocksInRow + blockJ] = new Block(i, j, blockM, blockN, matrix);
            }
        }
        return result;
    }

    public static Block sequentialMultiply(Block first, Block second, Block result) {
        if (first.n() != second.m()) {
            throw new IllegalArgumentException();
        }

        int firstN = (int) sqrt(first.matrix().length);
        int secondN = (int) sqrt(second.matrix().length);
        int resultN = (int) sqrt(result.matrix().length);

        for (int i = 0; i < first.m(); i++) {
            for (int j = 0; j < second.n(); j++) {
                int sum = 0;
                for (int k = 0; k < first.n(); k++) {
                    sum += first.matrix()[(i + first.i()) * firstN + first.j() + k] * second.matrix()[(second.i() + k) * secondN + (j + second.j())];
                }
                result.matrix()[(first.i() + i) * resultN + (second.j() + j)] += sum;
            }
        }
        return result;
    }

    public static Block[] sequentialMultiply(Block[] first, Block[] second, int[] resultMatrix) {
        if ((int) Math.sqrt(first.length) != (int) Math.sqrt(second.length)) {
            throw new IllegalArgumentException();
        }
        int n = (int) Math.sqrt(first.length);
        Block[] result = new Block[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Block resultBlock = new Block(first[i * n + j].i(), first[i * n + j].j(), first[i * n + j].m(), first[i * n + j].n(), resultMatrix);
                for (int k = 0; k < n; k++) {
                    sequentialMultiply(first[i * n + k], second[k * n + j], resultBlock);
                }
                result[i * n + j] = resultBlock;
            }
        }
        return result;
    }

    @SneakyThrows
    public static Block[] parallelMultiply(Block[] first, Block[] second, int[] resultMatrix) {
        if ((int) Math.sqrt(first.length) != (int) Math.sqrt(second.length)) {
            throw new IllegalArgumentException();
        }
        int n = (int) Math.sqrt(first.length);
        ArrayList<Future<Block>> futureResult = new ArrayList<>(n * n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int finalI = i;
                int finalJ = j;
                Future<Block> blockFuture = executorService.submit(() -> {
                    Block resultBlock = new Block(first[finalI * n + finalJ].i(), first[finalI * n + finalJ].j(), first[finalI * n + finalJ].m(), first[finalI * n + finalJ].n(), resultMatrix);
                    for (int k = 0; k < n; k++) {
                        sequentialMultiply(first[finalI * n + k], second[k * n + finalJ], resultBlock);
                    }
                    return resultBlock;
                });
                futureResult.add(i * n + j, blockFuture);
            }
        }
        Block[] result = new Block[n * n];
        for (int i = 0; i < n * n; i++) {
            result[i] = futureResult.get(i).get();
        }
        return result;
    }

    public static int[] sequentialMultiply(int[] first, int[] second, int blockSize) {
        if ((int) Math.sqrt(first.length) != (int) Math.sqrt(second.length)) {
            throw new IllegalArgumentException();
        }
        int[] resultMatrix = new int[first.length];
        sequentialMultiply(toBlockMatrix(first, blockSize), toBlockMatrix(second, blockSize), resultMatrix);

        return resultMatrix;
    }

    public static int[] parallelMultiply(int[] first, int[] second, int blockSize) {
        if ((int) Math.sqrt(first.length) != (int) Math.sqrt(second.length)) {
            throw new IllegalArgumentException();
        }
        int[] resultMatrix = new int[first.length];
        parallelMultiply(toBlockMatrix(first, blockSize), toBlockMatrix(second, blockSize), resultMatrix);

        return resultMatrix;
    }
}
