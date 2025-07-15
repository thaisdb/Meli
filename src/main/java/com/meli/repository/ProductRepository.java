package com.meli.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.meli.model.Product;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // Added for more detailed list logging

@Repository
public class ProductRepository {

    private final File PRODUCT_PATH = new File("data/products.json");
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    private List<Product> products;

    public ProductRepository() {
        System.out.println("BACKEND: ProductRepository: --- Initializing Repository ---");
        if (!PRODUCT_PATH.getParentFile().exists()) {
            PRODUCT_PATH.getParentFile().mkdirs();
            System.out.println("BACKEND: ProductRepository: Created data directory: " + PRODUCT_PATH.getParentFile().getAbsolutePath());
        }
        products = loadProducts();
        if (products.isEmpty() && !PRODUCT_PATH.exists()) {
            System.out.println("BACKEND: ProductRepository: products list is empty and file does not exist. Saving empty list to create file.");
            saveProducts();
            System.out.println("BACKEND: ProductRepository: Initialized empty products.json file.");
        }
        System.out.println("BACKEND: ProductRepository: --- Initialization complete. In-memory products count: " + products.size() + " ---");
    }

    private List<Product> loadProducts() {
        System.out.println("BACKEND: ProductRepository: Attempting to load products from: " + PRODUCT_PATH.getAbsolutePath());
        List<Product> loadedProducts = new ArrayList<>();
        Set<Integer> usedIds = new HashSet<>();
        int nextId = 1;

        try {
            if (PRODUCT_PATH.exists()) {
                String fileContent = new String(Files.readAllBytes(Paths.get(PRODUCT_PATH.getAbsolutePath())));
                System.out.println("BACKEND: ProductRepository: Raw file content before deserialization:\n" + fileContent);

                if (fileContent.trim().isEmpty() || fileContent.trim().equals("[]")) {
                    System.out.println("BACKEND: ProductRepository: products.json is empty or contains only an empty array. Returning empty list.");
                    return new ArrayList<>();
                }

                List<Product> rawProducts = new ArrayList<>();
                try {
                    rawProducts = objectMapper.readValue(fileContent, new TypeReference<List<Product>>() {});
                    System.out.println("BACKEND: ProductRepository: Successfully deserialized " + rawProducts.size() + " raw products from file.");
                } catch (JsonMappingException e) {
                    System.err.println("BACKEND: ProductRepository: JSON MAPPING ERROR during deserialization: " + e.getMessage());
                    System.err.println("BACKEND: ProductRepository: This often means a field type mismatch or malformed JSON within an object.");
                    e.printStackTrace();
                    return new ArrayList<>();
                }

                boolean idsCorrected = false;
                for (Product product : rawProducts) {
                    if (product.getId() == 0 || usedIds.contains(product.getId())) {
                        while (usedIds.contains(nextId)) {
                            nextId++;
                        }
                        System.out.println("BACKEND: ProductRepository: Correcting ID for product '" + product.getTitle() + "'. Old ID: " + product.getId() + ", New ID: " + nextId);
                        product.setId(nextId);
                        idsCorrected = true;
                    }
                    usedIds.add(product.getId());
                    loadedProducts.add(product);
                    nextId = Math.max(nextId, product.getId() + 1);
                }
                System.out.println("BACKEND: ProductRepository: Processed " + loadedProducts.size() + " products with unique IDs.");

                if (idsCorrected) {
                    System.out.println("BACKEND: ProductRepository: IDs were corrected during load. Saving cleaned data back to file.");
                    this.products = loadedProducts;
                    saveProducts();
                }

                return loadedProducts;
            } else {
                System.out.println("BACKEND: ProductRepository: products.json does not exist. Returning an empty list.");
            }
        } catch (IOException e) {
            System.err.println("BACKEND: ProductRepository: GENERAL I/O ERROR loading products from JSON file: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void saveProducts() {
        System.out.println("BACKEND: ProductRepository: Attempting to save " + products.size() + " products to: " + PRODUCT_PATH.getAbsolutePath());
        // Log product IDs and titles for verification
        System.out.println("BACKEND: ProductRepository: In-memory products list BEFORE saving (IDs): " + products.stream().map(Product::getId).collect(Collectors.toList()));
        try {
            objectMapper.writeValue(PRODUCT_PATH, products);
            System.out.println("BACKEND: ProductRepository: Products saved successfully. Total products: " + products.size());
        } catch (IOException e) {
            System.err.println("BACKEND: ProductRepository: ERROR saving products to JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Product> getAll() {
        System.out.println("BACKEND: ProductRepository.getAll() called. Returning " + products.size() + " products from in-memory list.");
        return new ArrayList<>(products);
    }

    public Optional<Product> getById(int id) {
        System.out.println("BACKEND: ProductRepository.getById() called for ID: " + id);
        Optional<Product> foundProduct = products.stream().filter(p -> p.getId() == id).findFirst();
        System.out.println("BACKEND: ProductRepository.getById(): Product " + (foundProduct.isPresent() ? "found" : "NOT found") + " for ID " + id);
        return foundProduct;
    }

    public Product save(Product product) {
        System.out.println("BACKEND: ProductRepository.save() called for new product: " + product.getTitle());
        int maxId = products.stream().mapToInt(Product::getId).max().orElse(0);
        product.setId(maxId + 1);
        System.out.println("BACKEND: ProductRepository.save(): Assigned new ID " + product.getId() + " to product '" + product.getTitle() + "'");

        products.add(product);
        saveProducts();
        return product;
    }

    public boolean update(Product productToUpdate) {
        System.out.println("BACKEND: ProductRepository.update() called for product ID: " + productToUpdate.getId());
        boolean updated = false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == productToUpdate.getId()) {
                products.set(i, productToUpdate);
                updated = true;
                break;
            }
        }
        if (updated) {
            saveProducts();
            System.out.println("BACKEND: ProductRepository: Product ID " + productToUpdate.getId() + " updated in memory and file saved.");
        } else {
            System.err.println("BACKEND: ProductRepository: Product ID " + productToUpdate.getId() + " not found for update in repository. No save performed.");
        }
        return updated;
    }

    public boolean deleteById(int id) {
        System.out.println("BACKEND: ProductRepository.deleteById() called for ID: " + id);
        System.out.println("BACKEND: ProductRepository.deleteById(): Current in-memory product IDs before removal attempt: " + products.stream().map(Product::getId).collect(Collectors.toList()));

        // Find the product to log its details before attempting removal
        Optional<Product> productToDeleteOptional = products.stream().filter(p -> p.getId() == id).findFirst();
        if (productToDeleteOptional.isPresent()) {
            Product productToDelete = productToDeleteOptional.get();
            System.out.println("BACKEND: ProductRepository.deleteById(): Product '" + productToDelete.getTitle() + "' (ID: " + id + ") found for deletion in memory.");

            boolean removed = products.removeIf(p -> p.getId() == id);
            if (removed) {
                System.out.println("BACKEND: ProductRepository.deleteById(): Product ID " + id + " successfully removed from in-memory list.");
                System.out.println("BACKEND: ProductRepository.deleteById(): In-memory product IDs AFTER removal: " + products.stream().map(Product::getId).collect(Collectors.toList()));
                saveProducts();
                System.out.println("BACKEND: ProductRepository: Product ID " + id + " deleted from repository and file saved.");
            } else {
                // This case should ideally not be hit if productToDeleteOptional.isPresent() is true
                System.err.println("BACKEND: ProductRepository: ERROR: Product ID " + id + " was found but failed to remove from list via removeIf.");
            }
            return removed;
        } else {
            System.out.println("BACKEND: ProductRepository: Product ID " + id + " NOT found for deletion in memory. No removal or save performed.");
            return false;
        }
    }
}
