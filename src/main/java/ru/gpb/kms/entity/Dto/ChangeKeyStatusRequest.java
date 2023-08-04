package ru.gpb.kms.entity.Dto;

import lombok.Builder;
import lombok.Data;
import ru.gpb.kms.entity.KeyStatus;

@Data
@Builder
public class ChangeKeyStatusRequest {
    String id;
    private KeyStatus status;
}
