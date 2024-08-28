package com.ericsson.de.allure.service.presentation.dto.builder;

import com.ericsson.de.allure.service.api.resource.dto.AppInfoBean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AppInfoBeanBuilderTest {

    @Test
    public void createAndVerifyFullAppInfoBean() {
        String groupId = "com.ericsson.de";
        String artifactId = "allure-service-core";
        String name = "Allure Report Service Core";
        String version = "1.0.0-SNAPSHOT";
        String buildDate = "timestamp";

        AppInfoBean bean = AppInfoBeanBuilder.anAppInfoBean()
                .withGroupId(groupId)
                .withArtifactId(artifactId)
                .withName(name)
                .withVersion(version)
                .withBuildDate(buildDate)
                .build();
        assertThat(bean).isNotNull();
        assertThat(bean.getName()).isEqualTo(name);
        assertThat(bean.getArtifactId()).isEqualTo(artifactId);
        assertThat(bean.getGroupId()).isEqualTo(groupId);
        assertThat(bean.getVersion()).isEqualTo(version);
        assertThat(bean.getBuildDate()).isEqualTo(buildDate);
    }

    @Test
    public void createAndVerifyEmptyAppInfoBean() {
        AppInfoBean bean = AppInfoBeanBuilder.anAppInfoBean().build();
        assertThat(bean).isNotNull();
        assertThat(bean.getName()).isNullOrEmpty();
        assertThat(bean.getArtifactId()).isNullOrEmpty();
        assertThat(bean.getGroupId()).isNullOrEmpty();
        assertThat(bean.getVersion()).isNullOrEmpty();
        assertThat(bean.getBuildDate()).isNullOrEmpty();
    }
}
