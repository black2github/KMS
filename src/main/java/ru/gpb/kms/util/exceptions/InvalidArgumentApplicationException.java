package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ApplicationException;

public class InvalidArgumentApplicationException extends ApplicationException {

    public InvalidArgumentApplicationException(String err) {
        super(err);
        setCode(1);
    }

    public InvalidArgumentApplicationException(String err, Throwable th) {
        super(err, th);
        setCode(1);
    }

    // public InvalidArgumentApplicationException(int code, String err, Throwable th) {
    //     super(code, err, th);
    // }
    //
    // public InvalidArgumentApplicationException(int code, String err) {
    //     super(code, err);
    // }
}
