package com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper;

import com.ericsson.de.allure.service.presentation.controllers.exceptions.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Provider
public class NotFoundExceptionMapper extends AbstractExceptionMapper<NotFoundException> {

    @Override
    protected Response.Status getResponseStatus() {
        return NOT_FOUND;
    }

}