package ru.gazprombank.token.kms.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Creation date and time.
     */

    @NotNull(message = "Реквизит 'Время создания' обязателен")
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Expiration date and time.
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * Encryption key
     */
    @NotNull(message = "Реквизит 'Ключ шифрования' обязателен")
    //@ToString.Exclude
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn
    @Fetch(FetchMode.JOIN)
    private KeyData key;

    /**
     * Secret data.
     */
    @NotNull(message = "Реквизит 'Секрет' обязателен")
    @Column(nullable = false)
    private String secret;

    /**
     * Type.
     */
    @Column(length = 32)
    @Convert(converter = TokenTypeConverter.class)
    private TokenType type;

    /**
     * Index.
     */
    @Column(length = 128)
    private String index;

    /**
     *
     * @param secret
     * @param type
     */
    public Token(KeyData key, String secret, TokenType type) {
        this.key = key;
        this.secret = secret;
        this.type = type;
        this.createdDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;

        if (!getCreatedDate().equals(token.getCreatedDate())) return false;
        if (getExpiryDate() != null ? !getExpiryDate().equals(token.getExpiryDate()) : token.getExpiryDate() != null)
            return false;
        if (!getKey().equals(token.getKey())) return false;
        if (!getSecret().equals(token.getSecret())) return false;
        if (getType() != null ? !getType().equals(token.getType()) : token.getType() != null) return false;
        return getIndex() != null ? getIndex().equals(token.getIndex()) : token.getIndex() == null;
    }

    @Override
    public int hashCode() {
        int result = getCreatedDate().hashCode();
        result = 31 * result + (getExpiryDate() != null ? getExpiryDate().hashCode() : 0);
        result = 31 * result + getSecret().hashCode();
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getIndex() != null ? getIndex().hashCode() : 0);
        return result;
    }
}

@Converter
class TokenTypeConverter implements AttributeConverter<TokenType, String> {
    @Override
    public String convertToDatabaseColumn(TokenType type) {
        switch (type) {
            case PAN: return "PAN";
            default: return "PAN";
        }
    }
    @Override
    public TokenType convertToEntityAttribute(String s) {
        switch (s) {
            case "PAN": return TokenType.PAN;
            default: return TokenType.PAN;
        }
    }
}