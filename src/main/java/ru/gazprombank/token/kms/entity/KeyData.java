package ru.gazprombank.token.kms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.Length;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
@RequiredArgsConstructor
@Table(name = "key_data")
public class KeyData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Alias.
     */
    @NotNull(message = "Реквизит Алиас является обязательным")
    @Column(name = "alias", length = 128, nullable = false)
    private String alias;

    /**
     * Description.
     */
    @Column(name = "description", length = 128, nullable = true)
    private String description;

    /**
     * Key.
     */
    @Column(name = "key", length = 4096, nullable = true)
    private String key;

    /**
     * Key expiration date and time.
     */
    @NotNull(message = "Реквизит 'Срок действия' является обязателным")
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * Key algorithm.
     */
    @NotNull(message = "Реквизит Алгортим является обязательным")
    @Column(name = "algorithm", length = 127, nullable = false)
    private String algorithm;

    /**
     * Notification data and time.
     */
    @Column(name = "notify_date")
    private LocalDateTime notifyDate;

    /**
     * Key type.
     */

    @NotNull(message = "Реквизит 'Тип ключа' является обязательным")
    @Column(name = "type", length = 16, nullable = false)
    private KeyType keyType;

    /**
     * Purpose type.
     */
    @NotNull(message = "Реквизит 'Тип назначения' является обязательным")
    @Column(name = "purpose", length = 5, nullable = false)
    private PurposeType purposeType;

    /**
     * Related key.
     */
    @ToString.Exclude
    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn
    @Fetch(FetchMode.JOIN)
    private KeyData relatedKey;

    /**
     * Creation date and time.
     */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Status.
     */
    // @Pattern(message = "Invalid Status name", regexp = "[0-9\\w_]+")
    // @Length(message = "Invalid Status length", min = 3, max = 16)
    @Column(name = "status", length = 16, nullable = false)
    private KeyStatus status;

    /**
     * Encrypted by the key.
     */
    @ToString.Exclude
    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn
    @Fetch(FetchMode.JOIN)
    private KeyData encKey;

    /**
     * Online presence.
     */
    @Column(name = "online")
    private boolean online;

    /**
     *
     * @param alias
     * @param algorithm
     * @param keyType
     * @param purposeType
     * @param status
     */
    public KeyData(String alias, String algorithm, KeyType keyType, PurposeType purposeType, KeyStatus status) {
        this.alias = alias;
        this.algorithm = algorithm;
        this.keyType = keyType;
        this.purposeType = purposeType;
        this.status = status;
        this.createdDate = LocalDateTime.now();
        this.expiryDate = createdDate.plusYears(10); // default - 10 years
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyData keyData)) return false;

        if (getAlias() != null ? !getAlias().equals(keyData.getAlias()) : keyData.getAlias() != null) return false;
        if (getKey() != null ? !getKey().equals(keyData.getKey()) : keyData.getKey() != null) return false;
        if (getExpiryDate() != null ? !getExpiryDate().equals(keyData.getExpiryDate()) : keyData.getExpiryDate() != null)
            return false;
        if (!getAlgorithm().equals(keyData.getAlgorithm())) return false;
        if (getKeyType() != keyData.getKeyType()) return false;
        if (getPurposeType() != keyData.getPurposeType()) return false;
        if (getCreatedDate() != null ? !getCreatedDate().equals(keyData.getCreatedDate()) : keyData.getCreatedDate() != null)
            return false;
        return getEncKey() != null ? getEncKey().equals(keyData.getEncKey()) : keyData.getEncKey() == null;
    }

    @Override
    public int hashCode() {
        int result = getAlias() != null ? getAlias().hashCode() : 0;
        result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
        result = 31 * result + (getExpiryDate() != null ? getExpiryDate().hashCode() : 0);
        result = 31 * result + getAlgorithm().hashCode();
        result = 31 * result + getKeyType().hashCode();
        result = 31 * result + getPurposeType().hashCode();
        result = 31 * result + (getCreatedDate() != null ? getCreatedDate().hashCode() : 0);
        result = 31 * result + (getEncKey() != null ? getEncKey().hashCode() : 0);
        return result;
    }
}