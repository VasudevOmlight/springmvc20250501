package com.javarush.springmvc.repository;

import com.javarush.springmvc.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
// Інтерфейс, що успадковує функціонал Spring Data JPA.
// Він є частиною шару доступу до даних і надає стандартні методи для роботи з таблицею products
// (збереження, пошук за ID, пошук усіх, видалення тощо), а також ваші кастомні методи запитів
// (як пошук за назвою/описом з зображеннями).
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Запит для використання параметра search та додано LEFT JOIN FETCH p.images для завантаження зображень при пошуку
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Product> findByNameOrDescriptionContainingWithImages(@Param("search") String search);

    // Метод для завантаження продукту разом із зображеннями за ID (залишаємо без змін)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // Стандартний findById надається JpaRepository за замовчуванням
}