// handles loading/saving products
package com.meli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.meli.model.Product;
import com.meli.repository.ProductRepository;

@Service
public class ProductService {
    private final String FILE_PATH = "src/main/resources/products.json";
    private List<Product> products = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductService() {
        loadProducts();
    }

    private void loadProducts() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                products = mapper.readValue(file, new TypeReference<List<Product>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveProducts() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), products);
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

    public void addProduct(Product product) {
        products.add(product);
        saveProducts();
    }

    public void deleteById(int id) {
        products.removeIf(p -> p.getId() == id);
        saveProducts();
    }
}
