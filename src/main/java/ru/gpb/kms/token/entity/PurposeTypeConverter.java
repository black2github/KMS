package ru.gpb.kms.token.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// @Converter
// class KeyTypeConverter implements AttributeConverter<KeyType, String> {
//     @Override
//     public String convertToDatabaseColumn(KeyType keyType) {
//         switch (keyType) {
//             case PUBLIC: return "PUBLIC";
//             case PRIVATE: return "PRIVATE";
//             default: return "SYMMETRIC";
//         }
//     }
//     @Override
//     public KeyType convertToEntityAttribute(String s) {
//         switch (s) {
//             case "PUBLIC": return KeyType.PUBLIC;
//             case "PRIVATE": return KeyType.PRIVATE;
//             default: return KeyType.SYMMETRIC;
//         }
//     }
// }
@Converter
class PurposeTypeConverter implements AttributeConverter<PurposeType, String> {
    @Override
    public String convertToDatabaseColumn(PurposeType type) {
        switch (type) {
            case KEK:
                return "KEK";
            case DEK:
                return "DEK";
            case CEK:
                return "CEK";
            default:
                return "SIG";
        }
    }

    @Override
    public PurposeType convertToEntityAttribute(String s) {
        switch (s) {
            case "KEK":
                return PurposeType.KEK;
            case "DEK":
                return PurposeType.DEK;
            case "CEK":
                return PurposeType.CEK;
            default:
                return PurposeType.SIG;
        }
    }
}
