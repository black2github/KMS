package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.KeyNotFoundApplicationException;

@ControllerAdvice
public class KeyNotFoundApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(KeyNotFoundApplicationException.class)
    public ResponseEntity<ErrorResponseDto> keyNotFoundApplicationException(final KeyNotFoundApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }
}
