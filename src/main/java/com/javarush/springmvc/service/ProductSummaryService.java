package com.javarush.springmvc.service;

import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.service.dto.ProductSummaryDto;
import org.springframework.stereotype.Service;
// Анотація Spring для позначення класу як сервісу (компонент бізнес-логіки)
import java.util.List;

// Сервісний клас, відповідальний за бізнес-логіку, пов'язану з підрахунком підсумкових даних для списку товарів.

@Service // Позначає цей клас як сервісний компонент Spring. Spring автоматично знайде його при скануванні.
public class ProductSummaryService {

    // Розраховує підсумкові дані (кількість позицій, загальну кількість одиниць, загальну вартість)
    // для наданого списку об'єктів Product.

    public ProductSummaryDto calculateSummary(List<Product> products) {
        // Перевірка на випадок, якщо передано null замість списку
        if (products == null) {
            // Повертаємо DTO з нульовими значеннями, щоб уникнути помилок далі
            return new ProductSummaryDto(0, 0, 0.0);
        }

        // Кількість позицій - це просто розмір наданого списку.
        int totalPositions = products.size();

        // Розрахунок загальної кількості одиниць товару.
        // Використовуємо Stream API для обробки списку:
        int totalUnits = products.stream() // Створюємо потік (stream) з елементів списку products.
                // mapToInt: Перетворює кожен Product у потоці на int (його кількість).
                // p.getAmount() != null ? p.getAmount() : 0 : Тернарний оператор для безпечного отримання кількості. Якщо amount не null, беремо його значення, інакше - 0.
                .mapToInt(p -> p.getAmount() != null ? p.getAmount() : 0)
                // sum: Підсумовує всі отримані значення кількості.
                .sum();

        // Розрахунок загальної вартості всіх товарів.
        // Використовуємо Stream API:
        double totalValue = products.stream() // Створюємо потік з елементів списку products.
                // filter: Відфільтровує товари, у яких ціна або кількість дорівнює null, щоб уникнути NullPointerException при множенні.
                .filter(p -> p.getPrice() != null && p.getAmount() != null)
                // mapToDouble: Перетворює кожен відфільтрований Product на double (його вартість = ціна * кількість).
                .mapToDouble(p -> p.getPrice() * p.getAmount())
                // sum: Підсумовує всі отримані значення вартості.
                .sum();

        // Створюємо та повертаємо новий об'єкт DTO з розрахованими значеннями.
        return new ProductSummaryDto(totalPositions, totalUnits, totalValue);
    }
}