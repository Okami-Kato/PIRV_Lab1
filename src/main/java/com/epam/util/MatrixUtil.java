package com.epam.util;

public final class MatrixUtil {

    public static int[][] to2DArray(int[] matrix, int m, int n) {
        int[][] result = new int[m][n];

        for (int i = 0; i < m; i++) {
            System.arraycopy(matrix, i * n, result[i], 0, n);
        }
        return result;
    }

    public static boolean areEqual(int[][] first, int[][] second, int m, int n) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (first[i][j] != second[i][j]) return false;
            }
        }
        return true;
    }

    public static boolean areEqual(int[] first, int[] second, int m, int n) {
        for (int i = 0; i < m * n; i++) {
            if (first[i] != second[i]) return false;
        }
        return true;
    }
}
