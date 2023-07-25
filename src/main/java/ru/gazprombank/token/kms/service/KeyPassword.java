package ru.gazprombank.token.kms.service;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import ru.gazprombank.token.kms.entity.KeyData;

@Data
public class KeyPassword {
    private KeyData keyData;
    private char[] part1;
    private char[] part2;
    private UserDetails user1;
    private UserDetails user2;
}
