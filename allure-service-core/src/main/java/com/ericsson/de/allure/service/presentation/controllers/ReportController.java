package com.ericsson.de.allure.service.presentation.controllers;

import com.ericsson.de.allure.service.api.resource.ReportsResource;
import com.ericsson.de.allure.service.application.ReportGenerationService;
import com.ericsson.de.allure.service.application.ResourceUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

public class ReportController implements ReportsResource {

    private static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=\"report-%s.zip\"";

    @Autowired
    private ResourceUploadService resourceUploadService;

    @Autowired
    private ReportGenerationService reportGenerationService;

    @Context
    UriInfo uriInfo;

    @Override
    public Response putReportsByReportId(String reportId, InputStream inputStream) {
        resourceUploadService.upload(reportId, inputStream);
        Link get = Link.fromUri(uriInfo.getAbsolutePath()).rel("self").type("GET").build();
        return Response.ok().links(get).build();
    }

    @Override
    public Response getReportsByReportId(String reportId) {
        File reportFile = reportGenerationService.generate(reportId);

        CacheControl cache = new CacheControl();
        cache.setMaxAge(3600);
        cache.setPrivate(false);
        cache.setNoTransform(true);
        return Response.ok(reportFile, APPLICATION_OCTET_STREAM)
                .cacheControl(cache)
                .header(CONTENT_LENGTH, valueOf(reportFile.length()))
                .header(CONTENT_DISPOSITION, format(CONTENT_DISPOSITION_VALUE, reportId))
                .build();
    }

    @Override
    public Response getReportsCombinedByReportId(final String reportId, final List<String> executionId) {
        File reportFile = reportGenerationService.generate(reportId, executionId);
        final String uri = uriInfo.getBaseUri().toString() + "reports/" + reportId;
        Link get = Link.fromUri(uri).rel("self").type("GET").build();
        return Response.ok(reportFile, APPLICATION_OCTET_STREAM).links(get).build();
    }
}
