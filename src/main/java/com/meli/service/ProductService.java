// encapsulates business logic
// handles loading/saving products
package com.meli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.meli.model.Product;
import com.meli.repository.ProductRepository;

@Service
public class ProductService {
    private final String DATA_FILE_PATH = "data/products.json";
    private List<Product> products = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductService() {
        loadProducts();
    }

    private void loadProducts() {
        System.out.println("Trying to load from: " + new File(DATA_FILE_PATH).getAbsolutePath());
        try {
            File file = new File(DATA_FILE_PATH);
            if (file.exists()) {
                Product[] productArray = mapper.readValue(file, Product[].class);
                products.clear();
                products = new ArrayList<>(Arrays.asList(productArray));
            } else {
                System.out.println("No product file found. Starting with empty list.");
                products = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Failed to load products from file: " + e.getMessage());
        }
    }

    private void saveProducts() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE_PATH), products);
        } catch (IOException e) {
            System.err.println("Failed to save products to file: " + e.getMessage());
        }
    }

    public List<Product> getAll() {
        return products;
    }

    public Product getProductById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public Product addProduct(Product product) {
        // Auto-generate a new ID (simple version)
        int newId = products.stream()
                            .mapToInt(Product::getId)
                            .max()
                            .orElse(0) + 1;
        product.setId(newId);

        products.add(product);
        saveProducts();

        return product;
    }

    public void deleteById(int id) {
        products.removeIf(p -> p.getId() == id);
        saveProducts();
    }

    public boolean updateProduct(int id, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                updatedProduct.setId(id); // preserve original ID
                products.set(i, updatedProduct);
                saveProducts();
                return true;
            }
        }
        return false;
    }
}
