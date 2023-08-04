package ru.gazprombank.token.kms.entity.b2bDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Amount {
    private float amount;
    private String currency;
}
