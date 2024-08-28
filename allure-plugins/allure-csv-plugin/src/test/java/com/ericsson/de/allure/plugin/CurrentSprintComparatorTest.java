package com.ericsson.de.allure.plugin;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CurrentSprintComparatorTest {

    private CurrentSprintComparator comparator = new CurrentSprintComparator("current");

    @Test
    public void compare() {

        // testing order
        List<CsvRow> list = asList(of(""), of("previous"), of("current"), of("next"), of(""));
        Collections.sort(list, comparator);
        assertOrder(list, "current", "", "previous", "next", "");

        // testing sprint matching the record
        list = asList(of(""), of("previous,current"), of("current, next"), of("previous, current, next"), of(""));
        Collections.sort(list, comparator);
        assertOrder(list, "previous,current", "current, next", "previous, current, next", "", "");
    }

    private CsvRow of(String sprint) {
        CsvRow csvRow = new CsvRow();
        csvRow.setSprint(sprint);
        return csvRow;
    }

    private void assertOrder(List<CsvRow> list, String... expectedList) {
        assertEquals(list.size(), expectedList.length);
        for (int i = 0; i < list.size(); i++) {
            String actual = list.get(i).getSprint();
            String expected = expectedList[i];
            assertEquals(expected, actual);
        }
    }
}
