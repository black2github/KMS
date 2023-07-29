package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.KeyNotFoundApplicationException;
import ru.gazprombank.token.kms.util.exceptions.KeyPersistApplicationException;

@ControllerAdvice
public class KeyPersistApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(KeyPersistApplicationException.class)
    public ResponseEntity<ErrorResponseDto> KeyPersistApplicationException(final KeyPersistApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
