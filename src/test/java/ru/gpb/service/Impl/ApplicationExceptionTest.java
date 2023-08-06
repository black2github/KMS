package ru.gpb.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.gpb.kms.util.exceptions.KeyNotFoundApplicationException;

@Slf4j
@SpringBootTest
class ApplicationExceptionTest {

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