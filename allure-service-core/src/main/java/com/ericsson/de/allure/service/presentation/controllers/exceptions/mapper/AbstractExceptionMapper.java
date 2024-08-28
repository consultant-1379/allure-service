package com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper;

import com.ericsson.de.allure.service.api.resource.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionMapper.class);

    @Override
    public Response toResponse(T exception) {
        LOGGER.warn(exception.getMessage(), exception);
        return createApiMessageResponse(getResponseStatus(), exception.getMessage());
    }

    protected abstract Response.Status getResponseStatus();

    private Response createApiMessageResponse(Response.Status status, String message) {
        return Response.status(status)
                .entity(new ErrorResponse(message))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
