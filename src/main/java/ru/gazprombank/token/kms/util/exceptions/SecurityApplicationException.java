package ru.gazprombank.token.kms.util.exceptions;

import ru.gazprombank.token.kms.util.exceptions.ApplicationException;

public class SecurityApplicationException extends ApplicationException {

    public SecurityApplicationException() {
        setCode(7);
    }

    public SecurityApplicationException(String err) {
        super(err);
        setCode(7);
    }

    public SecurityApplicationException(String err, Throwable th) {
        super(err, th);
        setCode(7);
    }
}
