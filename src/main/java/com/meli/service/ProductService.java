package com.meli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.meli.model.Product;

/*
 * Encapsulates business logic
 * Handles loading/saving products
 */
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

    /*
     * Get list of all products
     * @return list of products
     */
    public List<Product> getAll() {
        return products;
    }

    /*
     * Get product by Id
     * @param product unique Id
     * @return product which matches Id
     */
    public Product getProductById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    /*
     * Create new product generating newId automatically
     * @param new product to be created
     * @return created product
     */
    public Product addProduct(Product product) {
        int newId = products.stream()
                            .mapToInt(Product::getId)
                            .max()
                            .orElse(0) + 1;
        product.setId(newId);

        products.add(product);
        saveProducts();

        return product;
    }

    /*
     * Search and delete product by id
     * @param product unique id
     */
    public void deleteById(int id) {
        products.removeIf(p -> p.getId() == id);
        saveProducts();
    }

    /*
     * Update product by Id, replacing it with updatedProduct
     * Preserves original Id and calls saveProducts function
     * @param product unique Id
     * @param product updated
     * @return true if successefully update the product
     *         false if can't find product
     */
    public boolean updateProduct(int id, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                updatedProduct.setId(id);
                products.set(i, updatedProduct);
                saveProducts();
                return true;
            }
        }
        return false;
    }

    public Product buyProduct(int id, int quantity) {
        Product product = getProductById(id); 
        if (product == null) {
            throw new IllegalArgumentException("buyProduct: Produto não encontrado");
        }

        if (quantity <= 0 || quantity > product.getStock()) {
            throw new IllegalArgumentException("buyProduct: Quantidade inválida: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        saveProducts();
        return product;
    }
}
