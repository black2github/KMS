package ru.gpb.kms.token.entity.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MasterKeyGenerationRequest {
    private KeyDataDto key;
    private String password;
}
