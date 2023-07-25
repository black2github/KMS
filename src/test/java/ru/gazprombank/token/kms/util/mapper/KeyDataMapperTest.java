package ru.gazprombank.token.kms.util.mapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Component
class KeyDataMapperTest {

    @Autowired
    KeyDataMapper keyDataMapper;

    @Test
    void toModel() {
        KeyDataDto keyDataDto = new KeyDataDto();
        log.info("DTO=" + keyDataDto);
        KeyData keyData = keyDataMapper.toModel(keyDataDto);
        log.info("Model=" + keyData);
    }

    @Test
    void toDto() {
    }
}