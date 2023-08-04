package ru.gpb.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.gpb.kms.repository.KeyDataRepository;
import ru.gpb.kms.service.KeyDataService;
import ru.gpb.kms.util.exceptions.KeyNotFoundApplicationException;
import ru.gpb.token.service.TokenService;

import java.util.Random;

@Slf4j
@SpringBootTest
class ApplicationExceptionTest {

    private static final int RANGE = 100;
    private static final Random r = new Random();

    @Autowired
    private KeyDataService keyDataService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private KeyDataRepository keyDataRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void checkException() {
        try {
            throw new KeyNotFoundApplicationException("123");
        } catch (Exception ex) {
            log.info("ckechExcption: " + ex.getMessage());
        }
    }
}