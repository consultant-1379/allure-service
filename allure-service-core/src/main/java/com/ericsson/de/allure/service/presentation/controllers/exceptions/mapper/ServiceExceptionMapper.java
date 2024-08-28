package com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper;

import com.ericsson.de.allure.service.presentation.controllers.exceptions.ServiceException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class ServiceExceptionMapper extends AbstractExceptionMapper<ServiceException> {

    @Override
    protected Response.Status getResponseStatus() {
        return BAD_REQUEST;
    }

}
