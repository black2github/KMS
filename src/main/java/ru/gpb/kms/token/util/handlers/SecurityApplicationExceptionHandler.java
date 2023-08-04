package ru.gpb.kms.token.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.token.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.token.util.exceptions.SecurityApplicationException;

@ControllerAdvice
public class SecurityApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(SecurityApplicationException.class)
    public ResponseEntity<ErrorResponseDto> securityApplicationException(final SecurityApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
