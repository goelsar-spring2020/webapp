package edu.neu.cloudwebapp.exceptions;

import org.springframework.http.HttpStatus;

public class PostRequestException extends RuntimeException {

    public PostRequestException(HttpStatus status, Throwable message) {
        super(String.valueOf(status), message);
    }
}
