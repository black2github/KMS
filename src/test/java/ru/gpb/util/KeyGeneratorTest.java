package ru.gpb.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ru.gpb.kms.util.KeyGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class KeyGeneratorTest {

    static char[] keyPassword = "keyPassword".toCharArray();
    static char[] storePassword = "storePassword".toCharArray();
    static String storeURI = "file:///C:/work/JavaProjects/GazpromBank/KMS/myKeystore.jks";
    static String password = "Password1" + "Password2";

    @Test
    void symmetricFullTest() {

        try {

            // Генерация симметричного мастер ключа (KEK)
            SecretKey masterKey = KeyGenerator.generateMasterKey(password.toCharArray());
            log.info("Shared Master Key: ==> " + Base64.getEncoder().encodeToString(masterKey.getEncoded()));

            // Генерация ключа данных (DEK)
            SecretKey dataKey = KeyGenerator.generateDataKey();

            // Шифрование ключа данных (DEK) с использованием симметричного мастер ключа (KEK)
            byte[] encryptedDataKey = KeyGenerator.encryptDataKey(dataKey, masterKey);
            // Расшифровка ключа данных (DEK) с использованием мастер ключа (KEK)
            SecretKey decryptedDataKey = KeyGenerator.decryptDataKey(encryptedDataKey, masterKey);
            // Вывод оригинального ключа данных (DEK) и расшифрованного ключа данных (DEK)
            log.info("Original Data Key (SYM):  " + Base64.getEncoder().encodeToString(dataKey.getEncoded()));
            log.info("Decrypted Data Key (SYM): " + Base64.getEncoder().encodeToString(decryptedDataKey.getEncoded()));

            // Шифрование данных с использованием ключа данных (DEK)
            String originalData = "Hello, Nikita!";
            log.info("Original Data: " + originalData);
            String encryptedData = KeyGenerator.encryptData(originalData, dataKey);
            log.info("Encrypted Data: " + encryptedData);

            // Расшифровка данных с использованием ключа данных (DEK)
            String decryptedData = KeyGenerator.decryptData(encryptedData, dataKey);
            log.info("Decrypted Data: " + decryptedData);

            // Сравнение исходных данных и расшифрованных данных
            assertEquals(originalData, decryptedData);


        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException |
                 IOException | InvalidKeySpecException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void asymmetricFullTest() {

        try {
            URI uri = new URI(storeURI);

            // Генерация ключа данных (DEK)
            SecretKey dataKey = KeyGenerator.generateDataKey();

            // Чтение секретного и публичного ключа из jks.store файла
            PrivateKey loadedPrivateKey = KeyGenerator.loadPrivateKey(KeyGenerator.getFileNameFromURI(uri), "myKey", storePassword, keyPassword);
            PublicKey loadedPublicKey = KeyGenerator.loadPublicKey(KeyGenerator.getFileNameFromURI(uri), "myKey", storePassword);

            // Шифрование ключа данных (DEK) с использованием публичной части мастер ключа (KEK)
            byte[] encryptedDataKey = KeyGenerator.encryptDataKey(dataKey, loadedPublicKey);
            // Расшифровка ключа данных (DEK) с использованием приватной части мастер ключа (KEK)
            SecretKey decryptedDataKey = KeyGenerator.decryptDataKey(encryptedDataKey, loadedPrivateKey);
            // Вывод оригинального ключа данных (DEK) и расшифрованного ключа данных (DEK)
            log.info("Original Data Key (ASY):  " + Base64.getEncoder().encodeToString(dataKey.getEncoded()));
            log.info("Decrypted Data Key (ASY): " + Base64.getEncoder().encodeToString(decryptedDataKey.getEncoded()));

            // Шифрование данных с использованием ключа данных (DEK)
            String originalData = "Hello, Nikita!";
            log.info("Original Data: " + originalData);
            String encryptedData = KeyGenerator.encryptData(originalData, dataKey);
            log.info("Encrypted Data: " + encryptedData);

            // Расшифровка данных с использованием ключа данных (DEK)
            String decryptedData = KeyGenerator.decryptData(encryptedData, dataKey);
            log.info("Decrypted Data: " + decryptedData);

            // Сравнение исходных данных и расшифрованных данных
            assertEquals(originalData, decryptedData);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException |
                 IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void isValidURI() {
        String file = "file:///C:/work/JavaProjects/GazpromBank/KMS/masterKeystore.jks";
        assertTrue(KeyGenerator.isValidURI(file));
    }
}