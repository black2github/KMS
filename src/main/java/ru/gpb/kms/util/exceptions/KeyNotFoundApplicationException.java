package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ApplicationException;

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
