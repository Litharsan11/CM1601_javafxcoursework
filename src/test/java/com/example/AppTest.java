package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

class AppTest {

    private App app;
    private File csvFile;
    private File tsvFile;
    private File invalidFile;

    @BeforeEach
    void setUp() throws IOException {
        app = new App();

        // Create a test CSV file
        csvFile = File.createTempFile("test", ".csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("bill_number,item_code,sale_price,quantity,line_total,discount,internal_price,checksum\n");
            writer.write("B001,ITEM1,100.0,2,200.0,10.0,80.0,CHECK1\n");
            writer.write("B002,ITEM2,50.0,3,150.0,5.0,40.0,CHECK2\n");
        }

        // Create a test TSV file
        tsvFile = File.createTempFile("test", ".tsv");
        try (FileWriter writer = new FileWriter(tsvFile)) {
            writer.write("bill_number\titem_code\tsale_price\tquantity\tline_total\tdiscount\tinternal_price\tchecksum\n");
            writer.write("B003\tITEM3\t75.0\t4\t300.0\t15.0\t60.0\tCHECK3\n");
            writer.write("B004\tITEM4\t120.0\t1\t120.0\t20.0\t90.0\tCHECK4\n");
        }

        // Create an invalid file (not enough columns)
        invalidFile = File.createTempFile("invalid", ".csv");
        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write("bill_number,item_code,sale_price\n");
            writer.write("B005,ITEM5,200.0\n");
        }
    }

    @Test
    void testImportDelimitedCSV() throws IOException {
        app.importDelimited(csvFile, ",");

        List<Transaction> transactions = app.transactions;
        assertEquals(2, transactions.size());

        Transaction first = transactions.get(0);
        assertEquals("B001", first.getBillNumber());
        assertEquals("ITEM1", first.getItemCode());
        assertEquals(100.0, first.getSalePrice(), 0.001);
        assertEquals(2, first.getQuantity());
        assertEquals(200.0, first.getLineTotal(), 0.001);
        assertEquals(10.0, first.getDiscount(), 0.001);
        assertEquals(80.0, first.getInternalPrice(), 0.001);
        assertEquals("CHECK1", first.getChecksum());
    }

    @Test
    void testImportDelimitedTSV() throws IOException {
        app.importDelimited(tsvFile, "\t");

        List<Transaction> transactions = app.transactions;
        assertEquals(2, transactions.size());

        Transaction second = transactions.get(1);
        assertEquals("B004", second.getBillNumber());
        assertEquals("ITEM4", second.getItemCode());
        assertEquals(120.0, second.getSalePrice(), 0.001);
        assertEquals(1, second.getQuantity());
        assertEquals(120.0, second.getLineTotal(), 0.001);
        assertEquals(20.0, second.getDiscount(), 0.001);
        assertEquals(90.0, second.getInternalPrice(), 0.001);
        assertEquals("CHECK4", second.getChecksum());
    }

    @Test
    void testImportDelimitedWithInvalidFile() throws IOException {
        app.importDelimited(invalidFile, ",");

        // Should skip the invalid line and only have header
        assertEquals(0, app.transactions.size());
    }

    @Test
    void testImportDelimitedWithEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", ".csv");
        app.importDelimited(emptyFile, ",");

        assertEquals(0, app.transactions.size());
    }

    @Test
    void testImportDelimitedWithNonExistentFile() {
        assertThrows(IOException.class, () -> {
            app.importDelimited(new File("nonexistent.csv"), ",");
        });
    }
}