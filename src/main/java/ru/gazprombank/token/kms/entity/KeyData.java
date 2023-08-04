package ru.gazprombank.token.kms.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Key Data entity class.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since 31.07.2023
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode
@Table(name = "key_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"alias", "key_type"})
})
public class KeyData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Exclude
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
    @NotNull(message = "Реквизит 'Срок действия' является обязательным")
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
    @Column(name = "key_type", length = 16, nullable = false)
    @Convert(converter = KeyTypeConverter.class)
    private KeyType keyType;

    /**
     * Purpose type.
     */
    @NotNull(message = "Реквизит 'Тип назначения' является обязательным")
    @Column(name = "purpose", length = 5, nullable = false)
    @Convert(converter = PurposeTypeConverter.class)
    private PurposeType purposeType;

    /**
     * Related key.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "related_key_id")
    @Fetch(FetchMode.JOIN)
    private KeyData relatedKey;

    /**
     * Encrypted by the key.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "enc_key_id")
    @Fetch(FetchMode.JOIN)
    private KeyData encKey;

    /**
     * Creation date and time.
     */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Status.
     */
    @Column(name = "status", length = 16, nullable = false)
    @Convert(converter = KeyStatusConverter.class)
    private KeyStatus status;

    @OneToMany(mappedBy = "key", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<KeyDataHistory> history = new LinkedList<>();


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

    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (!(o instanceof KeyData keyData)) return false;
    //
    //     if (getAlias() != null ? !getAlias().equals(keyData.getAlias()) : keyData.getAlias() != null) return false;
    //     if (getKey() != null ? !getKey().equals(keyData.getKey()) : keyData.getKey() != null) return false;
    //     if (getExpiryDate() != null ? !getExpiryDate().equals(keyData.getExpiryDate()) : keyData.getExpiryDate() != null)
    //         return false;
    //     if (!getAlgorithm().equals(keyData.getAlgorithm())) return false;
    //     if (getKeyType() != keyData.getKeyType()) return false;
    //     if (getPurposeType() != keyData.getPurposeType()) return false;
    //     if (getCreatedDate() != null ? !getCreatedDate().equals(keyData.getCreatedDate()) : keyData.getCreatedDate() != null)
    //         return false;
    //     return getEncKey() != null ? getEncKey().equals(keyData.getEncKey()) : keyData.getEncKey() == null;
    // }
    //
    // @Override
    // public int hashCode() {
    //     int result = getAlias() != null ? getAlias().hashCode() : 0;
    //     result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
    //     result = 31 * result + (getExpiryDate() != null ? getExpiryDate().hashCode() : 0);
    //     result = 31 * result + getAlgorithm().hashCode();
    //     result = 31 * result + getKeyType().hashCode();
    //     result = 31 * result + getPurposeType().hashCode();
    //     result = 31 * result + (getCreatedDate() != null ? getCreatedDate().hashCode() : 0);
    //     result = 31 * result + (getEncKey() != null ? getEncKey().hashCode() : 0);
    //     return result;
    // }
}