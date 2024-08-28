package com.ericsson.de.allure.service.application.exceptions;

import com.ericsson.de.allure.service.api.resource.dto.ErrorResponse;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.NotFoundException;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.ServiceException;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper.NotFoundExceptionMapper;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.mapper.ServiceExceptionMapper;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApplicationExceptionMapperTest {

    @Test
    public void toResponse_checkNotFoundException() {
        NotFoundExceptionMapper exceptionMapper = new NotFoundExceptionMapper();
        Response response = exceptionMapper.toResponse(new NotFoundException("NotFoundException exception message"));
        assertResponseEntity(response, NOT_FOUND, "NotFoundException exception message");
    }

    @Test
    public void toResponse_checkServiceException() {
        ServiceExceptionMapper exceptionMapper = new ServiceExceptionMapper();
        Response response = exceptionMapper.toResponse(new ServiceException("ServiceException exception message"));
        assertResponseEntity(response, BAD_REQUEST, "ServiceException exception message");
    }

    private void assertResponseEntity(Response response, Response.Status expectedStatus, String expectedMessage) {
        ErrorResponse apiMessage = (ErrorResponse) response.getEntity();
        assertThat(response.getStatus()).isEqualTo(expectedStatus.getStatusCode());
        assertThat(apiMessage.getMessage()).isEqualTo(expectedMessage);
    }
}
