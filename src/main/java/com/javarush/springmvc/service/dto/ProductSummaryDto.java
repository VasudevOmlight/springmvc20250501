package com.javarush.springmvc.service.dto;

 // Data Transfer Object - Об'єкт Передачі Даних
 // Використовується для зручної передачі агрегованих даних (підсумків) між сервісним шаром та іншими частинами програми (наприклад, контролером).
 // Інкапсулює кількість позицій, загальну кількість одиниць та загальну вартість.

public class ProductSummaryDto {

    // Загальна кількість товарних позицій (рядків) у вибірці.
    private final int totalPositions;

    // Сумарна кількість одиниць усіх товарів у вибірці.

    private final int totalUnits;

    // Загальна вартість усіх товарів у вибірці (з урахуванням ціни та кількості кожної позиції).
    private final double totalValue;

    // Конструктор для створення об'єкта DTO з розрахованими підсумками.
    public ProductSummaryDto(int totalPositions, int totalUnits, double totalValue) {
        this.totalPositions = totalPositions;
        this.totalUnits = totalUnits;
        this.totalValue = totalValue;
    }

    // Геттери для доступу до полів DTO
    // Повертає загальну кількість товарних позицій.
    public int getTotalPositions() {
        return totalPositions;
    }

    // Повертає сумарну кількість одиниць товару.
    public int getTotalUnits() {
        return totalUnits;
    }

    // Повертає загальну вартість товару.
    public double getTotalValue() {
        return totalValue;
    }
}