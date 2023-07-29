package ru.gazprombank.token.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gazprombank.token.kms.entity.Dto.ErrorResponseDto;
import ru.gazprombank.token.kms.util.exceptions.KeyPersistApplicationException;
import ru.gazprombank.token.kms.util.exceptions.SecurityApplicationException;

@ControllerAdvice
public class SecurityApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(SecurityApplicationException.class)
    public ResponseEntity<ErrorResponseDto> securityApplicationException(final SecurityApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
