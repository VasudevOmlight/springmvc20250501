package com.javarush.springmvc.security;

import jakarta.annotation.PostConstruct; // Імпорт анотації PostConstruct
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level; // Імпорт для рівнів логування
import java.util.logging.Logger; // Імпорт Logger

//FileUserDetailsService надає Spring Security дані про користувачів, завантажуючи їх з файлу users.properties
@Service // Робимо його біном Spring
public class FileUserDetailsService implements UserDetailsService {

    // Додаємо логер для класу
    private static final Logger logger = Logger.getLogger(FileUserDetailsService.class.getName());

    // Вказуємо шлях до файлу з користувачами у ресурсах
    @Value("classpath:users.properties")
    private Resource usersFile;

    // Ініціалізуємо об'єкт Properties одразу
    private final Properties users = new Properties();

    // Метод для завантаження користувачів ПІСЛЯ ініціалізації біна 
    @PostConstruct
    public void loadUsers() {
        // Перевіряємо, чи ресурс був впроваджений
        if (usersFile == null || !usersFile.exists()) {
            logger.log(Level.SEVERE, "Файл користувачів 'users.properties' не знайдений або не впроваджений!");
            // Можна викинути виняток або обробити ситуацію іншим чином
            throw new RuntimeException("Users properties file not found or not injected.");
        }

        logger.info("Завантаження користувачів з файлу: " + usersFile);
        try (InputStream is = usersFile.getInputStream()) {
            users.load(is);
            logger.info("Користувачів завантажено успішно. Знайдено ключів: " + users.size());
            // Логування завантажених імен користувачів (без паролів!)
            users.stringPropertyNames().forEach(username -> logger.fine("Знайдено користувача: " + username));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Помилка завантаження файлу користувачів: " + usersFile, e);
            // В реальному додатку тут може бути більш продумана обробка помилки
            throw new RuntimeException("Failed to load users properties file.", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Неочікувана помилка під час завантаження користувачів", e);
            throw new RuntimeException("Unexpected error during user loading.", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.fine("Спроба завантаження даних для користувача: " + username);

        // Отримуємо рядок "пароль,роль1,роль2,..." для даного логіну з файлу
        String userDetailsString = users.getProperty(username);

        if (userDetailsString == null) {
            logger.warning("Користувач з логіном '" + username + "' не знайдений у файлі.");
            // Якщо логін не знайдено у файлі
            throw new UsernameNotFoundException("Користувач з логіном " + username + " не знайдений");
        }
        logger.fine("Знайдено дані для користувача '" + username + "': " + userDetailsString.substring(0, userDetailsString.indexOf(',')) + ",[ROLES HIDDEN]"); // Приховуємо ролі при логуванні

        // Розбиваємо рядок на пароль та ролі
        String[] details = userDetailsString.split(",");
        if (details.length < 2) { // Має бути як мінімум пароль і одна роль
            logger.severe("Неправильний формат даних у файлі для користувача: " + username);
            throw new UsernameNotFoundException("Неправильний формат даних користувача для логіну " + username);
        }

        String password = details[0];
        // Ролі починаються з другого елемента масиву
        String[] roles = new String[details.length - 1];
        System.arraycopy(details, 1, roles, 0, roles.length);

        logger.fine("Створення UserDetails для користувача '" + username + "' з ролями: " + String.join(",", roles));

        // Пароль читається у відкритому вигляді! Оскільки використовується NoOpPasswordEncoder, пароль не хешується.
        // Створюємо об'єкт UserDetails
        return User.withUsername(username)
                .password(password) // Передається відкритий пароль з файлу
                .roles(roles)       // Передаються ролі
                .build();
    }
}