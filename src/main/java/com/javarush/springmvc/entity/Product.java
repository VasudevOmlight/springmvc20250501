package com.javarush.springmvc.entity;

// Імпорти класів для роботи з JPA (Java Persistence API) та валідацією
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

// Налаштування бази даних
// Клас-сутність (Entity), що представляє товар у базі даних.
// Містить інформацію про назву, опис, ціну, кількість та пов'язані зображення.
@Entity // Позначає, що цей клас є сутністю JPA і буде відображений на таблицю в базі даних.
@Table(name = "products") // Явно вказує ім'я таблиці в базі даних ("products").
public class Product {

    // Унікальний ідентифікатор товару (первинний ключ).

    @Id // Позначає поле 'id' як первинний ключ таблиці.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Вказує, що значення ID буде генеруватися базою даних автоматично (зазвичай автоінкремент).
    private Long id;

    // Назва товару.
    // Обов'язкове поле, не може бути порожнім рядком.
    @NotBlank(message = "Name is required") // Анотація валідації: поле не може бути null або складатися тільки з пробілів. Якщо умова порушена, буде використано повідомлення "Name is required".
    @Column(nullable = false) // Відображення на колонку БД. `nullable = false` означає, що колонка не може містити NULL.
    private String name;

    // Опис товару.
    // Необов'язкове поле.
    private String description;

    // Ціна товару.
    // Обов'язкове поле, не може бути від'ємним.
    @NotNull(message = "Price is required") // Анотація валідації: поле не може бути null.
    @Min(value = 0, message = "Price must be non-negative") // Анотація валідації: значення ціни повинно бути 0 або більше.
    @Column(nullable = false) // Відображення на колонку БД, яка не може містити NULL.
    private Double price;

    // Кількість товару на складі.
    // Обов'язкове поле, не може бути від'ємним.
    @NotNull(message = "Amount is required") // Анотація валідації: поле не може бути null.
    @Min(value = 0, message = "Amount must be non-negative") // Анотація валідації: значення кількості повинно бути 0 або більше.
    @Column(nullable = false) // Відображення на колонку БД, яка не може містити NULL.
    private Integer amount;

    // Список зображень, пов'язаних з цим товаром.
    // Зв'язок "один до багатьох" з сутністю ProductImage.

    // Анотація для зв'язку "один (Product) до багатьох (ProductImage)".
    // mappedBy = "product": Вказує, що поле 'product' у класі ProductImage відповідає за цей зв'язок (зворотній бік).
    // cascade = CascadeType.ALL: Всі операції (PERSIST, MERGE, REMOVE, REFRESH, DETACH), що виконуються над Product, будуть каскадно застосовані і до пов'язаних ProductImage.
    // orphanRemoval = true: Якщо ProductImage видаляється з колекції 'images' (наприклад, images.remove(img)), то цей ProductImage буде також видалений з бази даних.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>(); // Ініціалізація списку для уникнення NullPointerException.

    // Конструктор за замовчуванням. Необхідний для JPA.
    public Product() {
    }

    // Конструктор з параметрами для створення об'єкта Product з усіма основними полями.
    public Product(Long id, String name, String description, Double price, Integer amount) { // [cite: 100]
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

    // Стандартні методи доступу (геттери та сеттери) для всіх полів класу
    // Дозволяють отримувати та встановлювати значення полів об'єкта.

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }
}