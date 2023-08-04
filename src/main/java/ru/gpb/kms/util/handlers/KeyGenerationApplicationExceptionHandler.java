package ru.gpb.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.util.exceptions.KeyGenerationApplicationException;
import ru.gpb.util.handlers.AbstractExceptionHandler;

@ControllerAdvice
public class KeyGenerationApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(KeyGenerationApplicationException.class)
    public ResponseEntity<ErrorResponseDto> keyGenerationApplicationException(final KeyGenerationApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
