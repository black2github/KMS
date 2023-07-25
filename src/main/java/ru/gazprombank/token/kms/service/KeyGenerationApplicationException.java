package ru.gazprombank.token.kms.service;

public class KeyGenerationApplicationException extends ApplicationException {

    public KeyGenerationApplicationException(String msg) {
        super(msg);
        setCode(5);
    }
}
