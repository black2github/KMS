package ru.gpb.kms.token.util.exceptions;

public class InvalidKeyApplicationException extends ApplicationException {

    public InvalidKeyApplicationException(String err, Throwable th) {
        super(err, th);
        setCode(3);
    }

    public InvalidKeyApplicationException(String msg) {
        super(msg);
        setCode(3);
    }
}
