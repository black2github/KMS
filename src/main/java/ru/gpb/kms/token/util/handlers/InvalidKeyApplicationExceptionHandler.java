package ru.gpb.kms.token.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.token.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.token.util.exceptions.InvalidKeyApplicationException;

@ControllerAdvice
public class InvalidKeyApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidKeyApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidKeyApplicationException(final InvalidKeyApplicationException ex) {
        // buildErrorResponse(ex, HttpStatus.  HttpStatus.BAD_REQUEST);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
