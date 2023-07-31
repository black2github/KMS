package ru.gazprombank.token.kms.controller;

import lombok.Builder;
import lombok.Data;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyStatus;

@Data
@Builder
public class ChangeKeyStatusRequest {
    String id;
    private KeyStatus status;
}
