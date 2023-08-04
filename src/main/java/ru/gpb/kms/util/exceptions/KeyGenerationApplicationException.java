package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ApplicationException;

public class KeyGenerationApplicationException extends ApplicationException {

    public KeyGenerationApplicationException(String msg) {
        super(msg);
        setCode(5);
    }
    public KeyGenerationApplicationException(String msg, Throwable th) {
        super(msg, th);
        setCode(5);
    }
}
