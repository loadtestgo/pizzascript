package com.loadtestgo.script.engine.internal.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CSVImplTest {
    @Test
    public void testParseLine() {
        List<String> r = CSVImpl.parseCsvRow(
            "a,b,c,d"
        );

        assertEquals(4, r.size());
        assertEquals("a", r.get(0));
        assertEquals("b", r.get(1));
        assertEquals("c", r.get(2));
        assertEquals("d", r.get(3));

        r = CSVImpl.parseCsvRow(
            "\"a\",\"b\",\"c\",\"d\""
        );

        assertEquals(4, r.size());
        assertEquals("a", r.get(0));
        assertEquals("b", r.get(1));
        assertEquals("c", r.get(2));
        assertEquals("d", r.get(3));

        r = CSVImpl.parseCsvRow(
            "\"a\",b,c,d"
        );

        assertEquals(4, r.size());
        assertEquals("a", r.get(0));
        assertEquals("b", r.get(1));
        assertEquals("c", r.get(2));
        assertEquals("d", r.get(3));

        r = CSVImpl.parseCsvRow(
            "\"abcdefg\",b,c,abcdefgh"
        );

        assertEquals(4, r.size());
        assertEquals("abcdefg", r.get(0));
        assertEquals("b", r.get(1));
        assertEquals("c", r.get(2));
        assertEquals("abcdefgh", r.get(3));

        r = CSVImpl.parseCsvRow(
            "\"abc\"\"defg\",b,\"c\",abcdefgh"
        );

        assertEquals(4, r.size());
        assertEquals("abc\"defg", r.get(0));
        assertEquals("b", r.get(1));
        assertEquals("c", r.get(2));
        assertEquals("abcdefgh", r.get(3));
    }

    private BufferedReader reader(String s) {
        InputStream is = new ByteArrayInputStream(s.getBytes());
        return new BufferedReader(new InputStreamReader(is));
    }

    @Test
    public void testParseFile() throws IOException {
        List<String> r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,c\nd,f,g"));
        assertEquals(2, r.size());
        assertEquals("a,b,c", r.get(0));
        assertEquals("d,f,g", r.get(1));

        r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,c\rd,f,g"));
        assertEquals(2, r.size());
        assertEquals("a,b,c", r.get(0));
        assertEquals("d,f,g", r.get(1));

        r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,c\rd,f,g\n"));
        assertEquals(2, r.size());
        assertEquals("a,b,c", r.get(0));
        assertEquals("d,f,g", r.get(1));

        r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,c\r\nd,f,g\n"));
        assertEquals(2, r.size());
        assertEquals("a,b,c", r.get(0));
        assertEquals("d,f,g", r.get(1));

        r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,c\n\rd,f,g\n"));
        assertEquals(2, r.size());
        assertEquals("a,b,c", r.get(0));
        assertEquals("d,f,g", r.get(1));

        r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,c\r\nd,f,g\n\n"));
        assertEquals(3, r.size());
        assertEquals("a,b,c", r.get(0));
        assertEquals("d,f,g", r.get(1));
        assertEquals("", r.get(2));

        r = CSVImpl.parseCsvRowsIntoArray(reader("a,b,\"c\nd\",f,g\n"));
        assertEquals(1, r.size());
        assertEquals("a,b,c\nd,f,g", r.get(0));
    }

    @Test
    public void testWithBom() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("bom.csv");
        CSVImpl csv = new CSVImpl(inputStream);
        CSVImpl.Row row = csv.row(1);
        assertEquals(1, row.getNumColumns());
        assertEquals(2, csv.getNumRows());
        assertEquals("hello", csv.value(0, 0));
        assertEquals("bom", csv.value(1, 0));
        assertEquals("bom", csv.value(1, "hello"));
    }
}
