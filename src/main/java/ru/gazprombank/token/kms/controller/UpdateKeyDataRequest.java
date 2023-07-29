package ru.gazprombank.token.kms.controller;

import lombok.Builder;
import lombok.Data;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;

@Data
@Builder
public class UpdateKeyDataRequest {
    String id;
    private KeyDataDto key;
}
