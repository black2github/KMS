package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ApplicationException;

public class KeyPersistApplicationException extends ApplicationException {

    public KeyPersistApplicationException(String msg) {
        super(msg);
        setCode(6);
    }
    public KeyPersistApplicationException(String msg, Throwable th) {
        super(msg, th);
        setCode(6);
    }
}
