package ru.gpb.kms.token.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.token.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.token.util.exceptions.InvalidPasswordApplicationException;

@ControllerAdvice
public class InvalidPasswordApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidPasswordApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidPasswordApplicationException(final InvalidPasswordApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }
}
