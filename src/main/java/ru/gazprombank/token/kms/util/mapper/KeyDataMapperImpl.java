package ru.gazprombank.token.kms.util.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.repository.KeyDataRepository;

import javax.annotation.processing.Generated;
import java.util.UUID;

@Generated(
        value = "org.mapstruct.ap.MappingProcessor"
)
@Component
public class KeyDataMapperImpl implements KeyDataMapper {
    @Autowired
    private KeyDataRepository keyDataRepository;

    public KeyData map(String id) {
        if (id == null) return null;
        return keyDataRepository.findById(UUID.fromString(id)).orElse(null);
    }

    @Override
    public KeyData toModel(KeyDataDto keyDataDto) {
        if (keyDataDto == null) {
            return null;
        }

        KeyData keyData = new KeyData();

        keyData.setId(UUID.fromString(keyDataDto.getId()));
        keyData.setAlias(keyDataDto.getAlias());
        keyData.setDescription(keyDataDto.getDescription());
        keyData.setKey(keyDataDto.getKey());
        keyData.setExpiryDate(keyDataDto.getExpiryDate());
        keyData.setAlgorithm(keyDataDto.getAlgorithm());
        keyData.setNotifyDate(keyDataDto.getNotifyDate());
        keyData.setKeyType(keyDataDto.getKeyType());
        keyData.setPurposeType(keyDataDto.getPurposeType());
        keyData.setRelatedKey(map(keyDataDto.getRelatedKey()));
        keyData.setCreatedDate(keyDataDto.getCreatedDate());
        keyData.setStatus(keyDataDto.getStatus());
        keyData.setEncKey(map(keyDataDto.getEncKey()));
        keyData.setOnline(keyDataDto.isOnline());

        return keyData;
    }

    @Override
    public KeyDataDto toDto(KeyData keyData) {
        if (keyData == null) {
            return null;
        }

        KeyDataDto.KeyDataDtoBuilder keyDataDto = KeyDataDto.builder();

        keyDataDto.id(keyData.getId().toString());
        keyDataDto.alias(keyData.getAlias());
        keyDataDto.description(keyData.getDescription());
        keyDataDto.key(keyData.getKey());
        keyDataDto.expiryDate(keyData.getExpiryDate());
        keyDataDto.algorithm(keyData.getAlgorithm());
        keyDataDto.notifyDate(keyData.getNotifyDate());
        keyDataDto.keyType(keyData.getKeyType());
        keyDataDto.purposeType(keyData.getPurposeType());
        if (keyData.getRelatedKey() != null) {
            keyDataDto.relatedKey(keyData.getRelatedKey().getId().toString());
        }
        keyDataDto.createdDate(keyData.getCreatedDate());
        keyDataDto.status(keyData.getStatus());
        if (keyData.getEncKey() != null) {
            keyDataDto.encKey(keyData.getEncKey().getId().toString());
        }
        keyDataDto.online(keyData.isOnline());

        return keyDataDto.build();
    }
}
