package ru.gazprombank.token.kms.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserController {

    public void getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                // Получение информации о пользователе
                String username = userDetails.getUsername();
                // Дополнительная информация о пользователе, например, роли
                // userDetails.getAuthorities();

                // Действия с информацией о пользователе
                // ...
            }
        }
    }
}

