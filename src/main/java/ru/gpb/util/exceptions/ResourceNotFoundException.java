package ru.gpb.util.exceptions;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class ResourceNotFoundException extends ApplicationException {

    @Override
    public String getMessage() {
        return message;
    }

    private String message;

    protected ResourceNotFoundException() {
        this.message = buildMessage();
        setCode(2);
    }

    protected ResourceNotFoundException(final UUID resourceId) {
        this.message = buildMessage(resourceId);
        setCode(2);
    }

    public ResourceNotFoundException(String msg) {
        super(msg);
        this.message = msg;
        setCode(2);
    }

    public ResourceNotFoundException(String msg, Throwable th) {
        super(msg, th);
        this.message = msg;
        setCode(2);
    }

    private String buildMessage(final UUID resourceId) {
        return getResourceAlias() + "'" + resourceId + "' is not found.";
    }

    private String buildMessage() {
        return "No " + getResourceAlias() + "s are found.";
    }

    protected abstract String getResourceAlias();
}
