package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ApplicationException;

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
