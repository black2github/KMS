package ru.gazprombank.token.kms.entity.Dto;

import lombok.Builder;
import lombok.Data;
import ru.gazprombank.token.kms.entity.KeyStatus;

@Data
@Builder
public class ChangeKeyStatusRequest {
    String id;
    private KeyStatus status;
}
