package ru.gpb.kms.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * RSA encryption example util class.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since 31.07.2023
 */
public class RSAEncryptionExample {

    private static final String MASTER_KEY_ALGORITHM = "RSA";
    private static final String MASTER_KEY_ALGORITHM_TYPE = "RSA";
    private static final String ENCRYPTED_KEY_ALGORITHM = "AES";
    private static final String DATA_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static void main(String[] args) {
        try {
            // Генерация ключевой пары RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Сообщение, которое нужно зашифровать
            String message = "Hello, RSA Encryption!";

            // Шифрование данных с использованием открытого ключа (зашифрованный текст - cipherText)
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherText = cipher.doFinal(message.getBytes());

            // Расшифровка данных с использованием закрытого ключа (расшифрованный текст - plainText)
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plainText = cipher.doFinal(cipherText);

            // Вывод зашифрованного и расшифрованного текста
            System.out.println("Зашифрованный текст: " + new String(cipherText));
            System.out.println("Расшифрованный текст: " + new String(plainText));

            // Генерация ключа данных (DEK)
            SecretKey dataKey = generateDataKey();

            // Шифрование ключа данных (DEK) с использованием мастер ключа (KEK)
            byte[] encryptedDataKey = encryptDataKey(dataKey, publicKey);

            // Расшифровка ключа данных (DEK) с использованием мастер ключа (KEK)
            SecretKey decryptedDataKey = decryptDataKey(encryptedDataKey, privateKey);

            // Вывод оригинального ключа данных (DEK) и расшифрованного ключа данных (DEK)
            System.out.println("Original Data Key:  " + Base64.getEncoder().encodeToString(dataKey.getEncoded()));
            System.out.println("Decrypted Data Key: " + Base64.getEncoder().encodeToString(decryptedDataKey.getEncoded()));

            // Шифрование данных с использованием ключа данных (DEK)
            String originalData = "Hello, Nikita!";
            System.out.println("Original Data: " + originalData);
            String encryptedData = encryptData(originalData, dataKey);
            System.out.println("Encrypted Data: " + encryptedData);

            // Расшифровка данных с использованием ключа данных (DEK)
            String decryptedData = decryptData(encryptedData, dataKey);
            System.out.println("Decrypted Data: " + decryptedData);

            // Сравнение исходных данных и расшифрованных данных
            boolean isMatch = originalData.equals(decryptedData);
            System.out.println("Data Match: " + isMatch);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SecretKey generateDataKey() throws NoSuchAlgorithmException {
        javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance(ENCRYPTED_KEY_ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private static byte[] encryptDataKey(SecretKey dataKey, PublicKey masterKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(MASTER_KEY_ALGORITHM_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        return cipher.doFinal(dataKey.getEncoded());
    }

    private static SecretKey decryptDataKey(byte[] encryptedDataKey, PrivateKey masterKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(MASTER_KEY_ALGORITHM_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedDataKeyBytes = cipher.doFinal(encryptedDataKey);
        return new SecretKeySpec(decryptedDataKeyBytes, ENCRYPTED_KEY_ALGORITHM);
    }

    private static String encryptData(String data, SecretKey dataKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);

        // Генерация нового вектора инициализации
        byte[] iv = generateInitializationVector(cipher.getBlockSize());

        // Переключение на алгоритм шифрации ключей
        cipher = Cipher.getInstance(ENCRYPTED_KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, dataKey);
        // Шифрование вектора инициализации
        byte[] eiv = cipher.doFinal(iv);

        // Переключение на алгоритм шифрации данных
        cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);
        // Инициализация вектором инициализации
        cipher.init(Cipher.ENCRYPT_MODE, dataKey, new IvParameterSpec(iv));
        // Шифрование данных
        byte[] eData = cipher.doFinal(data.getBytes());

        // Кодирование зашифрованных данных и зашифрованного вектора инициализации в строку
        String encodedIV = Base64.getEncoder().encodeToString(eiv);
        String encodedData = Base64.getEncoder().encodeToString(eData);

        return encodedIV + ":" + encodedData;
    }

    private static String decryptData(String ciphertext, SecretKey dataKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        // Разделение строки на вектор инициализации и зашифрованные данные
        String[] parts = ciphertext.split(":");
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[1]);

        // Переключение на шифр ключей
        Cipher cipher = Cipher.getInstance(ENCRYPTED_KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, dataKey);
        // Расшифровка вектора инициализации
        byte[] div = cipher.doFinal(iv);

        // Переключение на шифр для данных
        cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);
        // Инициализая шифра для данных
        cipher.init(Cipher.DECRYPT_MODE, dataKey, new IvParameterSpec(div));
        // Расшифровка данных
        byte[] decryptedDataBytes = cipher.doFinal(encryptedData);

        return new String(decryptedDataBytes, StandardCharsets.UTF_8);
    }

    private static byte[] generateInitializationVector(int size) {
        byte[] iv = new byte[size];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

}

