package ru.gpb.kms.service;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import ru.gpb.kms.entity.KeyData;

/**
 * Key password and user holder class.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since 31.07.2023
 */
@Data
public class KeyPassword {
    private KeyData keyData;
    private char[] part1;
    private char[] part2;
    private UserDetails user1;
    private UserDetails user2;
}
