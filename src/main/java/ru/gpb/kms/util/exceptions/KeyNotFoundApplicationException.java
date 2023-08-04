package ru.gpb.kms.util.exceptions;

import ru.gpb.util.exceptions.ResourceNotFoundException;

import java.util.UUID;

public class KeyNotFoundApplicationException extends ResourceNotFoundException {

    public KeyNotFoundApplicationException() {
        super();
    }

    public KeyNotFoundApplicationException(UUID id) {
        super(id);
    }

    public KeyNotFoundApplicationException(String msg) {
        super(msg);
    }

    public KeyNotFoundApplicationException(String msg, Throwable th) {
        super(msg, th);
    }

    @Override
    protected String getResourceAlias() {
        return "Key";
    }
}
