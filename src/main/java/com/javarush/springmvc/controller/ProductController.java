package com.javarush.springmvc.controller;

import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.service.ProductService;
import com.javarush.springmvc.service.operation.NewProductSaver;
import com.javarush.springmvc.service.operation.ProductEditor;
import jakarta.validation.Valid; // Імпорт для анотації @Valid (валідація бінів)
import org.slf4j.Logger; // Інтерфейс логера SLF4J
import org.slf4j.LoggerFactory; // Фабрика для створення логерів SLF4J
import org.springframework.beans.factory.annotation.Autowired; // Для впровадження залежностей
import org.springframework.stereotype.Controller; // Позначає клас як Spring MVC контролер
import org.springframework.ui.Model; // Інтерфейс для передачі даних у представлення (view)
import org.springframework.validation.BindingResult; // Інтерфейс для зберігання результатів валідації
import org.springframework.web.bind.annotation.*; // Анотації для мапінгу веб-запитів (GetMapping, PostMapping, PathVariable, RequestParam, etc.)
import org.springframework.web.multipart.MultipartFile; // Інтерфейс для обробки завантажених файлів
import java.io.IOException;
import java.util.List;

// Контролер для обробки HTTP-запитів, пов'язаних з товарами (Product).
// Відповідає за відображення сторінок списку, перегляду, додавання, редагування товарів,
// а також за обробку даних форм та взаємодію з сервісним шаром.
@Controller // Позначаємо клас як контролер Spring MVC
@RequestMapping("/products") // Всі запити до цього контролера починатимуться з "/products"
public class ProductController {

    // Створюємо екземпляр логера для цього класу
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    // Впроваджуємо залежність: сервіс для роботи з товарами
    @Autowired
    private ProductService productService;

    // Впроваджуємо залежність: сервіс для збереження нових товарів
    @Autowired
    private NewProductSaver newProductSaver;

    // Впроваджуємо залежність: сервіс для редагування існуючих товарів
    @Autowired
    private ProductEditor productEditor;

    // Обробляє GET-запити до "/products".
    // Відображає список товарів з можливістю пошуку та сортування.

    @GetMapping // Обробка GET-запитів на "/products"
    public String listProducts(Model model,
                               @RequestParam(required = false) String search, // Параметр запиту "?search=..." (необов'язковий)
                               @RequestParam(defaultValue = "name") String sortBy, // Параметр запиту "?sortBy=..." (за замовчуванням "name")
                               @RequestParam(defaultValue = "asc") String sortDir) { // Параметр запиту "?sortDir=..." (за замовчуванням "asc")

        // Логуємо отримані параметри пошуку та сортування
        logger.info("Listing products with server search: {}, sortBy: {}, sortDir: {}", search, sortBy, sortDir);

        // Викликаємо сервіс для отримання списку товарів з урахуванням пошуку та сортування
        List<Product> products = productService.searchAndSortProducts(search, sortBy, sortDir);

        // Додаємо отриманий список товарів до моделі під іменем "products"
        model.addAttribute("products", products);
        // Додаємо рядок пошуку до моделі для відображення у полі пошуку на сторінці
        model.addAttribute("search", search);
        // Додаємо параметри сортування до моделі (для формування посилань сортування)
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Повертаємо логічне ім'я представлення (Thymeleaf шаблон)
        return "products/list";
    }

    // Обробляє GET-запити до "/products/view/{id}".
    // Відображає детальну інформацію про один товар.
    @GetMapping("/view/{id}") // Обробка GET-запитів на "/products/view/1", "/products/view/5" і т.д.
    public String viewProduct(@PathVariable Long id, Model model) { // @PathVariable отримує {id} з URL
        logger.info("Viewing product with ID: {}", id); // Логуємо ID товару, який переглядається

        // Знаходимо товар за ID за допомогою сервісу
        Product product = productService.findById(id);

        // Перевіряємо, чи знайдено товар
        if (product == null) {
            logger.warn("Product not found with ID: {}", id); // Логуємо попередження, якщо товар не знайдено
            return "redirect:/products"; // Перенаправляємо користувача на сторінку списку товарів
        }

        // Додаємо знайдений товар до моделі під іменем "product"
        model.addAttribute("product", product);
        // Повертаємо логічне ім'я представлення для відображення деталей
        return "products/view";
    }

    // Обробляє GET-запити до "/products/add".
    // Відображає форму для додавання нового товару.
    @GetMapping("/add") // Обробка GET-запитів на "/products/add"
    public String addProductForm(Model model) {
        logger.info("Showing add product form"); // Логуємо дію показу форми
        // Додаємо до моделі новий порожній об'єкт Product.
        // Це потрібно для прив'язки полів форми Thymeleaf (th:object="${product}")
        model.addAttribute("product", new Product());
        // Повертаємо логічне ім'я представлення з формою додавання
        return "products/add";
    }

    // Обробляє POST-запити до "/products/add".
    // Приймає дані з форми додавання, валідує їх, зберігає новий товар та зображення.
    public String addProduct(@Valid @ModelAttribute Product product, // @Valid - валідувати об'єкт, @ModelAttribute - зв'язати з даними форми
                             BindingResult result, // Результати валідації для product
                             @RequestParam("imageFiles") List<MultipartFile> imageFiles, // Отримати файли з поля "imageFiles"
                             Model model) {
        logger.info("Attempting to add product: {}", product.getName()); // Логуємо спробу додати товар

        // Перевіряємо, чи є помилки валідації
        if (result.hasErrors()) {
            logger.warn("Validation errors during add: {}", result.getAllErrors()); // Логуємо помилки валідації
            // Якщо є помилки, повертаємо користувача на ту ж саму форму додавання.
            // Об'єкт product з помилками автоматично передається назад у модель,
            // Thymeleaf відобразить помилки поруч з відповідними полями.
            return "products/add";
        }

        // Спроба зберегти товар та зображення
        try {
            // Викликаємо сервіс для збереження нового товару та переданих зображень
            Product savedProduct = newProductSaver.saveNewProduct(product, imageFiles);
            logger.info("Product saved: {}", savedProduct.getName()); // Логуємо успішне збереження
            // Додаємо повідомлення про успіх до моделі для відображення на сторінці
            model.addAttribute("message", "Товар " + savedProduct.getName() + " додано успішно!");
            // Очищаємо форму, додаючи новий порожній об'єкт Product до моделі
            model.addAttribute("product", new Product());
            // Залишаємо користувача на сторінці додавання (для можливості додати ще один товар)
            return "products/add";
        } catch (IOException e) {

            logger.error("Error saving product images for product: {}", product.getName(), e);
            // Додаємо повідомлення про помилку до моделі
            model.addAttribute("error", "Помилка збереження зображень товару.");
            // Повертаємо введені користувачем дані назад у форму
            model.addAttribute("product", product);
            // Повертаємо користувача на форму додавання
            return "products/add";
        } catch (IllegalArgumentException e) {
            // Обробка інших помилок під час збереження (наприклад, передано null product)
            logger.error("Error saving product: {}", e.getMessage());
            // Додаємо повідомлення про помилку до моделі
            model.addAttribute("error", e.getMessage());
            // Повертаємо введені користувачем дані назад у форму
            model.addAttribute("product", product);
            // Повертаємо користувача на форму додавання
            return "products/add";
        }
    }

    // Обробляє GET-запити до "/products/edit/{id}".
    // Відображає форму для редагування існуючого товару.
    @GetMapping("/edit/{id}") // Обробка GET-запитів на "/products/edit/1", "/products/edit/5" і т.д.
    public String editProductForm(@PathVariable Long id, Model model) { // @PathVariable отримує {id} з URL
        logger.info("Showing edit form for product ID: {}", id); // Логуємо показ форми редагування

        // Знаходимо товар за ID, який потрібно редагувати
        Product product = productService.findById(id);

        // Перевіряємо, чи знайдено товар
        if (product == null) {
            logger.warn("Product not found with ID: {}", id); // Логуємо попередження
            return "redirect:/products"; // Перенаправляємо на список
        }

        // Додаємо знайдений товар до моделі під іменем "product" для заповнення форми
        model.addAttribute("product", product);
        // Повертаємо логічне ім'я представлення з формою редагування
        return "products/edit";
    }

    // Обробляє POST-запити до "/products/edit/{id}".
    // Приймає оновлені дані з форми редагування, валідує їх, оновлює товар та додає нові зображення.
    @PostMapping("/edit/{id}") // Обробка POST-запитів на "/products/edit/1", "/products/edit/5" і т.д.
    public String editProduct(@PathVariable Long id, // @PathVariable отримує {id} з URL
                              @Valid @ModelAttribute Product product, // Валідувати оновлені дані з форми
                              BindingResult result, // Результати валідації
                              @RequestParam("imageFiles") List<MultipartFile> imageFiles, // Нові файли зображень
                              Model model) {
        logger.info("Attempting to edit product ID: {}", id); // Логуємо спробу редагування

        //  Встановлюємо ID для об'єкта product, отриманого з форми.
        // Це потрібно, щоб Spring розумів, який саме запис оновлювати,
        // і щоб об'єкт product був повним при поверненні на форму у разі помилки.
        product.setId(id);

        // Перевіряємо наявність помилок валідації
        if (result.hasErrors()) {
            logger.warn("Validation errors during edit: {}", result.getAllErrors()); // Логуємо помилки
            // При помилках валідації повертаємо на сторінку редагування.
            // Щоб не втратити вже існуючі зображення, отримуємо актуальний стан товару з БД
            Product currentData = productService.findById(id);
            // Якщо товар існує, копіюємо його поточні зображення до об'єкта product, який буде переданий у модель
            if(currentData != null) {
                product.setImages(currentData.getImages());
            }
            // Передаємо об'єкт product (з введеними даними та поточними зображеннями) назад у модель
            model.addAttribute("product", product);
            // Повертаємо ім'я шаблону редагування
            return "products/edit";
        }

        // Спроба оновити товар та додати нові зображення
        try {
            // Викликаємо сервіс для редагування товару
            productEditor.editProduct(id, product, imageFiles);
            logger.info("Product updated with ID: {}", id); // Логуємо успішне оновлення
            // У разі успіху перенаправляємо користувача на сторінку списку товарів
            return "redirect:/products";
        } catch (IOException e) {
            // Обробка помилки під час запису нових файлів зображень
            logger.error("Error saving product images for product ID: {}", id, e);
            model.addAttribute("error", "Помилка збереження зображень товару.");
            // Як і у випадку помилки валідації, зберігаємо поточні зображення
            Product currentData = productService.findById(id);
            if(currentData != null) {
                product.setImages(currentData.getImages());
            }
            model.addAttribute("product", product); // Передаємо об'єкт з введеними даними та зображеннями
            return "products/edit"; // Повертаємо на форму редагування
        } catch (IllegalArgumentException e) {
            // Обробка інших помилок (наприклад, товар не знайдено при спробі редагування в сервісі)
            logger.error("Error editing product: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            // Зберігаємо поточні зображення
            Product currentData = productService.findById(id);
            if(currentData != null) {
                product.setImages(currentData.getImages());
            }
            model.addAttribute("product", product); // Передаємо об'єкт з введеними даними та зображеннями
            return "products/edit"; // Повертаємо на форму редагування
        }
    }

    // Обробляє GET-запити до "/products/delete/{id}".
    // Видаляє товар за вказаним ID.
    @GetMapping("/delete/{id}") // Обробка GET-запитів на "/products/delete/1", "/products/delete/5" і т.д.
    public String deleteProduct(@PathVariable Long id) { // @PathVariable отримує {id} з URL
        logger.info("Deleting product with ID: {}", id); // Логуємо видалення
        // Викликаємо сервіс для видалення товару за ID
        productService.deleteById(id);
        // Перенаправляємо користувача на сторінку списку товарів
        return "redirect:/products";
    }


    // Обробляє GET-запити до "/products/delete-image/{imageId}".
    // Видаляє конкретне зображення товару.
    @GetMapping("/delete-image/{imageId}") // Обробка GET-запитів типу "/products/delete-image/12"
    public String deleteImage(@PathVariable Long imageId, // ID зображення з URL
                              @RequestParam Long productId) { // ID товару з параметра запиту "?productId=..."
        logger.info("Deleting image with ID: {} for product ID: {}", imageId, productId); // Логуємо видалення зображення
        // Викликаємо сервіс для видалення зображення за його ID
        productService.deleteImageById(imageId);
        // Перенаправляємо користувача назад на сторінку редагування того товару, якому належало зображення
        return "redirect:/products/edit/" + productId;
    }
}