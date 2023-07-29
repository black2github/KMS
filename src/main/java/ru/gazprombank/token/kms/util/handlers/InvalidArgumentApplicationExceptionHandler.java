package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.InvalidArgumentApplicationException;
import ru.gazprombank.token.kms.util.exceptions.InvalidKeyApplicationException;

@ControllerAdvice
public class InvalidArgumentApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidArgumentApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidArgumentApplicationException(final InvalidArgumentApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
