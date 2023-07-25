package ru.gazprombank.token.kms.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface KeyDataMapper.
 * Declares mapper between KeyData and KeyDataDto via MapStruct.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since - 19.07.2023
 */
@Mapper(componentModel = "spring", imports = {LocalDateTime.class, UUID.class})
public interface KeyDataMapper {
    /**
     * Method maps KeyDataDto to KeyData
     * @param keyDataDto KeyDataDto to map from.
     * @return KeyData
     */
    @Mapping(source = "keyDataDto.encodedKey", target = "key")
    KeyData toModel(KeyDataDto keyDataDto);

    default KeyData map(UUID id) {
        KeyData keyData = new KeyData();
        keyData.setId(id);
        return keyData;
    }

    /**
     * Method maps KeyData to KeyDataDto
     * @param keyData KeyData to map from.
     * @return KeyDataDto
     */
    @Mapping(source = "keyData.key", target = "encodedKey")
    KeyDataDto toDto(KeyData keyData);
}
