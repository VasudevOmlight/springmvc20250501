package com.javarush.springmvc.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

// Конфігураційний клас для налаштування інтернаціоналізації (i18n) у Spring MVC.
// Визначає біни для джерела повідомлень, визначника локалі та перехоплювача зміни локалі.

@Configuration // Позначає цей клас як джерело конфігурації бінів Spring
public class I18nConfig implements WebMvcConfigurer { // Реалізує інтерфейс для налаштування Spring MVC

    // Визначає бін MessageSource, відповідальний за завантаження та надання
    // перекладених повідомлень (текстів) з файлів властивостей.
    // Об'єкт MessageSource, налаштований для роботи з ресурсними бандлами.

    @Bean // Позначає цей метод як фабричний метод для створення біна messageSource
    public MessageSource messageSource() {
        // Використовуємо ResourceBundleMessageSource для завантаження повідомлень з файлів .properties
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // Вказуємо базові імена файлів (без розширення та суфіксів локалі).
        // Spring автоматично шукатиме файли типу messages_uk.properties, messages_en.properties тощо
        // у папці 'language' в classpath (зазвичай src/main/resources/language).
        messageSource.setBasenames("language/messages");
        // Встановлюємо кодування за замовчуванням для файлів властивостей. UTF-8 підтримує кирилицю.
        messageSource.setDefaultEncoding("UTF-8");
        // Повертаємо налаштований об'єкт, який стане біном Spring
        return messageSource;
    }

    // Визначає бін LocaleResolver, відповідальний за визначення та збереження
    // поточної локалі для сесії користувача.
    // Об'єкт LocaleResolver, налаштований для використання сесії та з локаллю за замовчуванням.

    @Bean // Позначає цей метод як фабричний метод для створення біна localeResolver
    public LocaleResolver localeResolver() {
        // Використовуємо SessionLocaleResolver, який зберігає обрану локаль в HTTP-сесії.
        // Це означає, що вибір мови буде діяти протягом усієї сесії користувача.
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        // Встановлюємо локаль за замовчуванням. Якщо локаль не може бути визначена з запиту
        // або сесії, буде використовуватися ця локаль (Англійська (США)).
        localeResolver.setDefaultLocale(Locale.US);
        // Повертаємо налаштований об'єкт, який стане біном Spring
        return localeResolver;
    }

    // Визначає бін LocaleChangeInterceptor. Цей перехоплювач аналізує вхідні HTTP-запити
    // на наявність певного параметра і, якщо знаходить його, змінює поточну локаль користувача
    // (зберігаючи її за допомогою LocaleResolver).
    // Об'єкт LocaleChangeInterceptor, налаштований на зміну локалі за параметром 'language'.

    @Bean // Позначає цей метод як фабричний метод для створення біна localeChangeInterceptor
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        // Встановлюємо ім'я параметра HTTP-запиту, який буде використовуватися для зміни локалі.
        // Наприклад, запит /products?language=uk встановить українську локаль.
        lci.setParamName("language");
        // Повертаємо налаштований об'єкт, який стане біном Spring
        return lci;
    }

    // Реєструє створений LocaleChangeInterceptor у Spring MVC.
    // Цей метод викликається Spring для додавання кастомних перехоплювачів до конвеєра обробки запитів.
    // registry Реєстр перехоплювачів, куди додається наш LocaleChangeInterceptor.

    @Override // Перевизначаємо метод з інтерфейсу WebMvcConfigurer
    public void addInterceptors(InterceptorRegistry registry) {
        // Додаємо наш перехоплювач зміни локалі до реєстру Spring MVC.
        // Тепер він буде викликатися для кожного вхідного запиту.
        registry.addInterceptor(localeChangeInterceptor());
    }
}