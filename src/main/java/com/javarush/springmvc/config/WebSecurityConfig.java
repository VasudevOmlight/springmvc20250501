package com.javarush.springmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder; // Тимчасово, для прикладу
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//WebSecurityConfig налаштовує фільтр-ланцюжок безпеки Spring Security. Він визначає:
//Які URL-адреси доступні без автентифікації (наприклад, статичні ресурси /css/**, /js/**, /images/**, /favicon.png, /login).
//Які URL-адреси захищені і вимагають певної ролі (наприклад, /products/** вимагає роль ADMIN).
//Як відбувається форма входу (/login).
//Як відбувається вихід (/logout).
@Configuration
@EnableWebSecurity // Включає веб-безпеку Spring Security
public class WebSecurityConfig {

    // Визначаємо UserDetailsService, який буде завантажувати дані користувачів
    @Bean
    public UserDetailsService userDetailsService() {
        // Використовуємо наш кастомний сервіс для читання з файлу
        return new com.javarush.springmvc.security.FileUserDetailsService(); // Повна назва класу
    }

    // Налаштовуємо провайдера автентифікації
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder()); // Використовуємо PasswordEncoder
        return provider;
    }

    // Визначаємо PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Тимчасово, без кодування паролів
    }


    // Налаштовуємо правила авторизації та форму входу/виходу
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Відключаємо CSRF для простоти прикладу (НЕ РЕКОМЕНДУЄТЬСЯ в продакшені)
                .authorizeHttpRequests((requests) -> requests
                        // Дозволяємо доступ до статичних ресурсів, сторінки помилки та сторінки входу всім
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error", "/login").permitAll()

                        // Дозволяємо доступ до списку товарів та перегляду товару ВСІМ (анонімним та автентифікованим)
                        .requestMatchers("/products", "/products/view/*").permitAll()

                        // Дозволяємо доступ до додавання, редагування та видалення товарів тільки користувачам з роллю ADMIN
                        .requestMatchers("/products/add", "/products/edit/*", "/products/delete/*", "/products/delete-image/*").hasRole("ADMIN")

                        // Будь-який інший запит вимагає автентифікації (хоча з попередніми правилами це може бути зайвим)
                        // .anyRequest().authenticated() // Можна закоментувати або залишити в кінці як правило за замовчуванням
                        .anyRequest().authenticated() // Залишаємо, щоб заблокувати будь-які невідомі URL

                )
                .formLogin((form) -> form
                        .loginPage("/login") // Вказуємо URL сторінки входу (ми її створили)
                        .defaultSuccessUrl("/products", true) // URL після успішного входу
                        .permitAll() // Дозволяємо доступ до форми входу всім
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/products") // URL після успішного виходу
                        .permitAll() // Дозволяємо доступ до виходу всім
                );

        return http.build();
    }
}