package com.ericsson.de.allure.service.presentation.dto.builder;

import com.ericsson.de.allure.service.api.resource.dto.AppInfoBean;

public final class AppInfoBeanBuilder {
    private String version;
    private String name;
    private String groupId;
    private String artifactId;
    private String buildDate;

    private AppInfoBeanBuilder() {
    }

    public static AppInfoBeanBuilder anAppInfoBean() {
        return new AppInfoBeanBuilder();
    }

    public AppInfoBeanBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public AppInfoBeanBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AppInfoBeanBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public AppInfoBeanBuilder withArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public AppInfoBeanBuilder withBuildDate(String buildDate) {
        this.buildDate = buildDate;
        return this;
    }

    public AppInfoBean build() {
        AppInfoBean appInfoBean = new AppInfoBean();
        appInfoBean.setVersion(version);
        appInfoBean.setName(name);
        appInfoBean.setGroupId(groupId);
        appInfoBean.setArtifactId(artifactId);
        appInfoBean.setBuildDate(buildDate);
        return appInfoBean;
    }
}
