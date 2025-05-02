package com.javarush.springmvc.service;

import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.entity.ProductImage; // Додано імпорт ProductImage
import com.javarush.springmvc.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Comparator;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    // Метод пошуку та сортування (завантажує зображення і коректно застосовує сортування)
    public List<Product> searchAndSortProducts(String search, String sortBy, String sortDir) {
        String validSortBy = switch (sortBy) {
            case "price", "amount" -> sortBy;
            default -> "name";
        };
        // Об'єкт Sort потрібен для потенційного використання в репозиторії, але компаратор створюємо вручну
        // Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, validSortBy);

        List<Product> products;

        if (search != null && !search.isEmpty()) {
            // Шукаємо за іменем/описом (зображення завантажуються)
            products = productRepository.findByNameOrDescriptionContainingWithImages(search);
            // Сортуємо отриманий список в пам'яті, створюючи компаратор вручну
            Comparator<Product> comparator;
            switch (validSortBy) {
                case "price":
                    comparator = Comparator.comparing(Product::getPrice);
                    break;
                case "amount":
                    comparator = Comparator.comparing(Product::getAmount);
                    break;
                case "name":
                default:
                    comparator = Comparator.comparing(Product::getName);
                    break;
            }

            if (sortDir.equalsIgnoreCase("desc")) {
                comparator = comparator.reversed();
            }

            products.sort(comparator);

        } else {
            // Якщо пошуку немає, отримуємо всі продукти з сортуванням (зображення не завантажуються eagerly методом findAll(sort))
            // Якщо потрібно eager завантаження зображень тут, слід додати метод findAllWithImages(Sort sort) в ProductRepository
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, validSortBy);
            products = productRepository.findAll(sort);
        }

        return products;
    }


    @Transactional(readOnly = true)
    public Product findById(Long id) {
        // Викликаємо метод, який завантажує зображення разом з продуктом
        return productRepository.findByIdWithImages(id).orElse(null);
    }

    @Transactional
    public void deleteById(Long id) {
        // Перевірка існування перед видаленням (опціонально, але безпечніше)
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            logger.info("Deleted product with ID: {}", id);
        } else {
            logger.warn("Attempted to delete non-existent product with ID: {}", id);
        }
    }

    @Transactional
    public void deleteImageById(Long imageId) {
        // Метод видаляє зображення за ID
        productRepository.findAll().stream()
                .filter(p -> p.getImages().stream().anyMatch(img -> img.getId().equals(imageId)))
                .findFirst() // Знаходимо перший (і єдиний) продукт з цим зображенням
                .ifPresent(product ->
                {
                    // Отримуємо шлях до файлу зображення перед видаленням з колекції
                    String imagePath = product.getImages().stream()
                            .filter(image -> image.getId().equals(imageId))
                            .findFirst()
                            .map(ProductImage::getImagePath)
                            .orElse(null);

                    product.getImages().removeIf(image -> image.getId().equals(imageId));
                    productRepository.save(product); // Зберігаємо тільки змінений продукт
                    logger.info("Removed image with ID: {} from product ID: {}", imageId, product.getId()); // Логування видалення з БД

                    // Опціонально: видалення самого файлу зображення з файлової системи
                    if (imagePath != null) {
                        try {
                            // Перетворюємо веб-шлях (/images/...) у файловий шлях
                            // Потрібно знати базовий шлях до статичних ресурсів (src/main/resources/static)
                            Path filePath = Paths.get("src/main/resources/static" + imagePath);
                            Files.deleteIfExists(filePath);
                            logger.info("Deleted image file: {}", filePath); // Логування видалення файлу
                        } catch (IOException e) {
                            logger.error("Error deleting image file: {}", imagePath, e); // Логування помилки видалення файлу
                        }
                    }
                });
    }
}