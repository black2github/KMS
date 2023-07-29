package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.InvalidKeyApplicationException;
import ru.gazprombank.token.kms.util.exceptions.InvalidPasswordApplicationException;

@ControllerAdvice
public class InvalidPasswordApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidPasswordApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidPasswordApplicationException(final InvalidPasswordApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }
}
