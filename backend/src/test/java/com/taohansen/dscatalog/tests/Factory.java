package com.taohansen.dscatalog.tests;

import com.taohansen.dscatalog.dto.ProductDTO;
import com.taohansen.dscatalog.entities.Category;
import com.taohansen.dscatalog.entities.Product;

import java.time.Instant;

public class Factory {
    public static Product createProduct() {
        Product product = new Product(1L, "Phone", "Good Phone", 800.0, "https://img.com/img.png", Instant.parse("2023-12-03T10:15:30.00Z" ));
        product.getCategories().add(new Category(1L, "Electronics"));
        return product;
    }

    public static ProductDTO createProductDTO() {
        Product product = createProduct();
        return new ProductDTO(product, product.getCategories());
    }

    public static ProductDTO createProductDTO(String name) {
        Product product = createProduct();
        product.setName(name);
        return new ProductDTO(product, product.getCategories());
    }
}
