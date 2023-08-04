package ru.gpb.kms.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.util.exceptions.InvalidArgumentApplicationException;
import ru.gpb.util.handlers.AbstractExceptionHandler;

@ControllerAdvice
public class InvalidArgumentApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(InvalidArgumentApplicationException.class)
    public ResponseEntity<ErrorResponseDto> invalidArgumentApplicationException(final InvalidArgumentApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
