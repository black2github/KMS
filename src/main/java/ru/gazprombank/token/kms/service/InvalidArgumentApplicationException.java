package ru.gazprombank.token.kms.service;

public class InvalidArgumentApplicationException extends ApplicationException {

    public InvalidArgumentApplicationException() {
    }

    public InvalidArgumentApplicationException(String err) {
        super(err);
        setCode(1);
    }

    public InvalidArgumentApplicationException(String err, Throwable th) {
        super(err, th);
    }

    public InvalidArgumentApplicationException(int code, String err, Throwable th) {
        super(code, err, th);
    }

    public InvalidArgumentApplicationException(int code, String err) {
        super(code, err);
    }
}
