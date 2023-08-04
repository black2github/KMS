package ru.gpb.kms.token.entity;

/**
 * Key status.
 */
public enum KeyStatus {
    NONE, // служебный фиктивный статус
    ENABLED,
    DISABLED,
    PENDING_CREATION,
    PENDING_DELETION,
    PENDING_IMPORT,
    UNAVAILABLE
}
