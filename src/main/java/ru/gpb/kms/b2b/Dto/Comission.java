package ru.gpb.kms.b2b.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Comission {
    private float acquirer;
    private float issuer;
    private String currency;
}
