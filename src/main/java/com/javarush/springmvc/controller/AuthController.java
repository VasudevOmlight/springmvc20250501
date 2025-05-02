package com.javarush.springmvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// Імпортуємо Logger з java.util.logging
import java.util.logging.Logger;


@Controller
@RequestMapping("/") // Контролер обробляє запити від кореня контексту (наприклад, /login)
public class AuthController {

    // Додаємо логер
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());


    // Обробка запиту на відображення стандартної форми входу Spring Security
    // Spring Security автоматично обробляє POST запит на /login для обробки введених даних
    @GetMapping("/login")
    public String loginPage() {
        // Додамо логування, щоб переконатись, що цей метод викликається
        logger.info("Запит на сторінку входу (/login) отримано. Повертаємо шаблон 'login'.");
        return "login"; // Повертаємо назву шаблону форми входу (login.html)
    }
}