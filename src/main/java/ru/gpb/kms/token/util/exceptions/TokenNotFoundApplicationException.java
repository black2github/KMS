package ru.gpb.kms.token.util.exceptions;

public class TokenNotFoundApplicationException extends ApplicationException {

    public TokenNotFoundApplicationException() {
        super();
        setCode(8);
    }

    public TokenNotFoundApplicationException(String msg) {
        super(msg);
        setCode(8);
    }

    public TokenNotFoundApplicationException(String msg, Throwable th) {
        super(msg, th);
        setCode(8);
    }
}
