package ru.gazprombank.token.kms.service;

public class SecurityApplicationException extends ApplicationException {

    public SecurityApplicationException() {
    }

    public SecurityApplicationException(String err) {
        super(err);
    }

    public SecurityApplicationException(String err, Throwable th) {
        super(err, th);
    }
}
