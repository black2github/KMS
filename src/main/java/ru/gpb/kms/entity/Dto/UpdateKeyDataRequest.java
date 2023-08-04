package ru.gpb.kms.entity.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateKeyDataRequest {
    String id;
    private KeyDataDto key;
}
