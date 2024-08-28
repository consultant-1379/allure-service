package com.ericsson.de.allure.plugin;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CsvRowTest {

    @Test
    public void testGetJoinedValue() throws Exception {
        assertEquals("", CsvRow.getJoinedValue(null));
        assertEquals("", CsvRow.getJoinedValue(new ArrayList<>()));
        assertEquals("CIP-123", CsvRow.getJoinedValue(Arrays.asList("CIP-123")));
        assertEquals("CIP-123;CIP-234", CsvRow.getJoinedValue(Arrays.asList("CIP-123", "CIP-234")));
        assertEquals("CIP-123", CsvRow.getJoinedValue(Arrays.asList("", "CIP-123")));
        assertEquals("CIP-123", CsvRow.getJoinedValue(Arrays.asList("CIP-123", "")));
        assertEquals("CIP-123", CsvRow.getJoinedValue(Arrays.asList(null, "CIP-123")));
        assertEquals("CIP-123", CsvRow.getJoinedValue(Arrays.asList("CIP-123", null)));
        assertEquals("CIP-123;CIP-234", CsvRow.getJoinedValue(Arrays.asList(null, "CIP-123", null, "CIP-234")));
    }
}
