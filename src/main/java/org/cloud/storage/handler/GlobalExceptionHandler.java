package org.cloud.storage.handler;

import org.cloud.storage.dto.ErrorDto;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ErrorDto error(Exception e) {
        return new ErrorDto(e.getMessage());
    }
}
