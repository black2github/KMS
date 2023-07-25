package ru.gazprombank.token.kms;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.entity.Token;
import ru.gazprombank.token.kms.repository.KeyDataRepository;
import ru.gazprombank.token.kms.repository.TokenRepository;

import java.util.Random;

import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
class RepositoryTests {
    private static final Logger log = LoggerFactory.getLogger(RepositoryTests.class);

    private static final int RANGE = 100;
    private static Random r = new Random();

    @Autowired
    private KeyDataRepository keyDataRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    void createKeyData() {
        log.info("createKeyData: <-");
        // create
        KeyData key = new KeyData("alias-" + r.nextInt(RANGE), "algorithm-"+r.nextInt(RANGE),
                KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.ENABLED);
        keyDataRepository.save(key);
        log.info("createKeyData: key = " + key);
        assertTrue("Just created key not found", keyDataRepository.existsById(key.getId()));

       // clean
        keyDataRepository.delete(key);
    }

    @Test
    void deleteKeyData() {
        log.info("deleteKeyData: <-");

        // create for test
        KeyData key = new KeyData("alias-" + r.nextInt(RANGE), "algorithm-"+r.nextInt(RANGE),
                KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.DISABLED);
        keyDataRepository.save(key);
        assertTrue("Just created key not found", keyDataRepository.existsById(key.getId()));

        // delete
        keyDataRepository.deleteById(key.getId());
        assertFalse("Found just deleted key", keyDataRepository.existsById(key.getId()));
     }

    @Test
    void createToken() {
        log.info("createToken: <-");

        KeyData key = new KeyData("alias-" + r.nextInt(RANGE), "algorithm-"+r.nextInt(RANGE),
                KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.PENDING_CREATION);
        keyDataRepository.save(key);
        Token token = new Token(key, "secret-" + r.nextInt(RANGE), "PAN");
        tokenRepository.save(token);
        log.info("createToken: token=" + token);
        assertTrue("Just created token not found", tokenRepository.existsById(token.getId()));

        // clean
        tokenRepository.delete(token);
        keyDataRepository.delete(key);
    }

}

