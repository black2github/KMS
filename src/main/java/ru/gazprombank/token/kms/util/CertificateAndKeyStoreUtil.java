package ru.gazprombank.token.kms.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import ru.gazprombank.token.kms.service.KeyPersistApplicationException;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

// генерация и сохранение в файл самоподписанного сертификата для всяческих тестов.
@Slf4j
@UtilityClass
public class CertificateAndKeyStoreUtil {

    public static void main(String[] args) {
        try {
            char[] storePassword = "storePassword".toCharArray();
            char[] keyPassword = "keyPassword".toCharArray();
            String alias = "myKey";
            String storeName = "myKeystore.jks";

            // Генерация приватного и публичного ключа с алгоритмом RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Сохранение пары ключей в jks.store файл
            storeKeyPair(keyPair, storeName, alias, storePassword, keyPassword);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Сохранение приватного и публичного ключа в хранилище.
     * @param keyPair
     * @param storeName
     * @param alias
     * @param storePassword
     * @param keyPassword
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void storeKeyPair(KeyPair keyPair, String storeName, String alias, char[] storePassword, char[] keyPassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        log.info(String.format("storeKeyPair: <- store='%s', alias='%s'", storeName, alias));

        // Создание самоподписанного сертификата
        X509Certificate cert = generateSelfSignedCertificate(keyPair);

        // Сохранение сертификата и секретного ключа в jks.store файл
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, storePassword);
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), keyPassword, new Certificate[]{cert});

        try (FileOutputStream fos = new FileOutputStream(storeName)) {
            keyStore.store(fos, storePassword);
        } catch (Exception ex) {
            String msg = String.format("Некорректный URI: '%s'", ex.getMessage());
            log.error("storeKeyPair: " + msg);
            throw new KeyPersistApplicationException(msg);
        }
        log.info("storeKeyPair: JKS store '" + storeName + "' created.");
    }

    /**
     * Генерация самоподписанного сертификата
     * @param keyPair
     * @return
     * @throws Exception
     */
    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws CertificateException {
        // Дата начала и окончания действия сертификата
        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 10 * 365 * 24 * 60 * 60 * 1000);

        // Идентификатор сертификата
        BigInteger certSerialNumber = BigInteger.valueOf(System.currentTimeMillis());

        // Имя субъекта сертификата
        X500Principal subjectName = new X500Principal("CN=kms.eco.gazprombank.ru");

        // Строитель сертификата
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subjectName,
                certSerialNumber,
                startDate,
                endDate,
                subjectName,
                keyPair.getPublic()
        );

        // Подписывающий алгоритм
        try {
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
            // Создание самоподписанного сертификата
            return new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
        } catch (Exception ex) {
            log.error("generateSelfSignedCertificate: " + ex.getMessage());
            throw new CertificateException(ex.getMessage());
        }
    }
}

