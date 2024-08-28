package com.ericsson.de.allure.service.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.UUID;

@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class JaxRsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxRsFilter.class);

    // business identifier for requests
    public static final String CORRELATION_ID = "X-CorrelationID";

    // HTTP call starting time
    public static final String START_TIME = "X-StartTime";

    // HTTP call ending time
    public static final String END_TIME = "X-EndTime";

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        MultivaluedMap<String, String> headers = req.getHeaders();
        String correlationId = headers.getFirst(CORRELATION_ID);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        req.setProperty(START_TIME, currentTime());
        req.setProperty(CORRELATION_ID, correlationId);
        String uri = req.getUriInfo().getRequestUri().toASCIIString();

        MDC.put("req.corrUID", correlationId);

        LOGGER.info("Start {}: {}", req.getMethod(), uri);
        LOGGER.info("Remote address: {}", servletRequest.getRemoteAddr());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        MultivaluedMap<String, Object> headers = res.getHeaders();

        String correlationID = (String) req.getProperty(CORRELATION_ID);
        String startTime = (String) req.getProperty(START_TIME);

        headers.add(END_TIME, currentTime());
        headers.add(CORRELATION_ID, correlationID);

        if (startTime != null) {
            headers.add(START_TIME, startTime);
            double duration = getDuration(Long.valueOf(startTime));
            LOGGER.info("End {}, duration: {}ms\n", correlationID, duration);
        }
        MDC.remove("req.corrUID");
    }

    private static String currentTime() {
        return String.valueOf(System.nanoTime());
    }

    private static double getDuration(long startTime) {
        return (System.nanoTime() - startTime) / 1e6;
    }

}
