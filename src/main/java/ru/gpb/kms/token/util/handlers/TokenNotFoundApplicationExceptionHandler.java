package ru.gpb.kms.token.util.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.gpb.kms.token.entity.Dto.ErrorResponseDto;
import ru.gpb.kms.token.util.exceptions.TokenNotFoundApplicationException;

@ControllerAdvice
public class TokenNotFoundApplicationExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(TokenNotFoundApplicationException.class)
    public ResponseEntity<ErrorResponseDto> tokenNotFoundApplicationException(final TokenNotFoundApplicationException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }
}
