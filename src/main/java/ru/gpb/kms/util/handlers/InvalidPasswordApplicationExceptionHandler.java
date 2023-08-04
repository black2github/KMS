package ru.gpb.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.util.exceptions.InvalidPasswordApplicationException;
import ru.gpb.util.handlers.AbstractExceptionHandler;

@ControllerAdvice
public class InvalidPasswordApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidPasswordApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidPasswordApplicationException(final InvalidPasswordApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }
}
