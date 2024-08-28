package com.ericsson.de.allure.plugin;

import com.google.common.annotations.VisibleForTesting;

import java.util.Comparator;

@VisibleForTesting
class CurrentSprintComparator implements Comparator<CsvRow> {

    private String currentSprint;

    public CurrentSprintComparator(String currentSprint) {
        this.currentSprint = currentSprint;
    }

    @Override
    public int compare(CsvRow csvRow1, CsvRow csvRow2) {
        boolean row1Candidate = isInCurrentSprint(csvRow1);
        boolean row2Candidate = isInCurrentSprint(csvRow2);
        if (row1Candidate == row2Candidate) {
            return 0;
        }
        return row1Candidate ? -1 : 1;
    }

    private boolean isInCurrentSprint(CsvRow csvRow) {
        return csvRow.getSprint().contains(currentSprint);
    }
}
