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
public class NewProductSaver {

    private static final Logger logger = LoggerFactory.getLogger(NewProductSaver.class);

    private final ProductRepository productRepository;

    public NewProductSaver(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product saveNewProduct(Product product, List<MultipartFile> images) throws IOException {
        if (product == null) {
            logger.error("Product is null");
            throw new IllegalArgumentException("Product cannot be null");
        }
        logger.info("Saving new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        if (images != null && !images.isEmpty()) {
            logger.info("Processing {} images for product: {}", images.size(), product.getName());
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty() && (image.getContentType().equals("image/jpeg") || image.getContentType().equals("image/png"))) {
                    String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                    Path filePath = Paths.get("src/main/resources/static/images/" + fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, image.getBytes());
                    ProductImage productImage = new ProductImage("/images/" + fileName, image.getContentType(), savedProduct);
                    savedProduct.getImages().add(productImage);
                    logger.info("Added image: {}", fileName);
                }
            }
            savedProduct = productRepository.save(savedProduct);
        }
        logger.info("New product saved with ID: {}", savedProduct.getId());
        return savedProduct;
    }
}