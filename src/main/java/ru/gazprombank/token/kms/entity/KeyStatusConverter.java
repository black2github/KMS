package ru.gazprombank.token.kms.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class KeyStatusConverter implements AttributeConverter<KeyStatus, String> {
    @Override
    public String convertToDatabaseColumn(KeyStatus type) {
        switch (type) {
            case ENABLED: return "ENABLED";
            case DISABLED: return "DISABLED";
            case UNAVAILABLE: return "UNAVAILABLE";
            case PENDING_IMPORT: return "PENDING_IMPORT";
            case PENDING_DELETION: return "PENDING_DELETION";
            case PENDING_CREATION: return "PENDING_CREATION";
            default: return "NONE";
        }
    }
    @Override
    public KeyStatus convertToEntityAttribute(String s) {
        switch (s) {
            case "ENABLED": return KeyStatus.ENABLED;
            case "DISABLED": return KeyStatus.DISABLED;
            case "UNAVAILABLE": return KeyStatus.UNAVAILABLE;
            case "PENDING_IMPORT": return KeyStatus.PENDING_IMPORT;
            case "PENDING_DELETION": return KeyStatus.PENDING_DELETION;
            case "PENDING_CREATION": return KeyStatus.PENDING_CREATION;
            default: return KeyStatus.NONE;
        }
    }
}
