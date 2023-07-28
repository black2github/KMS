package ru.gazprombank.token.kms.controller;

import lombok.Data;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;

@Data
public class MasterKeyGenerationRequest {
    private KeyDataDto keyDataDto;
    private String password;
}
