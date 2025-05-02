package com.javarush.springmvc.config;
import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger; // імпорт Logger
import org.slf4j.LoggerFactory; // імпорт LoggerFactory

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class); // Додано логер

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Перевірка, чи база даних порожня (опціонально, щоб не додавати дані щоразу)
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "iPhone", "Bad expensive phone", 1312.34, 3));
            productRepository.save(new Product(null, "Xiaomi", "Not bad phone", 223.45, 5));
            productRepository.save(new Product(null, "Motorola G54", "Golden mean", 134.56, 9));
            // System.out.println("Database initialized with sample products."); // Замінено на логування
            logger.info("Database initialized with sample products.");
        } else {
            // System.out.println("Database already contains data. Skipping initialization.");
            logger.info("Database already contains data. Skipping initialization.");
        }
    }
}
