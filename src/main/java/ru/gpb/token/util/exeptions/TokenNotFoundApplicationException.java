package ru.gpb.token.util.exeptions;

import ru.gpb.util.exceptions.ResourceNotFoundException;

import java.util.UUID;

public class TokenNotFoundApplicationException extends ResourceNotFoundException {

    public TokenNotFoundApplicationException() {
        super();
    }

    public TokenNotFoundApplicationException(UUID uuid) {
        super(uuid);
    }

    public TokenNotFoundApplicationException(String msg) {
        super(msg);
    }

    public TokenNotFoundApplicationException(String msg, Throwable th) {
        super(msg, th);
    }

    @Override
    protected String getResourceAlias() {
        return "Token";
    }
}
