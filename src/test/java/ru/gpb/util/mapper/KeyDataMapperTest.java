package ru.gpb.util.mapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.gpb.kms.entity.KeyData;
import ru.gpb.kms.entity.Dto.KeyDataDto;
import ru.gpb.kms.entity.KeyStatus;
import ru.gpb.kms.entity.KeyType;
import ru.gpb.kms.entity.PurposeType;
import ru.gpb.kms.repository.KeyDataRepository;
import ru.gpb.kms.util.mapper.KeyDataMapper;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class KeyDataMapperTest {

    @Autowired
    KeyDataMapper keyDataMapper;

    @Autowired
    KeyDataRepository keyDataRepository;

    Random r = new Random();

    @Test
    void toModel() {
        KeyDataDto keyDataDto = new KeyDataDto()
                .setId("d4cf5de1-c1cb-42e5-8184-eb34640cfce5")
                .setAlias("alias" + r.nextInt(1000))
                .setDescription("desc" + r.nextInt(1000))
                .setKey("key" + r.nextInt(1000))
                .setEncKey("d4cf5de1-c1cb-42e5-8184-eb34640cfce5")
                .setEncodedKey("12222")
                .setExpiryDate(LocalDateTime.now())
                .setCreatedDate(LocalDateTime.now())
                .setNotifyDate(LocalDateTime.now())
                .setAlgorithm("alg" + r.nextInt(1000))
                .setPurposeType(PurposeType.DEK)
                .setKeyType(KeyType.PRIVATE);
        log.info("DTO=" + keyDataDto);
        KeyData keyData = keyDataMapper.toModel(keyDataDto);
        log.info("Model=" + keyData);

        KeyData key1 = keyDataRepository.findById(UUID.fromString(keyDataDto.getId())).orElse(null);
        assertNotNull(key1);
        key1.setDescription("описание");
        key1.setOnline(true);
        KeyDataDto dto1 = keyDataMapper.toDto(key1);
        KeyData key2 = keyDataMapper.toModel(dto1);
        assertEquals(key1, key2);
        log.info("key1="+key1);
        log.info("key2="+key2);
    }

    @Test
    void toDto() {
        KeyData keyData = new KeyData("alias" + r.nextInt(1000),
                "alg" + r.nextInt(1000), KeyType.PUBLIC, PurposeType.KEK, KeyStatus.PENDING_IMPORT);
        keyDataRepository.save(keyData);
        log.info("key=" + keyData);
        KeyDataDto keyDataDto = keyDataMapper.toDto(keyData);
        log.info("dto=" + keyDataDto);
    }
}