package com.ericsson.de.allure.service.presentation.controllers;

import com.ericsson.de.allure.service.api.resource.dto.AppInfoBean;
import com.ericsson.de.allure.service.presentation.dto.builder.AppInfoBeanBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@RestController
public class ReferenceController {

    @Value("${info.build.version}")
    private String version;

    @Value("${info.build.name}")
    private String name;

    @Value("${info.build.date}")
    private String buildDate;

    @Value("${info.build.artifact}")
    private String artifactId;

    @Value("${info.build.groupId}")
    private String groupId;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AppInfoBean getInfo() {
        return AppInfoBeanBuilder.anAppInfoBean()
                .withGroupId(groupId)
                .withArtifactId(artifactId)
                .withName(name)
                .withVersion(version)
                .withBuildDate(buildDate)
                .build();
    }
}
