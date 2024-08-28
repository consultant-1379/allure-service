package com.ericsson.de.allure.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.plugins.DefaultTabPlugin;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.model.Label;

@Plugin.Name("kpi")
@Plugin.Priority(110)
public class KpiPlugin extends DefaultTabPlugin {

    @Plugin.Data
    Map<String, KpiItems> kpisById = Maps.newHashMap();

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiPlugin.class);

    @Override
    public void process(AllureTestCase testCase) {
        HashSet<Label> kpis = Sets.newHashSet();

        List<Label> labels = testCase.getLabels();
        if (!labels.isEmpty()) {
            for (Label label : labels) {
                extractKpiFromLabels(kpis, label);
            }

            for (Label kpi : kpis) {
                try {
                    createAndGetKpiGroupItem(kpi);
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOGGER.debug("Could not process Kpi Label '{}' as incorrectly formatted", kpi.getName());
                }
            }
        }
    }

    private void extractKpiFromLabels(HashSet<Label> kpisSet, Label label) {
        if (label.getName().contains("KPI")) {
            kpisSet.add(label);
        }
    }

    private void createAndGetKpiGroupItem(Label kpiLabel) {
        LOGGER.debug("createAndGetKpiGroupItem {}={}", kpiLabel.getName(), kpiLabel.getValue());
        KpiItems kpiItems;
        String[] kpiLabelValues = kpiLabel.getName().split(":::");
        String kpiUid = kpiLabelValues[0];
        String kpiName = kpiLabelValues[1];
        String kpiType = kpiUid.split("::")[1];

        if (!kpisById.containsKey(kpiType)) {
            LOGGER.debug("{} not found in kpisById", kpiType);
            kpiItems = new KpiItems();
            kpiItems.setKpiType(kpiType);
        } else {
            LOGGER.debug("{} found in kpisById", kpiType);
            kpiItems = kpisById.get(kpiType);
        }
        kpiItems.addKpiValue(kpiUid, kpiName, kpiLabel.getValue());
        kpisById.put(kpiType, kpiItems);
    }
}
