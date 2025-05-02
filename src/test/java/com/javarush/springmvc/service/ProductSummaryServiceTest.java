package com.javarush.springmvc.service;

import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.service.dto.ProductSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductSummaryServiceTest {

    private ProductSummaryService productSummaryService;

    @BeforeEach
    void setUp() {
        // Ініціалізуємо сервіс перед кожним тестом.
        // Оскільки він не має залежностей, просто створюємо новий екземпляр.
        productSummaryService = new ProductSummaryService();
    }

    @Test
    void calculateSummary_ShouldReturnCorrectSummaryForValidProducts() {
        // Створюємо тестові дані (список товарів)
        List<Product> products = Arrays.asList(
                new Product(1L, "Test Product 1", "Description 1", 10.50, 2),
                new Product(2L, "Test Product 2", "Description 2", 5.00, 5),
                new Product(3L, "Test Product 3", "Description 3", 20.00, 1)
        );

        // Викликаємо метод, який тестуємо
        ProductSummaryDto summary = productSummaryService.calculateSummary(products);

        // Перевіряємо результат за допомогою асертів
        assertNotNull(summary, "Результат розрахунку підсумку не має бути null");
        assertEquals(3, summary.getTotalPositions(), "Кількість позицій має бути правильною");
        assertEquals(2 + 5 + 1, summary.getTotalUnits(), "Загальна кількість одиниць має бути правильною");
        // Використовуємо дельту для порівняння чисел з плаваючою точкою
        assertEquals((10.50 * 2) + (5.00 * 5) + (20.00 * 1), summary.getTotalValue(), 0.001, "Загальна вартість має бути правильною");
    }

    @Test
    void calculateSummary_ShouldReturnZeroSummaryForEmptyList() {
        // Тестуємо випадок з порожнім списком
        List<Product> products = Collections.emptyList();

        // Викликаємо метод
        ProductSummaryDto summary = productSummaryService.calculateSummary(products);

        // Перевіряємо результат
        assertNotNull(summary, "Результат розрахунку підсумку не має бути null");
        assertEquals(0, summary.getTotalPositions(), "Кількість позицій має бути 0 для порожнього списку");
        assertEquals(0, summary.getTotalUnits(), "Загальна кількість одиниць має бути 0 для порожнього списку");
        assertEquals(0.0, summary.getTotalValue(), 0.001, "Загальна вартість має бути 0.0 для порожнього списку");
    }

    @Test
    void calculateSummary_ShouldReturnZeroSummaryForNullList() {
        // Тестуємо випадок з null списком
        List<Product> products = null;

        // Викликаємо метод
        ProductSummaryDto summary = productSummaryService.calculateSummary(products);

        // Перевіряємо результат
        assertNotNull(summary, "Результат розрахунку підсумку не має бути null");
        assertEquals(0, summary.getTotalPositions(), "Кількість позицій має бути 0 для null списку");
        assertEquals(0, summary.getTotalUnits(), "Загальна кількість одиниць має бути 0 для null списку");
        assertEquals(0.0, summary.getTotalValue(), 0.001, "Загальна вартість має бути 0.0 для null списку");
    }

    @Test
    void calculateSummary_ShouldHandleProductsWithNullAmountOrPrice() {
        // Тестуємо випадок з товарами, що мають null значення кількості або ціни
        List<Product> products = Arrays.asList(
                new Product(1L, "Product A", "Desc A", 10.0, null), // amount is null
                new Product(2L, "Product B", "Desc B", null, 5),   // price is null
                new Product(3L, "Product C", "Desc C", 20.0, 3),   // valid product
                new Product(4L, "Product D", "Desc D", null, null) // both null
        );

        // Викликаємо метод
        ProductSummaryDto summary = productSummaryService.calculateSummary(products);

        // Перевіряємо результат
        assertNotNull(summary, "Результат розрахунку підсумку не має бути null");
        assertEquals(4, summary.getTotalPositions(), "Кількість позицій має бути правильною");
        // Очікувана кількість одиниць має бути 5 (з Product B) + 3 (з Product C) = 8
        assertEquals(8, summary.getTotalUnits(), "Загальна кількість одиниць має враховувати тільки ненульові значення");
        // Перевірка загальної вартості залишається, як було, оскільки вона залежить від обох ненульових значень
        // Product A: 10.0 * null -> ігнорується
        // Product B: null * 5 -> ігнорується
        // Product C: 20.0 * 3 = 60.0
        // Product D: null * null -> ігнорується
        assertEquals(60.0, summary.getTotalValue(), 0.001, "Загальна вартість має враховувати тільки ненульові значення");
    }
}