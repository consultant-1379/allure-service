package com.ericsson.de.allure.plugin;

import java.util.HashMap;
import java.util.Map;

public class KpiItem {

    private Map<String, String> kpiValues = new HashMap<>();

    public Map<String, String> getKpiValues() {
        return kpiValues;
    }

    public void addKpiValue(String kpiName, String kpiValue) {
        kpiValues.put(kpiName, kpiValue);
    }

}
