package ru.gazprombank.token.kms.controller;

import lombok.Builder;
import lombok.Data;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;

@Data
@Builder
public class MasterKeyGenerationRequest {
    private KeyDataDto key;
    private String password;
}
