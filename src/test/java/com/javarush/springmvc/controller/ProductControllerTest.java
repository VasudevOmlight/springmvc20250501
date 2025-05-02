package com.javarush.springmvc.controller;

import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.service.ProductService;
import com.javarush.springmvc.service.operation.NewProductSaver;
import com.javarush.springmvc.service.operation.ProductEditor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // Імпорт для виключення автоконфігурації безпеки
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.isNull; // Імпортуємо isNull для відповідності null аргументу
import static org.mockito.ArgumentMatchers.eq; // Імпортуємо eq для відповідності конкретним значенням

// @WebMvcTest завантажує мінімальний контекст Spring, потрібний для тестування контролерів
// Вказуємо контролер, який тестуємо
// Особливість: excludeAutoConfiguration для виключення автоматичного налаштування безпеки
@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc; // Об'єкт для імітації HTTP-запитів

    // Замінюємо реальні біни сервісів на мок-об'єкти
    @MockBean
    private ProductService productService;

    @MockBean
    private NewProductSaver newProductSaver;

    @MockBean
    private ProductEditor productEditor;


    @Test
    void listProducts_ShouldReturnProductListView() throws Exception {
        // Створюємо тестові дані, які поверне мок-сервіс
        List<Product> mockProducts = Arrays.asList(
                new Product(1L, "Mock Product 1", "Mock Desc 1", 10.0, 1),
                new Product(2L, "Mock Product 2", "Mock Desc 2", 20.0, 2)
        );

        // Налаштовуємо поведінку мок-сервісу так, щоб вона спрацьовувала для виклику з аргументами (null, "name", "asc")
        // Використовуємо isNull() для відповідності null та eq() для відповідності конкретним значенням
        when(productService.searchAndSortProducts(isNull(), eq("name"), eq("asc"))).thenReturn(mockProducts);


        // Виконуємо імітований GET-запит до "/products"
        mockMvc.perform(get("/products"))
                // Перевіряємо статус відповіді (очікуємо 200 OK)
                .andExpect(status().isOk())
                // Перевіряємо, що повернуто правильне ім'я шаблону (view)
                .andExpect(view().name("products/list"))
                // Перевіряємо, що модель містить атрибут "products"
                .andExpect(model().attributeExists("products"))
                // Перевіряємо, що атрибут "products" в моделі дорівнює нашому списку mockProducts
                // Тепер цей асерт має спрацювати, оскільки мок поверне mockProducts
                .andExpect(model().attribute("products", mockProducts));

        // Перевіряємо, що метод сервісу був викликаний один раз з правильними аргументами
        verify(productService, times(1)).searchAndSortProducts(isNull(), eq("name"), eq("asc"));

    }

    @Test
    void viewProduct_ShouldReturnProductViewWhenProductExists() throws Exception {
        Long productId = 1L;
        Product mockProduct = new Product(productId, "View Product", "View Desc", 100.0, 10);

        // Налаштовуємо мок-сервіс: при пошуку за певним ID повертаємо тестовий продукт
        when(productService.findById(productId)).thenReturn(mockProduct);

        // Виконуємо GET-запит до "/products/view/{id}", додаємо параметр language для тестування i18n
        // Хоча для цього тесту i18n не є критичним, це показує, як додати параметри запиту
        mockMvc.perform(get("/products/view/{id}", productId).param("language", "en"))
                // Тепер очікуємо статус 200 OK
                .andExpect(status().isOk())
                .andExpect(view().name("products/view"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", mockProduct));

        // Перевіряємо, що метод сервісу був викликаний з правильним ID
        verify(productService, times(1)).findById(productId);
    }

    @Test
    void viewProduct_ShouldRedirectWhenProductNotFound() throws Exception {
        Long productId = 99L; // ID, якого немає

        // Налаштовуємо мок-сервіс: при пошуку за цим ID повертаємо null
        when(productService.findById(productId)).thenReturn(null);

        // Виконуємо GET-запит до "/products/view/{id}"
        mockMvc.perform(get("/products/view/{id}", productId))
                .andExpect(status().is3xxRedirection()) // Перевіряємо, що відбувається редирект (3xx статус)
                .andExpect(redirectedUrl("/products")); // Перевіряємо URL редиректу

        // Перевіряємо, що метод сервісу був викликаний
        verify(productService, times(1)).findById(productId);
    }


    // Тест для deleteProduct
    @Test
    void deleteProduct_ShouldDeleteProductAndRedirect() throws Exception {
        Long productId = 1L;

        // Налаштовуємо мок-сервіс: нічого не повертаємо при виклику deleteById
        doNothing().when(productService).deleteById(productId);

        // Виконуємо GET-запит до "/products/delete/{id}"
        mockMvc.perform(get("/products/delete/{id}", productId))
                .andExpect(status().is3xxRedirection()) // Перевіряємо, що відбувається редирект
                .andExpect(redirectedUrl("/products")); // Перевіряємо URL редиректу

        // Перевіряємо, що метод сервісу був викликаний один раз з правильним ID
        verify(productService, times(1)).deleteById(productId);
    }

}

















































// Приклади тестування методів POST з @Valid та MultipartFile (@PostMapping("/add"), @PostMapping("/edit/{id}"))
// є більш складними, але з вимкненою безпекою це стає простіше.
// Потрібно імітувати відправку відповідного типу запиту (POST)
// та передавати параметри форми, включаючи файли для multipart запитів.

    /*
     @Test
     void addProductForm_ShouldReturnAddView() throws Exception {
         mockMvc.perform(get("/products/add"))
                 .andExpect(status().isOk())
                 .andExpect(view().name("products/add"))
                 .andExpect(model().attributeExists("product")); // Перевіряємо наявність порожнього об'єкта Product у моделі
     }

     @Test
     void editProductForm_ShouldReturnEditViewWhenProductExists() throws Exception {
         Long productId = 1L;
         Product mockProduct = new Product(productId, "Edit Product", "Edit Desc", 200.0, 20);

         when(productService.findById(productId)).thenReturn(mockProduct);

         mockMvc.perform(get("/products/edit/{id}", productId))
                 .andExpect(status().isOk())
                 .andExpect(view().name("products/edit"))
                 .andExpect(model().attributeExists("product"))
                 .andExpect(model().attribute("product", mockProduct));

         verify(productService, times(1)).findById(productId);
     }

     // Додайте тест для editProductForm, коли товар не знайдено (має бути редирект на /products)

    */