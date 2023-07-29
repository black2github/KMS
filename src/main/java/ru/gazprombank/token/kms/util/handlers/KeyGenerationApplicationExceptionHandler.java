package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.KeyGenerationApplicationException;
import ru.gazprombank.token.kms.util.exceptions.KeyNotFoundApplicationException;

@ControllerAdvice
public class KeyGenerationApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(KeyGenerationApplicationException.class)
    public ResponseEntity<ErrorResponseDto> keyGenerationApplicationException(final KeyGenerationApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
