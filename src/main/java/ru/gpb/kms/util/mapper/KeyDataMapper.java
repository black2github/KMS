package ru.gpb.kms.util.mapper;

import ru.gpb.kms.entity.Dto.KeyDataDto;
import ru.gpb.kms.entity.KeyData;

/**
 * Interface KeyDataMapper.
 * Declares mapper between KeyData and KeyDataDto via MapStruct.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since - 19.07.2023
 */
// @Mapper(componentModel = "spring", imports = {LocalDateTime.class, UUID.class})
public interface KeyDataMapper {

    /**
     * Method maps KeyDataDto to KeyData
     *
     * @param keyDataDto KeyDataDto to map from.
     * @return KeyData
     */
    // @Mapping(source = "keyDataDto.encodedKey", target = "key")
    // @Mapping(source = "encodedKey", target = "key")
    // @Mapping(source = "id", target = "id")
    KeyData toModel(KeyDataDto keyDataDto);

    /**
     * Method maps KeyData to KeyDataDto
     *
     * @param keyData KeyData to map from.
     * @return KeyDataDto
     */
    // @Mapping(source = "keyData.key", target = "encodedKey")
    // @Mapping(source = "key", target = "encodedKey")
    // @Mapping(source = "id", target = "id")
    KeyDataDto toDto(KeyData keyData);

    default String map(KeyData value) {
        if (value == null) return null;
        return value.getId().toString();
    }

    KeyData map(String id);
}
