package ru.gazprombank.token.kms.service;

public class ApplicationException extends RuntimeException {
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ApplicationException() {
        super();
    }

    public ApplicationException(String err) {
        super(err);
    }

    public ApplicationException(String err, Throwable th) {
        super(err, th);
    }

    public ApplicationException(int code, String err, Throwable th) {
        super(err, th);
        this.code = code;
    }

    public ApplicationException(int code, String err) {
        super(err);
        this.code = code;
    }

    public ApplicationException(int code) {
        super();
        setCode(code);
    }
}
