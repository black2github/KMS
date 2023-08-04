package ru.gazprombank.token.kms.entity.b2bDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Source {
    private String pan;
    private String maskNum;
    private String virtualNum;
}
