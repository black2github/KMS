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
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;


@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeyDataServiceImplTest {

    private static final int RANGE = 100;
    private static final Random r = new Random();

    @Autowired
    private KeyDataService keyDataService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private KeyDataRepository keyDataRepository;

    private static KeyDataDto masterKeyData = null;

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
        log.info("generateMasterKey2: <- masterKeyData=" + masterKeyData);

        // given  (подготовка тестовых данных)
        UUID id = UUID.fromString(masterKeyData.getId());

        // when (вызов операций, которые мы тестируем)
        masterKeyData = keyDataService.generateMasterKey(id, masterKeyData.getAlias(), masterKeyData.getDescription(),
                masterKeyData.getExpiryDate(), "passwd2".toCharArray(), masterKeyData.getNotifyDate());
        // then (блок с ассертами)

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
        UUID id = UUID.fromString(masterKeyData.getId());

        // when
        try {
            masterKeyData = keyDataService.generateMasterKey(id, alias, masterKeyData.getDescription(),
                    masterKeyData.getExpiryDate(), "passwd2".toCharArray(), masterKeyData.getNotifyDate());
            failBecauseExceptionWasNotThrown(InvalidPasswordApplicationException.class);
        } catch (Exception ex) {
            // then should fail
        }
        // clean
        keyDataRepository.deleteById(id);
    }

    @Test
    @Order(4)
    @WithUserDetails(value = "master1")
    void loadMasterKey1() {
        // given
        log.info("loadMasterKey1: <- .");
        masterKeyData = null;
        List<KeyDataDto> ks = keyDataService.listAll();
        for (KeyDataDto k : ks) {
            log.info("loadMasterKey1: = " + k);
        }
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
        log.info("loadMasterKey2: <- masterKeyData=" + masterKeyData);

        // given
        UUID id = UUID.fromString(masterKeyData.getId());

        // when
        masterKeyData = keyDataService.loadMasterKey(id, "passwd2".toCharArray());

        // then
        assertNotNull(masterKeyData);
        assertSame(masterKeyData.getStatus(), KeyStatus.ENABLED, "Статус должен быть 'Доступен'");

        log.info("loadMasterKey2: -> masterKeyData=" + masterKeyData);
    }

    @Test
    @Order(6)
    @WithUserDetails(value = "admin")
    void generateDataKey() {
        KeyDataDto k = keyDataService.generateDataKey("alias" + r.nextInt(1000));
        assertNotNull(k);

        // clean
        keyDataRepository.deleteById(UUID.fromString(k.getId()));
    }

    @Test
    @Order(7)
    void listAll() {
        List<KeyDataDto> keys = keyDataService.listAll();
        for (KeyDataDto key : keys) {
            log.info("->: " + key);
        }
    }

    @Test
    @Order(8)
    @WithUserDetails(value = "admin")
    void changeKeyStatus() {
        // given

        // when
        KeyDataDto k = keyDataService.generateDataKey("alias" + r.nextInt(1000));
        assertNotNull(k);

        // then
        try {
            keyDataService.changeStatus(k.getId(), KeyStatus.ENABLED);
            keyDataService.changeStatus(k.getId(), KeyStatus.DISABLED);
            keyDataService.changeStatus(k.getId(), KeyStatus.PENDING_DELETION);
            keyDataService.changeStatus(k.getId(), KeyStatus.DISABLED);
            keyDataService.changeStatus(k.getId(), KeyStatus.UNAVAILABLE);
            keyDataService.changeStatus(k.getId(), KeyStatus.PENDING_IMPORT);
            keyDataService.changeStatus(k.getId(), KeyStatus.UNAVAILABLE);
            keyDataService.changeStatus(k.getId(), KeyStatus.ENABLED);
            keyDataService.changeStatus(k.getId(), KeyStatus.PENDING_IMPORT);
            keyDataService.changeStatus(k.getId(), KeyStatus.ENABLED);
            keyDataService.changeStatus(k.getId(), KeyStatus.PENDING_DELETION);
        } catch (Exception ex) {
            fail(ex);
        }
        KeyData k2 = keyDataRepository.findById(UUID.fromString(k.getId())).orElse(null);
        assertNotNull(k2);
        assertSame(k2.getStatus(), KeyStatus.PENDING_DELETION);

        // clean
        keyDataRepository.deleteById(UUID.fromString(k.getId()));
    }

    @Test
    @Order(9)
    @WithUserDetails(value = "admin")
    void createManyDataKey() {
        KeyDataDto[] keys = new KeyDataDto[3];
        try {
            for (int i = 0; i < keys.length; i++) {
                keys[i] = keyDataService.generateDataKey("alias" + r.nextInt(1000));
                assertNotNull(keys[i]);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // clean
        for (int i = 0; i < keys.length; i++) {
            keyDataService.delete(keys[i].getId());
        }
    }

    @Test
    @Order(10)
    @WithUserDetails(value = "admin")
    void decodeDataKey() {
        // given
        KeyDataDto k = keyDataService.generateDataKey("alias" + r.nextInt(1000));
        assertNotNull(k);
        log.info("decodeDataKey: key=" + k);

        // when
        SecretKey s = keyDataService.decodeDataKey(UUID.fromString(k.getId()));

        // then
        assertNotNull(s);

        // clean
    }

    @Test
    @Order(11)
    @WithUserDetails(value = "user")
    void secret2Token2Secret() {
        // given
        String pan = "1111222233334444";

        // when
        String token = tokenService.secret2Token(pan, TokenType.PAN, null);
        assertNotNull(token);
        log.info("secret2Token2Secret: token = '" + token + "'");
        String secret = tokenService.token2Secret(token);

        // then
        assertEquals(pan, secret);
    }

    @Test
    @Order(12)
    @WithUserDetails(value = "user")
    void shouldReturnTheSameToken() {
        // given
        Random r = new Random();
        String pan = "111122223333" + String.format("%04d", r.nextInt(10000));
        log.info("shouldReturnTheSameToken: token <- '" + pan + "'");
        // when
        String token = tokenService.secret2Token(pan, TokenType.PAN, null);
        assertNotNull(token);
        log.info("shouldReturnTheSameToken: token = '" + token + "'");
        String token2 = tokenService.secret2Token(pan, TokenType.PAN, null);

        // then
        assertEquals(token, token2);
    }
}