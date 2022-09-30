package com.epam.block;

import com.epam.generator.MatrixGenerator;
import com.epam.util.MatrixUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.epam.block.Blocks.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlocksTest {

    private static final int DEFAULT_ORDER = 0;
    private final String CSV_FILE_PATH = "result/results.csv";
    private final String[] CSV_HEADERS = {"size_of_matrix", "block_size", "sequential_algo", "parallel_algo"};
    MatrixGenerator generator = new MatrixGenerator(() -> new Random().nextInt(100) - 50);
    private ArrayList<Row> results = new ArrayList<>();

    @Test
    void shouldConvertToBlockMatrix() {
        int n = 5;

        int[] matrix = generator.generate(n, n);
        Block[] blocks = toBlockMatrix(matrix, 3);
        assertEquals(4, blocks.length);

        assertEquals(3, blocks[0].m());
        assertEquals(3, blocks[0].n());

        assertEquals(3, blocks[1].m());
        assertEquals(2, blocks[1].n());

        assertEquals(2, blocks[2].m());
        assertEquals(3, blocks[2].n());

        assertEquals(2, blocks[3].m());
        assertEquals(2, blocks[3].n());
    }

    @Test
    void shouldNotConvertToBlockMatrix() {
        int n = 5;
        int[] matrix = generator.generate(n, n);
        assertThrows(IllegalArgumentException.class, () -> Blocks.toBlockMatrix(matrix, n + 1));
    }

    @Test
    void shouldConvertToIntArray() {
        int n = 5;
        int blockSize = 2;
        int[] matrix = generator.generate(n, n);
        Block[] blocks = Blocks.toBlockMatrix(matrix, blockSize);
        int[] converted = Blocks.toIntArray(blocks);
        assertTrue(MatrixUtil.areEqual(matrix, converted, n, n));
    }

    @Test
    void shouldSequentiallyMultiply() {
        int n = 3;
        int blockSize = 2;

        int[] first = generator.generate(n, n);
        int[] second = generator.generate(n, n);

        int[] expected = Blocks.sequentialMultiply(new Block(n, n, first), new Block(n, n, second)).value();
        int[] actual = Blocks.sequentialMultiply(first, second, blockSize);
        assertTrue(MatrixUtil.areEqual(expected, actual, n, n));
    }

    @Test
    void shouldParallelMultiply() {
        int n = 3;
        int blockSize = 2;

        int[] first = generator.generate(n, n);
        int[] second = generator.generate(n, n);

        int[] expected = Blocks.sequentialMultiply(new Block(n, n, first), new Block(n, n, second)).value();
        int[] actual = Blocks.parallelMultiply(first, second, blockSize);
        assertTrue(MatrixUtil.areEqual(expected, actual, n, n));
    }

    @Test
    void shouldAdd() {
        int m = 4;
        int n = 3;

        int[] first = generator.generate(m, n);
        int[] second = generator.generate(m, n);

        Block firstBlock = new Block(m, n, first);
        Block secondBlock = new Block(m, n, second);
        Block sum = sum(firstBlock, secondBlock);

        for (int i = 0; i < m * n; i++) {
            assertEquals(first[i] + second[i], sum.value()[i]);
        }
    }

    @Test
    void shouldNotAdd() {
        int m1 = 4;
        int n1 = 3;

        int m2 = 3;
        int n2 = 3;

        int[] first = generator.generate(m1, n1);
        int[] second = generator.generate(m2, n2);

        Block firstBlock = new Block(m1, n1, first);
        Block secondBlock = new Block(m2, n2, second);
        assertThrows(IllegalArgumentException.class, () -> sum(firstBlock, secondBlock));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data/parameters.csv", delimiter = ';', numLinesToSkip = 1)
    @Order(DEFAULT_ORDER)
    void experiments(int n, int blockSize) {
        Block[] first = toBlockMatrix(generator.generate(n, n), blockSize);
        Block[] second = toBlockMatrix(generator.generate(n, n), blockSize);
        long sequentialStart = System.currentTimeMillis();
        sequentialMultiply(first, second);
        long sequentialTime = System.currentTimeMillis() - sequentialStart;

        long parallelStart = System.currentTimeMillis();
        parallelMultiply(first, second);
        long parallelTime = System.currentTimeMillis() - parallelStart;

        results.add(new Row(n, blockSize, sequentialTime, parallelTime));
    }

    @Test
    @Order(DEFAULT_ORDER + 1)
    void writeResultsToCsv() throws IOException {
        File outputFile = new File(CSV_FILE_PATH);
        outputFile.createNewFile();
        FileWriter out = new FileWriter(outputFile);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.Builder.create().setHeader(CSV_HEADERS).build())) {
            for (Row row : results) {
                printer.printRecord(row.n, row.blockSize, row.sequentialTime, row.parallelTime);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static record Row(int n, int blockSize, long sequentialTime, long parallelTime) {
    }
}