package ru.gazprombank.token.kms.service;

public class KeyPersistApplicationException extends ApplicationException {

    public KeyPersistApplicationException(String msg) {
        super(msg);
        setCode(6);
    }
}
