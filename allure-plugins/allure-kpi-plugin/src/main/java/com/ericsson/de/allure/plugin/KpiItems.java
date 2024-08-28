package com.ericsson.de.allure.plugin;

import java.util.HashMap;
import java.util.Map;

public class KpiItems {

    private String kpiType = "UnknownKpi - need to specify with 'KpiId' key";

    private Map<String, KpiItem> kpiValues = new HashMap<>();

    public String getKpiType() {
        return kpiType;
    }

    public void setKpiType(String kpiType) {
        this.kpiType = kpiType;
    }

    public Map<String, KpiItem> getKpiValues() {
        return kpiValues;
    }

    public void addKpiValue(String kpiUid, String kpiName, String kpiValue) {
        KpiItem kpiItem;
        if(kpiValues.containsKey(kpiUid)) {
            kpiItem = kpiValues.get(kpiUid);
        } else {
            kpiItem = new KpiItem();
        }
        kpiItem.addKpiValue(kpiName, kpiValue);
        kpiValues.put(kpiUid, kpiItem);
    }

}
