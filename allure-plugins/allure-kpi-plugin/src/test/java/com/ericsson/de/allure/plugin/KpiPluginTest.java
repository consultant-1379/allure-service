package com.ericsson.de.allure.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.qatools.allure.model.Status.BROKEN;
import static ru.yandex.qatools.allure.model.Status.FAILED;
import static ru.yandex.qatools.allure.model.Status.PASSED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.de.allure.plugin.common.TafAllureSuiteUtils;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.Status;

@RunWith(MockitoJUnitRunner.class)
public class KpiPluginTest {

    KpiPlugin plugin;

    @Before
    public void setUp() {
        plugin = new KpiPlugin();
    }

    @Test
    public void shouldAddKpi() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        List<Label> labels = new ArrayList<>();
        labels.add(new Label().withName("KPI::AREA::353:::LabelName").withValue("LabelValue"));

        plugin.process(createTestCase("Passed TestCase", PASSED, suite, labels));

        assertThat(plugin.kpisById.size()).isEqualTo(1);
    }

    @Test
    public void shouldNotAddKpiWhenParameterIsMissing() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        List<Label> labels = new ArrayList<>();
        plugin.process(createTestCase("Passed TestCase", PASSED, suite, labels));

        assertThat(plugin.kpisById.isEmpty()).isTrue();
    }

    @Test
    public void shouldAddAndGroupKpivaluesByKpiName() {
        AllureTestSuiteInfo suite1 = TafAllureSuiteUtils.createSuite("UID1", "Suite1", "Suite Nr1");
        AllureTestSuiteInfo suite2 = TafAllureSuiteUtils.createSuite("UID2", "Suite2", "Suite Nr2");

        List<Label> emptyLabels = new ArrayList<>();
        List<Label> pmLabelA = new ArrayList<>();
        List<Label> pmLabelB = new ArrayList<>();
        List<Label> fmLabel = new ArrayList<>();

        pmLabelA.add(new Label().withName("KPI::PM::353:::PM_Label_1").withValue("17"));
        pmLabelA.add(new Label().withName("KPI::PM::353:::PM_Label_2").withValue("test"));
        pmLabelA.add(new Label().withName("KPI::PM::353:::PM_Label_3").withValue("27-10-2017"));

        pmLabelB.add(new Label().withName("KPI::PM::140:::PM_Label_1").withValue("36"));
        pmLabelB.add(new Label().withName("KPI::PM::140:::PM_Label_2").withValue("hello"));
        pmLabelB.add(new Label().withName("KPI::PM::140:::PM_Label_3").withValue("27-10-2017"));

        fmLabel.add(new Label().withName("KPI::FM::85:::FM_Label_1").withValue("Run1"));
        fmLabel.add(new Label().withName("KPI::FM::85:::FM_Label_2").withValue("7s"));
        fmLabel.add(new Label().withName("KPI::FM::85:::FM_Label_3").withValue("27-10-2017"));
        fmLabel.add(new Label().withName("KPI::FM::85:::FM_Label_4").withValue("Passed"));

        plugin.process(createTestCase("S1: Passed PM_A", PASSED, suite1, pmLabelA));
        plugin.process(createTestCase("S1: Failed PM_B", FAILED, suite1, pmLabelB));
        plugin.process(createTestCase("S2: Broken FM", BROKEN, suite2, fmLabel));
        plugin.process(createTestCase("S1: Failed Empty", FAILED, suite1, emptyLabels));
        plugin.process(createTestCase("S1: Broken Empty", BROKEN, suite1, emptyLabels));
        plugin.process(createTestCase("S2: Passed Empty", PASSED, suite2, emptyLabels));

        Map<String, KpiItems> kpisById = plugin.kpisById;
        assertThat(kpisById.size()).isEqualTo(2);

        KpiItems kpiItems = kpisById.get("PM");
        assertThat(kpiItems.getKpiValues().size()).isEqualTo(2);
        assertThat(kpiItems.getKpiValues().get("KPI::PM::353").getKpiValues().size()).isEqualTo(3);
        assertThat(kpiItems.getKpiValues()).containsKey("KPI::PM::140");
        assertThat(kpiItems.getKpiType()).isEqualTo("PM");

        KpiItems kpiItems2 = kpisById.get("FM");
        assertThat(kpiItems2.getKpiValues().size()).isEqualTo(1);
        assertThat(kpiItems2.getKpiValues().get("KPI::FM::85").getKpiValues().size()).isEqualTo(4);
        assertThat(kpiItems2.getKpiType()).isEqualTo("FM");
    }

    private AllureTestCase createTestCase(String name, Status status, AllureTestSuiteInfo suite, List<Label> labels) {
        AllureTestCase testCase = new AllureTestCase();
        testCase.setName(name);
        testCase.setStatus(status);
        testCase.setSuite(suite);
        testCase.setLabels(labels);
        return testCase;
    }

}
