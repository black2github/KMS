package ru.gpb.kms.token.entity.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import ru.gpb.kms.token.entity.KeyStatus;
import ru.gpb.kms.token.entity.KeyType;
import ru.gpb.kms.token.entity.PurposeType;

import java.time.LocalDateTime;

/**
 * DTO для работы с сущностью KeyData
 */
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class KeyDataDto {

    @Id
    @Schema(description = "Id of the keyData.")
    private String id;

    @Schema(description = "Alias.")
    @NotNull(message = "Реквизит Алиас является обязательным")
    private String alias;

    @Schema(description = "Description.")
    private String description;

    @Schema(description = "Key.")
    private String key;

    @Schema(description = "Encoded key.")
    private String encodedKey;

    @Schema(description = "Key expiration date and time.")
    @NotNull(message = "Реквизит 'Срок действия' является обязателным")
    private LocalDateTime expiryDate;

    @Schema(description = "Key algorithm.")
    @NotNull(message = "Реквизит Алгортим является обязательным")
    private String algorithm;

    @Schema(description = "Notification data and time.")
    private LocalDateTime notifyDate;

    @Schema(description = "Key type.")
    @NotNull(message = "Реквизит 'Тип ключа' является обязательным")
    private KeyType keyType;

    @Schema(description = "Purpose type.")
    @NotNull(message = "Реквизит 'Тип назначения' является обязательным")
    private PurposeType purposeType;

    @Schema(description = "Related key.")
    @ToString.Exclude
    private String relatedKey;

    @Schema(description = "Creation date and time.")
    private LocalDateTime createdDate;

    @Schema(description = "Status.")
    @Pattern(message = "Invalid Status name", regexp = "[0-9\\w_]+")
    @Length(message = "Invalid Status length", min = 3, max = 16)
    private KeyStatus status;

    @Schema(description = "Encrypted by the key.")
    @ToString.Exclude
    private String encKey;

    @Schema(description = "Online presence.")
    private boolean online;
}
