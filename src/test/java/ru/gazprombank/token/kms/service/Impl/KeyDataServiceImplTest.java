package ru.gazprombank.token.kms.service.Impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.repository.KeyDataRepository;
import ru.gazprombank.token.kms.repository.TokenRepository;
import ru.gazprombank.token.kms.service.KeyDataService;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class KeyDataServiceImplTest {
    private static final Logger log = LoggerFactory.getLogger(KeyDataServiceImplTest.class);

    private static final int RANGE = 100;
    private static Random r = new Random();

    @Autowired
    private KeyDataService keyDataService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void listAll() {
        keyDataService.listAll();
    }

    @Test
    void updateKeyData() {
    }

    @Test
    void saveKeyData() {
    }

    @Test
    void delete() {
        // given

        // when

        // then
    }

    @Test
    void generateMasterKey() {
        for (int i = 0; i < 4; i++) {
            // given
            // when
            String alias = "alias" + r.nextInt(1000);
            KeyData keyData = keyDataService.generateMasterKey(null, alias, null, null,
                    "passwd1".toCharArray(), null);
            keyData = keyDataService.generateMasterKey(keyData.getId(), alias, keyData.getDescription(),
                    keyData.getExpiryDate(), "passwd2".toCharArray(), keyData.getNotifyDate());
            // then
        }
    }
}