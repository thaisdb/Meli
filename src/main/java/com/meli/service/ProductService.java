package com.meli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.meli.model.Product;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final String DATA_DIR = "data";
    private final String DATA_FILE_PATH = DATA_DIR + File.separator + "products.json";

    private List<Product> products = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductService() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                System.out.println("DEBUG: ProductService - Created data directory: " + dataDir.getAbsolutePath());
            } else {
                System.err.println("ERROR: ProductService - Failed to create data directory: " + dataDir.getAbsolutePath());
            }
        }
        loadProducts();
    }

    private void loadProducts() {
        File file = new File(DATA_FILE_PATH);
        System.out.println("DEBUG: ProductService.loadProducts - Attempting to load products from: " + file.getAbsolutePath());
        System.out.println("DEBUG: ProductService.loadProducts - File exists: " + file.exists());
        System.out.println("DEBUG: ProductService.loadProducts - File is readable: " + file.canRead());
        System.out.println("DEBUG: ProductService.loadProducts - File size (bytes): " + file.length());


        if (file.exists() && file.length() > 0) {
            try {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Product.class);
                products = mapper.readValue(file, listType);
                System.out.println("DEBUG: ProductService.loadProducts - Successfully loaded " + products.size() + " products.");
                if (products.isEmpty()) {
                    System.out.println("DEBUG: ProductService.loadProducts - Loaded list is empty. This might indicate malformed JSON or an empty array in the file.");
                } else {
                    System.out.println("DEBUG: ProductService.loadProducts - First product in list (for verification): " + products.get(0).getTitle() + " (ID: " + products.get(0).getId() + ")");
                }
            } catch (IOException e) {
                System.err.println("ERROR: ProductService.loadProducts - Failed to read or parse products.json: " + e.getMessage());
                e.printStackTrace();
                products = new ArrayList<>(); // Ensure it's an empty list if loading fails
            }
        } else {
            System.out.println("DEBUG: ProductService.loadProducts - products.json not found or is empty. Starting with an empty product list in memory.");
            products = new ArrayList<>(); // Explicitly ensure it's empty
        }
    }

    private synchronized void saveProducts() {
        File file = new File(DATA_FILE_PATH);
        System.out.println("DEBUG: ProductService.saveProducts - Attempting to save " + products.size() + " products to: " + file.getAbsolutePath());
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, products);
            System.out.println("DEBUG: ProductService.saveProducts - Products saved successfully.");
        } catch (IOException e) {
            System.err.println("ERROR: ProductService.saveProducts - Failed to save products to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Product> getAllProducts() {
        System.out.println("DEBUG: ProductService.getAllProducts - Returning " + products.size() + " products.");
        return new ArrayList<>(products);
    }

    public List<Product> getProductsBySellerId(int sellerId) {
        System.out.println("DEBUG: ProductService.getProductsBySellerId - Filtering for sellerId: " + sellerId);
        List<Product> filteredProducts = products.stream()
                       .filter(p -> p.getSellerId() == sellerId)
                       .collect(Collectors.toList());
        System.out.println("DEBUG: ProductService.getProductsBySellerId - Found " + filteredProducts.size() + " products for sellerId: " + sellerId);
        return filteredProducts;
    }

    public Product getProductById(int id) {
        System.out.println("DEBUG: ProductService.getProductById - Searching for product with ID: " + id);
        Optional<Product> foundProduct = products.stream().filter(p -> p.getId() == id).findFirst();
        if (foundProduct.isPresent()) {
            System.out.println("DEBUG: ProductService.getProductById - Found product: " + foundProduct.get().getTitle());
        } else {
            System.out.println("DEBUG: ProductService.getProductById - Product with ID " + id + " not found.");
        }
        return foundProduct.orElse(null);
    }

    public Product addProduct(Product product) {
        int newId = generateNextId();
        product.setId(newId);
        products.add(product);
        saveProducts();
        System.out.println("DEBUG: ProductService.addProduct - Added new product: " + product.getTitle() + " with ID: " + product.getId() + " for seller: " + product.getSellerId());
        return product;
    }

    public Product updateProduct(int id, Product updatedProduct) {
        System.out.println("DEBUG: ProductService.updateProduct - Attempting to update product with ID: " + id);
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                updatedProduct.setId(id);
                updatedProduct.setSellerId(products.get(i).getSellerId());

                products.set(i, updatedProduct);
                saveProducts();
                System.out.println("DEBUG: ProductService.updateProduct - Product " + id + " updated successfully.");
                return updatedProduct;
            }
        }
        System.out.println("DEBUG: ProductService.updateProduct - Product with ID " + id + " not found for update.");
        return null;
    }

    public boolean deleteProduct(int id) {
        System.out.println("DEBUG: ProductService.deleteProduct - Attempting to delete product with ID: " + id);
        boolean removed = products.removeIf(p -> p.getId() == id);
        if (removed) {
            saveProducts();
            System.out.println("DEBUG: ProductService.deleteProduct - Product with ID " + id + " removed successfully.");
        } else {
            System.out.println("DEBUG: ProductService.deleteProduct - Product with ID " + id + " not found for deletion.");
        }
        return removed;
    }

    private int generateNextId() {
        return products.stream()
                .mapToInt(Product::getId)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Busca produtos por termo de pesquisa (título, descrição, categoria, marca).
     * @param searchTerm O termo de pesquisa.
     * @return Uma lista de produtos que correspondem ao termo.
     */
    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("DEBUG: ProductService.searchProducts - No search term provided. Returning all products.");
            return getAllProducts(); // Retorna todos os produtos se o termo de busca for vazio
        }

        String normalizedSearchTerm = searchTerm.trim().toLowerCase();
        List<Product> allProducts = new ArrayList<>(products); // Trabalha com uma cópia
        List<Product> filteredProducts = new ArrayList<>();

        for (Product product : allProducts) {
            boolean matches = false;
            // Busca por título
            if (product.getTitle() != null && product.getTitle().toLowerCase().contains(normalizedSearchTerm)) {
                matches = true;
            }
            // Busca por descrição
            if (!matches && product.getDescription() != null && product.getDescription().toLowerCase().contains(normalizedSearchTerm)) {
                matches = true;
            }
            // Busca por categoria (AGORA INCLUI CATEGORIA NA BUSCA)
            if (!matches && product.getCategory() != null && product.getCategory().toLowerCase().contains(normalizedSearchTerm)) {
                matches = true;
            }
            // Busca por marca
            if (!matches && product.getBrand() != null && product.getBrand().toLowerCase().contains(normalizedSearchTerm)) {
                matches = true;
            }

            if (matches) {
                filteredProducts.add(product);
            }
        }
        System.out.println("DEBUG: ProductService.searchProducts - Found " + filteredProducts.size() + " products for search term: '" + searchTerm + "'");
        return filteredProducts;
    }
}
