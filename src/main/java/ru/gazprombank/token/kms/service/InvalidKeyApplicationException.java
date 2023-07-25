package ru.gazprombank.token.kms.service;

public class InvalidKeyApplicationException extends ApplicationException {


    public InvalidKeyApplicationException(String err, Throwable th) {
        super(err, th);
    }

    public InvalidKeyApplicationException(String msg) {
        super(msg);
        setCode(3);
    }
}
