package ru.gazprombank.token.kms.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.entity.TokenType;
import ru.gazprombank.token.kms.repository.KeyDataRepository;
import ru.gazprombank.token.kms.service.KeyDataService;
import ru.gazprombank.token.kms.service.TokenService;
import ru.gazprombank.token.kms.util.exceptions.InvalidPasswordApplicationException;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Последовательность выполнения шагов в данном сценарии важна (@TestMethodOrder), так как для проверки
 * необходимо наличие ключей в оперативном доступе (и того KEK, которым был зашифрован DEK
 * и того KEK, которыми предполагается шифровать).
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeyDataRotateTest {

    @Autowired
    private KeyDataService keyDataService;

    @Autowired
    private KeyDataRepository keyDataRepository;

    private static KeyDataDto masterKeyData1 = null;
    private static KeyDataDto masterKeyData2 = null;
    private static KeyDataDto dataKey = null;
    private static final int RANGE = 100;
    private static final Random r = new Random();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Order(1)
    @WithUserDetails(value = "master1")
    void generateMasterKey_Phase1() {
        // given
        log.info("generateMasterKey1: <- masterKeyData=" + masterKeyData1);

        // when
        String alias = "alias" + r.nextInt(1000);
        masterKeyData1 = keyDataService.generateMasterKey(null, alias, null, null,
                "passwd1".toCharArray(), null);
        // then
        assertNotNull(masterKeyData1);
        assertTrue(masterKeyData1.getStatus()==KeyStatus.PENDING_CREATION);
        log.info("generateMasterKey1: -> masterKeyData=" + masterKeyData1);
    }

    @Test
    @Order(2)
    @WithUserDetails(value = "master2")
    void generateMasterKey_Phase2() {
        log.info("generateMasterKey2: <- masterKeyData=" + masterKeyData1);

        // given  (подготовка тестовых данных)
        UUID id = UUID.fromString(masterKeyData1.getId());

        // when (вызов операций, которые мы тестируем)
        masterKeyData1 = keyDataService.generateMasterKey(id, masterKeyData1.getAlias(), masterKeyData1.getDescription(),
                masterKeyData1.getExpiryDate(), "passwd2".toCharArray(), masterKeyData1.getNotifyDate());

        // then (блок с ассертами)
        assertNotNull(masterKeyData1);
        // assertTrue(masterKeyData1.getStatus()==KeyStatus.ENABLED);
        log.info("generateMasterKey2: -> masterKeyData=" + masterKeyData1);
    }

    @Test
    @Order(6)
    @WithUserDetails(value = "admin")
    void generateDataKey() {
        // given
        dataKey = keyDataService.generateDataKey("alias" + r.nextInt(1000));
        assertNotNull(dataKey);
        // assertTrue(dataKey.getEncKey().equals(masterKeyData1.getId()));

        // clean
        // keyDataRepository.deleteById(UUID.fromString(k.getId()));
    }

    @Test
    @Order(7)
    @WithUserDetails(value = "admin")
    void generateDataKey_AndChangeMaster2Delete_Pending() {
        // генерация ключа данных
        KeyDataDto k = keyDataService.generateDataKey("alias" + r.nextInt(1000));
        assertNotNull(k);

        // удаление мастера
        keyDataService.changeStatus(masterKeyData1.getId(), KeyStatus.PENDING_DELETION);

        // clean
    }

    @Test
    @Order(8)
    @WithUserDetails(value = "master1")
    void generateOtherMasterKey_Phase1() {
        // given
        log.info("generateMasterKey1: <- masterKeyData=" + masterKeyData2);

        // when
        String alias = "alias" + r.nextInt(1000);
        masterKeyData2 = keyDataService.generateMasterKey(null, alias, null, null,
                "passwd1".toCharArray(), null);

        // then
        assertNotNull(masterKeyData2);
        // assertTrue(masterKeyData2.getStatus()==KeyStatus.PENDING_CREATION);
        log.info("generateMasterKey1: -> masterKeyData=" + masterKeyData2);
    }

    @Test
    @Order(9)
    @WithUserDetails(value = "master2")
    void generateOtherMasterKey_Phase2() {
        log.info("generateMasterKey2: <- masterKeyData=" + masterKeyData2);

        // given  (подготовка тестовых данных)
        UUID id = UUID.fromString(masterKeyData2.getId());

        // when (вызов операций, которые мы тестируем)
        masterKeyData2 = keyDataService.generateMasterKey(id, masterKeyData2.getAlias(), masterKeyData2.getDescription(),
                masterKeyData2.getExpiryDate(), "passwd2".toCharArray(), masterKeyData2.getNotifyDate());

        // then (блок с ассертами)
        assertNotNull(masterKeyData2);
        // assertTrue(masterKeyData2.getStatus()==KeyStatus.ENABLED);
        log.info("generateMasterKey2: -> masterKeyData=" + masterKeyData2);
    }

    @Test
    @Order(10)
    @WithUserDetails(value = "master2")
    void rotate() {
        log.info("rotate: <- ");

        // given  (подготовка тестовых данных)

        // when (вызов операций, которые мы тестируем)
        keyDataService.rotateDataKey();
        // KeyData k = keyDataRepository.findBy(dataKey.getId());
        // then (блок с ассертами)
        // assertTrue(masterKeyData1.getKey() != );
        log.info("rotate: ->");
    }

    @Test
    @Order(11)
    void listAll() {
        List<KeyDataDto> keys = keyDataService.listAll();
        for (KeyDataDto key : keys) {
            log.info("->: " + key);
        }
    }

}