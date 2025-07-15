package com.meli.service;

import com.meli.model.Product;
import com.meli.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        System.out.println("BACKEND: ProductService: Initialized with ProductRepository.");
    }

// This method contains the core filtering logic
    public List<Product> getAllProducts(Optional<String> tagsFilter) {
        List<Product> allProducts = productRepository.getAll(); // Fetches all products initially

        if (tagsFilter.isPresent() && !tagsFilter.get().trim().isEmpty()) {
            // Process the incoming tags string (e.g., "smartphone apple")
            Set<String> searchTags = Arrays.stream(tagsFilter.get().split(" "))
                                           .map(String::trim)
                                           .filter(tag -> !tag.isEmpty())
                                           .map(String::toLowerCase)
                                           .collect(Collectors.toSet());

            if (searchTags.isEmpty()) {
                // If after splitting and cleaning, no valid tags remain, return all products
                return allProducts;
            }

            // Filter products based on whether any of their tags match the search tags
            return allProducts.stream()
                .filter(product -> {
                    if (product.getTags() == null || product.getTags().isEmpty()) {
                        return false; // Product has no tags, so it won't match
                    }
                    // Check if any of the product's tags are present in the searchTags set
                    return product.getTags().stream()
                                  .map(String::toLowerCase)
                                  .anyMatch(searchTags::contains);
                })
                .collect(Collectors.toList());
        }
        // If no tagsFilter is present or it's empty, return all products
        return allProducts;
    }

    public Product getProductById(int id) {
        System.out.println("BACKEND: ProductService.getProductById() called for ID: " + id);
        return productRepository.getById(id)
                                .orElseThrow(() -> {
                                    System.err.println("BACKEND: ProductService.getProductById(): Product ID " + id + " not found. Throwing 404.");
                                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + id + " not found");
                                });
    }

    public Product addProduct(Product product) {
        System.out.println("BACKEND: ProductService.addProduct() called for product: " + product.getTitle());
        int newId = productRepository.getAll().stream()
                                    .mapToInt(Product::getId)
                                    .max()
                                    .orElse(0) + 1;
        product.setId(newId);
        System.out.println("BACKEND: ProductService.addProduct(): Assigned new ID " + newId + " to product.");
        return productRepository.save(product);
    }

    public void deleteProductById(int id) {
        System.out.println("BACKEND: ProductService.deleteProductById() called for ID: " + id);
        boolean deleted = productRepository.deleteById(id);
        if (!deleted) {
            System.err.println("BACKEND: ProductService.deleteProductById(): Product ID " + id + " not found by repository. Throwing 404.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + id + " not found for deletion.");
        }
        System.out.println("BACKEND: ProductService.deleteProductById(): Product ID " + id + " successfully deleted.");
    }

    public Product updateProduct(int id, Product updatedProductData) {
        System.out.println("BACKEND: ProductService.updateProduct() called for ID: " + id);
        Product existingProduct = getProductById(id); // This will throw 404 if not found

        System.out.println("BACKEND: ProductService.updateProduct(): Found existing product for ID " + id + ". Merging data.");
        existingProduct.setTitle(updatedProductData.getTitle());
        existingProduct.setPrice(updatedProductData.getPrice());
        existingProduct.setDescription(updatedProductData.getDescription());
        existingProduct.setImageUrl(updatedProductData.getImageUrl());
        existingProduct.setBrand(updatedProductData.getBrand());
        existingProduct.setStock(updatedProductData.getStock());
        existingProduct.setTags(updatedProductData.getTags());

        boolean updated = productRepository.update(existingProduct);
        if (!updated) {
            System.err.println("BACKEND: ProductService.updateProduct(): Failed to persist update for ID " + id + " in repository. Throwing 500.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist product update for ID " + id);
        }
        System.out.println("BACKEND: ProductService.updateProduct(): Product ID " + id + " successfully updated.");
        return existingProduct;
    }

    public Product buyProduct(int id, int quantity) {
        System.out.println("BACKEND: ProductService.buyProduct() called for ID: " + id + ", Quantity: " + quantity);
        Product product = getProductById(id);

        if (quantity <= 0) {
            System.err.println("BACKEND: ProductService.buyProduct(): Invalid quantity " + quantity + ". Throwing IllegalArgumentException.");
            throw new IllegalArgumentException("buyProduct: Quantity must be positive.");
        }
        if (quantity > product.getStock()) {
            System.err.println("BACKEND: ProductService.buyProduct(): Insufficient stock for ID " + id + ". Available: " + product.getStock() + ", Requested: " + quantity + ". Throwing IllegalArgumentException.");
            throw new IllegalArgumentException("buyProduct: Insufficient stock. Available: " + product.getStock() + ", Requested: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        System.out.println("BACKEND: ProductService.buyProduct(): Updated stock for ID " + id + " to " + product.getStock());
        boolean updated = productRepository.update(product);
        if (!updated) {
            System.err.println("BACKEND: ProductService.buyProduct(): Failed to persist stock update for ID " + id + ". Throwing 500.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist stock update for ID " + id);
        }
        System.out.println("BACKEND: ProductService.buyProduct(): Stock updated and persisted for ID " + id + ".");
        return product;
    }
}
