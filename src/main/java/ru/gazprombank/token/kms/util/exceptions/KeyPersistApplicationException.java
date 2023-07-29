package ru.gazprombank.token.kms.util.exceptions;

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
