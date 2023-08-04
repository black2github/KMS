package ru.gpb.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.util.exceptions.KeyNotFoundApplicationException;
import ru.gpb.util.handlers.AbstractExceptionHandler;

@ControllerAdvice
public class KeyNotFoundApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(KeyNotFoundApplicationException.class)
    public ResponseEntity<ErrorResponseDto> keyNotFoundApplicationException(final KeyNotFoundApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }
}
