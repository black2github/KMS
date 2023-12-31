package ru.gpb.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import ru.gpb.kms.entity.KeyData;
import ru.gpb.kms.entity.Dto.KeyDataDto;
import ru.gpb.kms.entity.KeyStatus;
import ru.gpb.kms.entity.KeyType;
import ru.gpb.kms.entity.PurposeType;
import ru.gpb.token.entity.Dto.TokenRequest;
import ru.gpb.token.entity.Dto.TokenResponse;
import ru.gpb.token.entity.TokenType;
import ru.gpb.kms.repository.KeyDataRepository;
import ru.gpb.kms.service.KeyDataService;
import ru.gpb.token.service.TokenService;
import ru.gpb.kms.util.exceptions.InvalidPasswordApplicationException;
import ru.gpb.kms.util.exceptions.SecurityApplicationException;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Последовательность выполнения шагов в данном сценарии важна (@TestMethodOrder), так как для проверки
 * необходимо наличие ключей в оперативном доступе.
 */
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
    void generateMasterKey_Phase1() {
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
    void generateMasterKey_Phase2() {
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
        KeyDataDto masterKeyData2 = keyDataService.generateMasterKey(null, alias, null, null,
                "passwd1".toCharArray(), null);
        UUID id = UUID.fromString(masterKeyData2.getId());

        // when
        try {
            masterKeyData2 = keyDataService.generateMasterKey(id, alias, masterKeyData2.getDescription(),
                    masterKeyData2.getExpiryDate(), "passwd2".toCharArray(), masterKeyData2.getNotifyDate());
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
    void loadMasterKey_Phase1() {
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
        Assertions.assertSame(masterKeyData.getStatus(), KeyStatus.PENDING_IMPORT, "Статус должен быть 'Ожидает импорта'");
        log.info("loadMasterKey1: -> masterKeyData=" + masterKeyData);
    }

    @Test
    @Order(5)
    @WithUserDetails(value = "master2")
    void loadMasterKey_Phase2() {
        log.info("loadMasterKey2: <- masterKeyData=" + masterKeyData);

        // given
        UUID id = UUID.fromString(masterKeyData.getId());

        // when
        masterKeyData = keyDataService.loadMasterKey(id, "passwd2".toCharArray());

        // then
        assertNotNull(masterKeyData);
        Assertions.assertSame(masterKeyData.getStatus(), KeyStatus.ENABLED, "Статус должен быть 'Доступен'");

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
    @Order(11)
    void listAll() {
        List<KeyDataDto> keys = keyDataService.listAll();
        for (KeyDataDto key : keys) {
            log.info("->: " + key);
        }
    }

    @Test
    @Order(12)
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
    @Order(13)
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
        for (KeyDataDto key : keys) {
            keyDataService.delete(key.getId());
        }
    }

    @Test
    @Order(14)
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
    @Order(15)
    @WithUserDetails(value = "user")
    void secret2Token2Secret() {
        // given
        String pan = "1111222233334444";
        TokenRequest req = new TokenRequest(pan, TokenType.PAN, 1L, "Eco", null, "123");


        // when
        String token = tokenService.secret2Token(req);
        assertNotNull(token);
        log.info("secret2Token2Secret: token = '" + token + "'");
        TokenResponse resp = tokenService.token2Secret(token);
        String secret = resp.getSecret();

        // then
        assertEquals(pan, secret);
    }

    @Test
    @Order(16)
    @WithUserDetails(value = "user")
    void shouldReturnTheSameToken() {
        // given
        Random r = new Random();
        String pan = "111122223333" + String.format("%04d", r.nextInt(10000));
        TokenRequest req = new TokenRequest(pan, TokenType.PAN, 1L, "Eco", null, "123");

        log.info("shouldReturnTheSameToken: token <- '" + pan + "'");
        // when
        String token = tokenService.secret2Token(req);
        assertNotNull(token);
        log.info("shouldReturnTheSameToken: token = '" + token + "'");
        String token2 = tokenService.secret2Token(req);

        // then
        assertEquals(token, token2);
    }

    @Test
    @Order(17)
    @WithUserDetails(value = "user")
    void shouldNotReturnExpiredDataFromToken() {
        // given
        Random r = new Random();
        String pan = "111122223333" + String.format("%04d", r.nextInt(10000));
        log.info("shouldNotReturnExpiredDataFromToken: token <- '" + pan + "'");
        // when
        TokenRequest req = new TokenRequest(pan, TokenType.PAN, 1L, "Eco", null, "123");
        String token = tokenService.secret2Token(req); // ставим протухание на 1 секунду

        // then
        try {
            Thread.sleep(1500L); // спим 1,5 секунды
            tokenService.token2Secret(token);
            fail("Возвращены данные из протухшего токена");
        } catch (SecurityApplicationException ex) {
            log.info("shouldNotReturnExpiredDataFromToken: " + ex.get());
            assertTrue(ex.getMessage().contains("истек"));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

}