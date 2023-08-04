package ru.gpb.kms.token.util.exceptions;

public class KeyNotFoundApplicationException extends ApplicationException {

    public KeyNotFoundApplicationException() {
        super();
        setCode(2);
    }

    public KeyNotFoundApplicationException(String msg) {
        super(msg);
        setCode(2);
    }

    public KeyNotFoundApplicationException(String msg, Throwable th) {
        super(msg, th);
        setCode(2);
    }
}