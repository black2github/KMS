package ru.gazprombank.token.kms.service;

public class InvalidPasswordApplicationException extends ApplicationException {

    public InvalidPasswordApplicationException(String msg) {
        super(msg);
        setCode(4);
    }
}
