package ru.gazprombank.token.kms.util.exceptions;

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
