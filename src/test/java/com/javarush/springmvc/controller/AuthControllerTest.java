package com.javarush.springmvc.controller;

import org.junit.jupiter.api.DisplayName; // Анотація для надання описового імені тесту
import org.junit.jupiter.api.Test; // Основна анотація для позначення тестового методу в JUnit 5
import org.springframework.beans.factory.annotation.Autowired; // Анотація для автоматичного впровадження залежностей Spring
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // Анотація для тестування тільки веб-шару (контролерів)
import org.springframework.boot.test.mock.mockito.MockBean; // Створює та впроваджує мок-об'єкт для вказаного біна
import org.springframework.context.annotation.Import; // Дозволяє імпортувати додаткові конфігурації
import org.springframework.security.core.userdetails.UserDetailsService; // Імпорт інтерфейсу, який потрібно мокувати
import org.springframework.test.web.servlet.MockMvc; // Основний клас для виконання HTTP-запитів до контролера в тестах

// Імпортуємо конфігурацію безпеки, щоб контекст тесту знав про неї
import com.javarush.springmvc.config.WebSecurityConfig;

// Імпорти статичних методів для зручного використання MockMvc та ResultMatchers
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; // Метод для створення GET-запиту
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status; // Метод для перевірки HTTP-статусу відповіді
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view; // Метод для перевірки імені представлення (view)

// Використовує {@link WebMvcTest} для тестування веб-шару ізольовано, фокусуючись тільки на {@link AuthController}.
// {@link MockMvc} використовується для симуляції HTTP-запитів та перевірки відповідей.

@WebMvcTest(AuthController.class) // Вказує Spring Boot Test налаштувати середовище тільки для тестування AuthController.
@Import(WebSecurityConfig.class) // Імпортуємо конфігурацію безпеки, оскільки вона впливає на обробку запитів, навіть для дозволених URL.
public class AuthControllerTest {

    // MockMvc надає програмний інтерфейс для виконання HTTP-запитів до контролера
    // та перевірки відповідей (статус, заголовки, вміст, ім'я представлення тощо)
    // без необхідності розгортання повного веб-сервера.
    // Spring Boot автоматично налаштовує та впроваджує цей бін при використанні @WebMvcTest.
    @Autowired // Впроваджуємо бін MockMvc
    private MockMvc mockMvc;

    // Мокуємо (створюємо імітацію) UserDetailsService.
    @MockBean // Створює мок-об'єкт для UserDetailsService і додає його в контекст тесту
    private UserDetailsService userDetailsService;

    // Тест для методу {@link AuthController#loginPage()}.
    // Мета: Перевірити, що GET-запит на ендпоінт "/login" успішно обробляється
    // контролером, повертає HTTP-статус 200 (OK), що свідчить про успіх,
    // і що контролер повертає правильне логічне ім'я представлення ("login"),
    // яке потім буде використано для рендерингу HTML-сторінки входу.
    @Test
    @DisplayName("Повинен повертати статус OK та представлення 'login' для GET /login")
    public void loginPage_ShouldReturnOkStatusAndLoginViewName() throws Exception {
        // Крок 1: Симулюємо виконання HTTP GET-запиту до ендпоінту "/login"
        mockMvc.perform(get("/login"))
                // Крок 2: Перевіряємо очікуваний результат (Assertions)
                .andExpect(status().isOk()) // Очікуємо, що HTTP-статус відповіді буде 200 (OK)
                .andExpect(view().name("login")); // Очікуємо, що ім'я представлення (view name), повернуте контролером, буде "login"
    }
}