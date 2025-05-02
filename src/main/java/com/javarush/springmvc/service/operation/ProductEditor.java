package com.javarush.springmvc.service.operation;

import com.javarush.springmvc.entity.Product;
import com.javarush.springmvc.entity.ProductImage;
import com.javarush.springmvc.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ProductEditor {
// Клас оновлює існуючий товар за ID.
// Копіює поля з `updatedProduct` до `existingProduct`.
// Додає нові зображення, якщо вони надані.
// Використовує логування для відстеження дій.
// Перевірка `if (updatedProduct == null)`.
// Перевірка `image != null`.
// Логування для відстеження помилок.

    private static final Logger logger = LoggerFactory.getLogger(ProductEditor.class);

    private final ProductRepository productRepository;

    public ProductEditor(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product editProduct(Long id, Product updatedProduct, List<MultipartFile> images) throws IOException {
        if (updatedProduct == null) {
            logger.error("Updated product is null");
            throw new IllegalArgumentException("Updated product cannot be null");
        }
        logger.info("Editing product with ID: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new IllegalArgumentException("Product not found with ID: " + id);
                });

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setAmount(updatedProduct.getAmount());

        if (images != null && !images.isEmpty()) {
            logger.info("Processing {} new images for product: {}", images.size(), existingProduct.getName());
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty() && (image.getContentType().equals("image/jpeg") || image.getContentType().equals("image/png"))) {
                    String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                    Path filePath = Paths.get("src/main/resources/static/images/" + fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, image.getBytes());
                    ProductImage productImage = new ProductImage("/images/" + fileName, image.getContentType(), existingProduct);
                    existingProduct.getImages().add(productImage);
                    logger.info("Added image: {}", fileName);
                }
            }
        }
        Product savedProduct = productRepository.save(existingProduct);
        logger.info("Product updated with ID: {}", savedProduct.getId());
        return savedProduct;
    }
}