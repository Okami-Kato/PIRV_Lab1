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
        for (int i = 0; i < n; i++) {
            int blockI = i / blockSize;
            for (int j = 0; j < n; j++) {
                int blockJ = j / blockSize;
                if (i % blockSize == 0 && j % blockSize == 0) {
                    int blockM = ((blockI + 1) * blockSize <= n) ? blockSize : n % blockSize;
                    int blockN = ((blockJ + 1) * blockSize <= n) ? blockSize : n % blockSize;
                    result[blockI * amountOfBlocksInRow + blockJ] = new Block(blockM, blockN);
                }
                Block block = result[blockI * amountOfBlocksInRow + blockJ];
                block.value()[i % block.m() * block.n() + j % block.n()] = matrix[i * n + j];
            }
        }
        return result;
    }

    public static Block sequentialMultiply(Block first, Block second) {
        if (first.n() != second.m()) {
            throw new IllegalArgumentException();
        }
        Block result = new Block(first.m(), second.n());
        for (int i = 0; i < first.m(); i++) {
            for (int j = 0; j < second.n(); j++) {
                int sum = 0;
                for (int k = 0; k < first.n(); k++) {
                    sum += first.value()[i * first.n() + k] * second.value()[k * second.n() + j];
                }
                result.value()[i * result.n() + j] = sum;
            }
        }
        return result;
    }

    public static Block[] sequentialMultiply(Block[] first, Block[] second) {
        if ((int) Math.sqrt(first.length) != (int) Math.sqrt(second.length)) {
            throw new IllegalArgumentException();
        }
        int n = (int) Math.sqrt(first.length);
        Block[] result = new Block[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Block sum = new Block(first[i * n + j].m(), first[i * n + j].n());
                for (int k = 0; k < n; k++) {
                    sum = sum(sum, sequentialMultiply(first[i * n + k], second[k * n + j]));
                }
                result[i * n + j] = sum;
            }
        }
        return result;
    }

    @SneakyThrows
    public static Block[] parallelMultiply(Block[] first, Block[] second) {
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
                    Block sum = new Block(first[finalI * n + finalJ].m(), first[finalI * n + finalJ].n());
                    for (int k = 0; k < n; k++) {
                        sum = sum(sum, sequentialMultiply(first[finalI * n + k], second[k * n + finalJ]));
                    }
                    return sum;
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

        Block[] result = sequentialMultiply(toBlockMatrix(first, blockSize), toBlockMatrix(second, blockSize));

        return toIntArray(result);
    }

    public static int[] parallelMultiply(int[] first, int[] second, int blockSize) {
        if ((int) Math.sqrt(first.length) != (int) Math.sqrt(second.length)) {
            throw new IllegalArgumentException();
        }

        Block[] result = parallelMultiply(toBlockMatrix(first, blockSize), toBlockMatrix(second, blockSize));

        return toIntArray(result);
    }

    public static int[] toIntArray(Block[] blocks) {
        int amountOfBlocksInRow = (int) Math.sqrt(blocks.length);
        int n = 0;
        for (int i = 0; i < amountOfBlocksInRow; i++) {
            n += blocks[i].n();
        }
        int blockSize = blocks[0].n();
        int[] result = new int[n * n];
        for (int i = 0; i < n; i++) {
            int blockI = i / blockSize;
            for (int j = 0; j < n; j++) {
                int blockJ = j / blockSize;
                Block block = blocks[blockI * amountOfBlocksInRow + blockJ];
                result[i * n + j] = block.value()[i % block.m() * block.n() + j % block.n()];
            }
        }
        return result;
    }

    public static Block sum(Block first, Block second) {
        if (first.n() != second.n() || first.m() != second.m()) {
            throw new IllegalArgumentException();
        }
        Block result = new Block(first.m(), first.n());
        for (int i = 0; i < first.m(); i++) {
            for (int j = 0; j < first.n(); j++) {
                int index = i * result.n() + j;
                result.value()[index] = first.value()[index] + second.value()[index];
            }
        }
        return result;
    }
}
