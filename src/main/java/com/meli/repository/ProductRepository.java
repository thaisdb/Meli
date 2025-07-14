package com.meli.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.model.Product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final File PRODUCT_PATH = new File("data/products.json");
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private List<Product> products;

    public ProductRepository() {
        products = loadProducts();
    }

    private List<Product> loadProducts() {
        try {
            if (PRODUCT_PATH.exists()) {
                return objectMapper.readValue(PRODUCT_PATH, new TypeReference<List<Product>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void saveProducts() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(PRODUCT_PATH, products);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAll() {
        return products;
    }

    public Product getById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public Product save(Product product) {
        int maxId = products.stream().mapToInt(Product::getId).max().orElse(0);
        product.setId(maxId + 1);
        products.add(product);
        saveProducts();
        return product;
    }

    public boolean update(int id, Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                updated.setId(id);
                products.set(i, updated);
                saveProducts();
                return true;
            }
        }
        return false;
    }

    public boolean deleteById(int id) {
        boolean removed = products.removeIf(p -> p.getId() == id);
        if (removed) saveProducts();
        return removed;
    }
}
