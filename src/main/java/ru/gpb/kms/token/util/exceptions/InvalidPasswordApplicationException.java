package ru.gpb.kms.token.util.exceptions;

public class InvalidPasswordApplicationException extends ApplicationException {

    public InvalidPasswordApplicationException(String err, Throwable th) {
        super(err, th);
        setCode(4);
    }

    public InvalidPasswordApplicationException(String msg) {
        super(msg);
        setCode(4);
    }
}
