package com.ericsson.de.allure.service.presentation.controllers;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import static com.ericsson.de.allure.service.application.util.UnsafeIOConsumer.safely;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.parboiled.common.Preconditions.checkArgument;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 30.06.2017
 */
public class AllureClient {

    private String baseUrl;

    private final CloseableHttpClient client;

    public AllureClient(String baseUrl) {
        this.baseUrl = appendIfMissing(baseUrl, "/");
        client = HttpClientBuilder.create().build();
    }

    public void close() {
        HttpClientUtils.closeQuietly(client);
    }

    /**
     * Uploads ZIP with Allure XML report.
     * Provided input stream will be closed automatically.
     */
    public void uploadReport(String reportId, InputStream xmlReportZip) {
        HttpPut httpPut = new HttpPut(getUploadUrl(reportId));
        httpPut.setEntity(new InputStreamEntity(xmlReportZip));
        CloseableHttpResponse response = safely(() -> client.execute(httpPut));
        checkStatus(response);
        HttpClientUtils.closeQuietly(response);
    }

    /**
     * Downloads ZIP with Allure HTML report.
     * Provided output stream will be closed automatically.
     */
    public void downloadReport(String reportId, OutputStream htmlReportZip) {
        HttpGet httpGet = new HttpGet(getDownloadUrl(reportId));
        executeDownload(htmlReportZip, httpGet);
    }

    private void executeDownload(final OutputStream htmlReportZip, final HttpGet httpGet) {
        CloseableHttpResponse response = safely(() -> client.execute(httpGet));
        checkStatus(response);

        safely(() -> {
            response.getEntity().writeTo(htmlReportZip);
            return null;
        });
        HttpClientUtils.closeQuietly(response);
    }

    public void downloadCombinedReport(String combinedReportId, OutputStream htmlReportZip, String... executionIds){
        HttpGet httpGet = new HttpGet(getCombinedUrl(combinedReportId, executionIds));
        executeDownload(htmlReportZip, httpGet);
    }

    public String getReportUrl(String reportId) {
        return format("%s/index.html", getDownloadUrl(reportId));
    }

    @VisibleForTesting
    String getUploadUrl(String reportId) {
        return format("%sreports/%s", baseUrl, reportId);
    }

    @VisibleForTesting
    String getDownloadUrl(String reportId) {
        return getUploadUrl(reportId);
    }

    @VisibleForTesting
    String getBaseUrl() {
        return baseUrl;
    }

    @VisibleForTesting
    String getCombinedUrl(final String reportName, final String... executionIds) {
        final StringBuilder queryParams = new StringBuilder();
        Stream.of(executionIds).forEach(executionId -> queryParams.append("executionId="+executionId+"&"));
        queryParams.delete(queryParams.lastIndexOf("&"), queryParams.length());
        return format("%sreports/combined/%s?%s", baseUrl, reportName, queryParams.toString());
    }

    private void checkStatus(CloseableHttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        checkArgument(statusCode == 200, "Expected status code 200, but was %s", statusCode);
    }
}
