package com.ericsson.de.allure.service.api.resource.dto;

public class ErrorResponse {

    private String message;

    public ErrorResponse() {
        //Intentionally empty
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
