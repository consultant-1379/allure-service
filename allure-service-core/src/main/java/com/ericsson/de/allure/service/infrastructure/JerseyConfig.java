package com.ericsson.de.allure.service.infrastructure;

import com.ericsson.de.allure.service.infrastructure.filter.JaxRsFilter;
import com.ericsson.de.allure.service.presentation.controllers.ReferenceController;
import com.ericsson.de.allure.service.presentation.controllers.ReportController;
import com.ericsson.de.allure.service.presentation.controllers.StaticContentController;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper.NotFoundExceptionMapper;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper.ServiceExceptionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;
import static org.glassfish.jersey.server.ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE;

@Configuration
public class JerseyConfig extends ResourceConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() throws IOException {
        // Disable auto discovery for Jackson ExceptionMapper's
        property(FEATURE_AUTO_DISCOVERY_DISABLE, true);
        register(new JacksonJaxbJsonProvider(objectMapper, DEFAULT_ANNOTATIONS));

        register(JaxRsFilter.class);
        registerControllers();
        registerExceptionMappers();
    }

    private void registerControllers() {
        register(ReportController.class);
        register(ReferenceController.class);
        register(StaticContentController.class);
    }

    private void registerExceptionMappers() {
        register(NotFoundExceptionMapper.class);
        register(ServiceExceptionMapper.class);
    }
}
