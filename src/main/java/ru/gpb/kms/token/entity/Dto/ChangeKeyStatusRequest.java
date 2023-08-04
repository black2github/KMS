package ru.gpb.kms.token.entity.Dto;

import lombok.Builder;
import lombok.Data;
import ru.gpb.kms.token.entity.KeyStatus;

@Data
@Builder
public class ChangeKeyStatusRequest {
    String id;
    private KeyStatus status;
}
