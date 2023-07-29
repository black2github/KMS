package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.InvalidKeyApplicationException;
import ru.gazprombank.token.kms.util.exceptions.KeyNotFoundApplicationException;

@ControllerAdvice
public class InvalidKeyApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidKeyApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidKeyApplicationException(final InvalidKeyApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
