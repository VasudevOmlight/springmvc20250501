package com.javarush.springmvc.entity;

import com.javarush.springmvc.entity.Product;
import jakarta.persistence.*;

// Клас-сутність (Entity), що представляє зображення товару в базі даних.
// Кожне зображення пов'язане з певним товаром (Product).

@Entity // Позначає, що цей клас є сутністю JPA і буде відображений на таблицю в базі даних.
@Table(name = "product_images") // Явно вказує ім'я таблиці в базі даних ("product_images").
public class ProductImage {

    // Унікальний ідентифікатор зображення (первинний ключ).

    @Id // Позначає поле 'id' як первинний ключ таблиці.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Значення ID генерується базою даних автоматично.
    private Long id;

    // Посилання на товар (Product), до якого належить це зображення.
    // Зв'язок "багато до одного" з сутністю Product.

    // Анотація для зв'язку "багато (ProductImage) до одного (Product)".
    // fetch = FetchType.LAZY: Вказує, що пов'язаний об'єкт Product не буде завантажуватися з бази даних
    // автоматично разом із ProductImage. Він буде завантажений тільки при прямому зверненні до поля 'product'.
    // Це покращує продуктивність, уникаючи завантаження непотрібних даних.
    @ManyToOne(fetch = FetchType.LAZY)
    // Анотація для налаштування колонки зовнішнього ключа в таблиці 'product_images'.
    // name = "product_id": Ім'я колонки в таблиці 'product_images', яка зберігає ID пов'язаного товару.
    // nullable = false: Ця колонка не може містити NULL, тобто кожне зображення повинно бути пов'язане з товаром.
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Поле для зберігання посилання на об'єкт Product.

    // Шлях до файлу зображення відносно статичних ресурсів.
    // Наприклад, "/images/iphone_1.jpg".

    // Відображення поля на колонку 'image_path'. Колонка не може бути NULL.
    @Column(name = "image_path", nullable = false) // [cite: 116]
    private String imagePath; // Поле для зберігання шляху до файлу.

    // Тип MIME зображення (наприклад, "image/jpeg", "image/png").

    // Відображення поля на колонку 'image_type'. Колонка не може бути NULL.
    @Column(name = "image_type", nullable = false) // [cite: 117]
    private String imageType; // Поле для зберігання MIME-типу.

    // Конструктор за замовчуванням. Необхідний для JPA.

    public ProductImage() {
    }

    // Конструктор з параметрами для створення об'єкта ProductImage.
    // @param imagePath Шлях до файлу зображення.
    // @param imageType Тип MIME зображення.
    // @param product   Товар, до якого належить зображення.

    public ProductImage(String imagePath, String imageType, Product product) { // [cite: 118]
        this.imagePath = imagePath;
        this.imageType = imageType;
        this.product = product;
    }

    // Стандартні методи доступу (геттери та сеттери) для всіх полів класу

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
}