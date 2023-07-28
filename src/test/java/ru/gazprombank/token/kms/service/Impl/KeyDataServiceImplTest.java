package ru.gazprombank.token.kms.service.Impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.repository.KeyDataRepository;
import ru.gazprombank.token.kms.repository.TokenRepository;
import ru.gazprombank.token.kms.service.InvalidPasswordApplicationException;
import ru.gazprombank.token.kms.service.KeyDataService;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest
class KeyDataServiceImplTest {
    private static final Logger log = LoggerFactory.getLogger(KeyDataServiceImplTest.class);

    private static final int RANGE = 100;
    private static final Random r = new Random();

    @Autowired
    private KeyDataService keyDataService;

    @Autowired
    private KeyDataRepository keyDataRepository;

    private static KeyData masterKeyData = null;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
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
    @Order(1)
    @WithUserDetails(value = "master1")
    void generateMasterKey1() {
        // given
        log.info("generateMasterKey1: <- masterKeyData=" + masterKeyData);

        // when
        String alias = "alias" + r.nextInt(1000);
        masterKeyData = keyDataService.generateMasterKey(null, alias, null, null,
                "passwd1".toCharArray(), null);
        // then
        log.info("generateMasterKey1: -> masterKeyData=" + masterKeyData);
    }

    @Test
    @Order(2)
    @WithUserDetails(value = "master2")
    void generateMasterKey2() {
        // given
        log.info("generateMasterKey2: <- masterKeyData=" + masterKeyData);

        // when
        masterKeyData = keyDataService.generateMasterKey(masterKeyData.getId(), masterKeyData.getAlias(), masterKeyData.getDescription(),
                masterKeyData.getExpiryDate(), "passwd2".toCharArray(), masterKeyData.getNotifyDate());
        // then
        log.info("generateMasterKey2: -> masterKeyData=" + masterKeyData);
    }

    @Test
    @Order(3)
    @WithUserDetails(value = "master1")
    void generateMasterKeyBySingleUserShouldFail() {
        // given
        log.info("generateMasterKeyBySingleUserShouldFail: <-.");
        String alias = "alias" + r.nextInt(1000);
        masterKeyData = keyDataService.generateMasterKey(null, alias, null, null,
                "passwd1".toCharArray(), null);

        // when
        try {
            masterKeyData = keyDataService.generateMasterKey(masterKeyData.getId(), alias, masterKeyData.getDescription(),
                    masterKeyData.getExpiryDate(), "passwd2".toCharArray(), masterKeyData.getNotifyDate());
            failBecauseExceptionWasNotThrown(InvalidPasswordApplicationException.class);
        } catch (Exception ex) {
            // then should fail

            // clean
            keyDataRepository.deleteById(masterKeyData.getId());
        }
    }

    @Test
    @Order(4)
    @WithUserDetails(value = "master1")
    void loadMasterKey1() {
        // given
        log.info("loadMasterKey1: <- .");
        masterKeyData = null;
        List<KeyData> keys = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(KeyType.PRIVATE, PurposeType.KEK, KeyStatus.ENABLED);
        if (keys.isEmpty()) {
            fail("Не найдено ни одного мастер-ключа подходящего для загрузки");
        }
        // when
        // берем первый попавшийся ключ
        masterKeyData = keyDataService.loadMasterKey(keys.get(0).getId(), "passwd1".toCharArray());

        // then
        assertNotNull(masterKeyData);
        assertSame(masterKeyData.getStatus(), KeyStatus.PENDING_IMPORT, "Статус должен быть 'Ожидает импорта'");
        log.info("loadMasterKey1: -> masterKeyData=" + masterKeyData);
    }

    @Test
    @Order(5)
    @WithUserDetails(value = "master2")
    void loadMasterKey2() {
        // given
        log.info("loadMasterKey2: <- masterKeyData=" + masterKeyData);

        // when
        masterKeyData = keyDataService.loadMasterKey(masterKeyData.getId(), "passwd2".toCharArray());

        // then
        assertNotNull(masterKeyData);
        assertSame(masterKeyData.getStatus(), KeyStatus.ENABLED, "Статус должен быть 'Доступен'");
        log.info("loadMasterKey2: -> masterKeyData=" + masterKeyData);
    }

    @Test
    @Order(6)
    void createDataKey() {
        KeyData k = keyDataService.createDataKey("alias" + r.nextInt(1000));
        assertNotNull(k);
    }

    @Test
    @Order(7)
    void listAll() {
        List<KeyData> keys = keyDataService.listAll();
        for (KeyData key: keys) {
            log.info("->: " + key);
        }
    }
}