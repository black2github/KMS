package ru.gazprombank.token.kms.service;

public class KeyNotFoundApplicationException extends ApplicationException {

    public KeyNotFoundApplicationException(String msg) {
        super(msg);
        setCode(2);
    }
}
