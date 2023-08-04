package ru.gpb.kms.token.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.util.HashMap;

@RestController
public class URL3Controller {
    private static final Logger log = LoggerFactory.getLogger(URL3Controller.class);

    @GetMapping("/url3")
    public ResponseEntity<String> handle(@CookieValue(name = "X-arg1") String arg1,
                                          @CookieValue(name = "X-arg2") String arg2,
                                          HttpServletRequest request) {
        HashMap<String, String> data = new HashMap<>();
        log.info("handle: <- " + " arg1=" + arg1 + ", arg2=" + arg2);

        // Получаем массив всех cookies из запроса
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {

            // Проходимся по массиву и вытаскиваем инфу по каждому
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                String value = URLDecoder.decode(cookie.getValue());
                if (name.startsWith("X-") && name.length() > 2) {
                    String key = name.substring(2);
                    data.put(key, value);
                }
            }
        } else {
            log.warn("handle: В запросе нет ожидаемых данных.");
        }

        log.info("handle: data = " + data);

        return new ResponseEntity<>("Hello " + arg1 + " " + arg2 + "!", HttpStatus.OK);
    }
}