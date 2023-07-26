package ru.gazprombank.token.kms.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.util.mapper.KeyDataMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KeyDataMap {
    @Autowired
    KeyDataMapper keyDataMapper;

    // оперативное хранилище ключей
    private static Map<UUID, KeyDataDto> map = new HashMap<>();

    public KeyDataDto get(UUID id) {
        return map.get(id);
    }

    public void put(UUID id, KeyData key) {
        KeyData newKey = new KeyData(key.getAlias(), key.getAlgorithm(), key.getKeyType(), key.getPurposeType(), key.getStatus());
        newKey.setExpiryDate(key.getExpiryDate());
        newKey.setRelatedKey(key.getRelatedKey());
        KeyDataDto dto = keyDataMapper.toDto(key);
        map.put(id, dto);
    }
}
