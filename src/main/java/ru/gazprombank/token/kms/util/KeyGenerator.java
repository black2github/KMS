package ru.gazprombank.token.kms.util;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.gazprombank.token.kms.service.KeyNotFoundApplicationException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

// рабочий пример, бери и вставляй кусками в KMS

// @Component
@UtilityClass
public class KeyGenerator {
    public static final String SYMMETRIC_MASTER_KEY_ALGORITHM = "PBKDF2WithHmacSHA512";
    public static final String SYMMETRIC_MASTER_KEY_ALGORITHM_TYPE = "AES";

    public static final String ASYMMETRIC_MASTER_KEY_ALGORITHM = "SHA256WithRSA";
    public static final String ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE = "RSA";

    public static final String KEY_ENCRYPTION_ALGORITHM = "AES";
    public static final String DATA_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static void main(String[] args) throws InvalidKeySpecException {

        // Данные сертификата и секретного ключа в jks.store файле
        char[] storePassword = "storePassword".toCharArray();
        char[] keyPassword = "keyPassword".toCharArray();
        char[] password = ("Password1" + "Password2").toCharArray();
        String storeURI = "file:///C:/work/JavaProjects/GazpromBank/KMS/myKeystore.jks";

        try {

            URI uri = new URI(storeURI);

            // Генерация ключа данных (DEK)
            SecretKey dataKey = generateDataKey();

            for (int i = 0; i < 1; i++) {
                // Генерация симметричного мастер ключа (KEK)
                SecretKey masterKey = generateMasterKey(password);
                System.out.println("Shared Master Key: ==> " + Base64.getEncoder().encodeToString(masterKey.getEncoded()));

                // Шифрование ключа данных (DEK) с использованием симметричного мастер ключа (KEK)
                byte[] encryptedDataKey = encryptDataKey(dataKey, masterKey);
                // Расшифровка ключа данных (DEK) с использованием мастер ключа (KEK)
                SecretKey decryptedDataKey = decryptDataKey(encryptedDataKey, masterKey);
                // Вывод оригинального ключа данных (DEK) и расшифрованного ключа данных (DEK)
                System.out.println("Original Data Key (SYM):  " + Base64.getEncoder().encodeToString(dataKey.getEncoded()));
                System.out.println("Decrypted Data Key (SYM): " + Base64.getEncoder().encodeToString(decryptedDataKey.getEncoded()));

                // Чтение секретного и публичного ключа из jks.store файла
                PrivateKey loadedPrivateKey = loadPrivateKey(getFileNameFromURI(uri), "myKey", storePassword, keyPassword);
                PublicKey loadedPublicKey = loadPublicKey(getFileNameFromURI(uri), "myKey", storePassword);

                // Шифрование ключа данных (DEK) с использованием публичной части мастер ключа (KEK)
                encryptedDataKey = encryptDataKey(dataKey, loadedPublicKey);
                // Расшифровка ключа данных (DEK) с использованием приватной части мастер ключа (KEK)
                decryptedDataKey = decryptDataKey(encryptedDataKey, loadedPrivateKey);
                // Вывод оригинального ключа данных (DEK) и расшифрованного ключа данных (DEK)
                System.out.println("Original Data Key (ASY):  " + Base64.getEncoder().encodeToString(dataKey.getEncoded()));
                System.out.println("Decrypted Data Key (ASY): " + Base64.getEncoder().encodeToString(decryptedDataKey.getEncoded()));

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
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException |
                 KeyStoreException | UnrecoverableKeyException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Если прижало и нет пароля. Не безопасно!
    private static SecretKey generateMasterKey() throws NoSuchAlgorithmException {
        javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance(SYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    /**
     * Генерация симметричного ключа шифрования ключей (KEK) на основе пароля
     *
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey generateMasterKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Определение параметров алгоритма PBKDF2
        int iterations = 10000;
        int keyLength = 256;
        byte[] iv = generateInitializationVector(16);

        // Генерация ключа шифрования ключей (KEK) на основе пароля
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SYMMETRIC_MASTER_KEY_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(password, iv, iterations, keyLength);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), SYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
    }

    /**
     * Генерация ключевой пары (секретный/публичный ключи) ключа шифрования ключей (KEK)
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateMasterKeyPair() throws NoSuchAlgorithmException {
        // Генерация ключевой пары, например, RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    /**
     * Загрузка секретного ключа из хранилищца ключей и сертификатов.
     *
     * @param store
     * @param alias
     * @param storePassword
     * @param keyPassword
     * @return
     * @throws IOException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    public static PrivateKey loadPrivateKey(String store, String alias, char[] storePassword, char[] keyPassword)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(store)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fis, storePassword);
            return (PrivateKey) keyStore.getKey(alias, keyPassword);
        } catch (IOException | CertificateException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Can't load key:" + ex.getMessage());
        }
    }

    /**
     * Загрузка публичного ключа из хранилищца ключей и сертификатов.
     *
     * @param store
     * @param alias
     * @param storePassword
     * @param storePassword
     * @return
     * @throws IOException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    public static PublicKey loadPublicKey(String store, String alias, char[] storePassword)
            throws IOException, KeyStoreException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(store)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fis, storePassword);
            Certificate cert = keyStore.getCertificate(alias);
            if (cert!=null) {
                return cert.getPublicKey();
            } else {
                throw new KeyNotFoundApplicationException(
                        String.format("Публичный ключ недоступен: Сертификат с алиасом '%s' в '%s' не найден.", alias, store));
            }

        } catch (IOException | CertificateException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Can't load key:" + ex.getMessage());
        }
    }

    /**
     * Генерация ключа шифрования данных
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateDataKey() throws NoSuchAlgorithmException {
        javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance(KEY_ENCRYPTION_ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    /**
     * Шифрование ключа данных симметричным ключем
     *
     * @param dataKey
     * @param masterKey
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] encryptDataKey(SecretKey dataKey, SecretKey masterKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(SYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        return cipher.doFinal(dataKey.getEncoded());
    }

    /**
     * Шифрование ключа данных публичным (ассиметричным) ключем
     *
     * @param dataKey
     * @param masterKey
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] encryptDataKey(SecretKey dataKey, PublicKey masterKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        return cipher.doFinal(dataKey.getEncoded());
    }

    /**
     * Расшифровка ключа данных симметричным ключем
     *
     * @param encryptedDataKey
     * @param masterKey
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static SecretKey decryptDataKey(byte[] encryptedDataKey, SecretKey masterKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(SYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedDataKeyBytes = cipher.doFinal(encryptedDataKey);
        return new SecretKeySpec(decryptedDataKeyBytes, KEY_ENCRYPTION_ALGORITHM);
    }

    /**
     * Расшифровка ключа данных секретным (ассимметричным) ключем
     *
     * @param encryptedDataKey
     * @param masterKey
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static SecretKey decryptDataKey(byte[] encryptedDataKey, PrivateKey masterKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedDataKeyBytes = cipher.doFinal(encryptedDataKey);
        return new SecretKeySpec(decryptedDataKeyBytes, KEY_ENCRYPTION_ALGORITHM);
    }

    /**
     * Шифрование данных
     *
     * @param data
     * @param dataKey
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String encryptData(String data, SecretKey dataKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);

        // Генерация нового вектора инициализации
        byte[] iv = generateInitializationVector(cipher.getBlockSize());

        // Переключение на алгоритм шифрации ключей
        cipher = Cipher.getInstance(KEY_ENCRYPTION_ALGORITHM);
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

    /**
     * Расшифровка данных
     *
     * @param ciphertext
     * @param dataKey
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     */
    public static String decryptData(String ciphertext, SecretKey dataKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        // Разделение строки на вектор инициализации и зашифрованные данные
        String[] parts = ciphertext.split(":");
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[1]);

        // Переключение на шифр ключей
        Cipher cipher = Cipher.getInstance(KEY_ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, dataKey);
        // Расшифровка вектора инициализации
        byte[] div = cipher.doFinal(iv);

        // Переключение на шифр для данных
        cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);
        // Инициализая шифра для данных
        cipher.init(Cipher.DECRYPT_MODE, dataKey, new IvParameterSpec(div));
        // Расшифровка данных
        byte[] decryptedDataBytes = cipher.doFinal(encryptedData);

        return new String(decryptedDataBytes, "UTF-8");
    }

    // Генерация соли
    private static byte[] generateInitializationVector(int size) {
        byte[] iv = new byte[size];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * Получение имени файла из URI
     *
     * @param uri
     * @return
     */
    public static String getFileNameFromURI(URI uri) {
        // Получаем путь из URI и создаем объект Path
        Path path = Path.of(uri);

        // Получаем абсолютный путь, включая имя драйвера (если есть)
        String absolutePath = path.toAbsolutePath().toString();

        // Возвращаем абсолютный путь в виде строки
        return absolutePath;
    }

    /**
     * Убирание имени файла из URI
     * @param uri
     * @return
     */
    public static String removeFileNameFromPath(URI uri) {
        String path = uri.getPath();
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return path.substring(0, lastSlashIndex + 1);
        }
        return path;
    }

    /**
     * @param uriString
     * @return
     */
    public static boolean isValidURI(String uriString) {
        try {
            new URI(uriString);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Конвертация массива байт в публичный ключ.
     *
     * @param keyBytes
     * @param algorithm
     * @return
     */
    public static PublicKey convertBytesToPublicKey(byte[] keyBytes, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }
}

