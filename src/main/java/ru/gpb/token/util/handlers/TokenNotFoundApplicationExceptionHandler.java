package ru.gpb.token.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.entity.Dto.ErrorResponseDto;
import ru.gpb.token.util.exceptions.TokenNotFoundApplicationException;
import ru.gpb.util.handlers.AbstractExceptionHandler;

@ControllerAdvice
public class TokenNotFoundApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(TokenNotFoundApplicationException.class)
    public ResponseEntity<ErrorResponseDto> tokenNotFoundApplicationException(final TokenNotFoundApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }
}
