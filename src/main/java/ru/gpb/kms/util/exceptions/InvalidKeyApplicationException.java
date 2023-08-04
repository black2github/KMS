package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ApplicationException;

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
