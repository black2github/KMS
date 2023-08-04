package ru.gpb.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.util.exceptions.InvalidKeyApplicationException;
import ru.gpb.util.handlers.AbstractExceptionHandler;

@ControllerAdvice
public class InvalidKeyApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidKeyApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidKeyApplicationException(final InvalidKeyApplicationException ex) {
        // buildErrorResponse(ex, HttpStatus.  HttpStatus.BAD_REQUEST);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
