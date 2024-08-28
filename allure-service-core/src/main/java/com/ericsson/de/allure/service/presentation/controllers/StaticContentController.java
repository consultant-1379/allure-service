package com.ericsson.de.allure.service.presentation.controllers;

import com.ericsson.de.allure.service.application.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;

/**
 * Serves Allure report as static content (while it is available shortly after generation).
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 20.06.2017
 */
@javax.ws.rs.Path("reports")
@RestController
public class StaticContentController {

    private static Map<String, MediaType> explicitTypes = new HashMap<>();

    @Autowired
    private ReportGenerationService reportService;

    @GET
    @javax.ws.rs.Path("{reportId}/{resourcePath:.+}")
    public Response getHtmlReport(@PathParam("reportId") String reportId, @PathParam("resourcePath") String path) {

        // resolving file
        Path rootDirectory = reportService.getReportDirectory(reportId);
        Path resourcePath = rootDirectory.resolve(path);
        File resourceFile = resourcePath.toFile();

        // null mime type is OK
        MediaType explicitType = explicitTypes.get(path.substring(path.lastIndexOf('.')));

        // caching
        CacheControl cache = new CacheControl();
        cache.setMaxAge(3600);
        cache.setPrivate(false);
        cache.setNoTransform(true);

        // response
        return Response.ok(resourceFile, explicitType)
                .cacheControl(cache)
                .header(CONTENT_LENGTH, valueOf(resourceFile.length()))
                .build();
    }

    static {
        explicitTypes.put(".html", MediaType.TEXT_HTML_TYPE);
        explicitTypes.put(".js", MediaType.valueOf("text/javascript"));
        explicitTypes.put(".json", MediaType.valueOf("application/json"));
        explicitTypes.put(".css", MediaType.valueOf("text/css"));
        explicitTypes.put(".png", MediaType.valueOf("image/png"));
        explicitTypes.put(".jpg", MediaType.valueOf("image/jpg"));
    }

}
