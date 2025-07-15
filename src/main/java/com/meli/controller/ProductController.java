package com.meli.controller;

import com.meli.model.Product;
import com.meli.service.ProductService;
import com.meli.dto.BuyRequestDTO; // Agora com o campo 'id'
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/products") // Base path for all methods in this controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get ALL products.
     * GET /products
     * This endpoint is for general viewing (e.g., home page).
     */
    @GetMapping // Maps to /products
    public ResponseEntity<List<Product>> getAllProducts() {
        System.out.println("DEBUG: ProductController - Fetching all products.");
        List<Product> products = productService.getAllProducts();
        products.removeIf(p -> p.getStock().equals(0));
        return ResponseEntity.ok(products);
    }

    /**
     * Get products belonging to a specific seller.
     * GET /products/seller/{sellerId}
     */
    @GetMapping("/seller/{sellerId}") // Correctly mapped to /products/seller/{sellerId}
    public ResponseEntity<List<Product>> getProductsBySeller(@PathVariable int sellerId) {
        System.out.println("DEBUG: ProductController - Fetching products for sellerId: " + sellerId);
        List<Product> products = productService.getProductsBySellerId(sellerId);
        return ResponseEntity.ok(products);
    }

    /**
     * Get a single product by ID (no seller scope here, for general product viewing if needed)
     * This can be used by consumers or for internal lookups.
     * GET /products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Add a new product.
     * POST /products
     * Requires X-User-Id header for validation.
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product,
                                        @RequestHeader("X-User-Id") int loggedInUserId) {
        // Validate that the sellerId in the product matches the logged-in user
        if (product.getSellerId() != loggedInUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Unauthorized: Product sellerId does not match logged-in user.");
        }
        Product createdProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Update an existing product.
     * PUT /products/{id}
     * Requires X-User-Id header for validation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id,
                                           @RequestBody Product product,
                                           @RequestHeader("X-User-Id") int loggedInUserId) {
        // First, check if the product exists and get its original sellerId
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        // Validate that the logged-in user owns this product
        if (existingProduct.getSellerId() != loggedInUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Unauthorized: You do not own this product.");
        }

        // Ensure the ID and sellerId are not changed via the request body
        product.setId(id); // Use path variable ID
        product.setSellerId(existingProduct.getSellerId()); // Preserve original sellerId

        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update product.");
        }
    }

    /**
     * Delete a product by ID.
     * DELETE /products/{id}
     * Requires X-User-Id header for validation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id,
                                           @RequestHeader("X-User-Id") int loggedInUserId) {
        // First, check if the product exists and get its original sellerId
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        // Validate that the logged-in user owns this product
        if (existingProduct.getSellerId() != loggedInUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Unauthorized: You do not own this product.");
        }

        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete product.");
        }
    }

    /**
     * Endpoint to handle a single product purchase (from detailProduct.html).
     * POST /products/{id}/buy
     * Receives the quantity to purchase using BuyRequestDTO.
     */
    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buyProduct(@PathVariable int id, @RequestBody BuyRequestDTO buyRequest) {
        Integer quantity = buyRequest.getQuantity(); // Obter a quantidade do DTO
        System.out.println("DEBUG: ProductController - Received buy request for product ID: " + id + ", quantity: " + quantity);

        if (quantity == null || quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid quantity provided.");
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        if (product.getStock() == null || product.getStock() < quantity) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock for " + product.getTitle() + ". Available: " + product.getStock() + ", Requested: " + quantity + ".");
        }

        // Decrement stock
        product.setStock(product.getStock() - quantity);
        productService.updateProduct(id, product); // Use updateProduct to save the new stock
        System.out.println("DEBUG: ProductController - Decremented stock for product " + product.getTitle() + " (ID: " + id + ") by " + quantity + ". New stock: " + product.getStock());

        return ResponseEntity.ok(product); // Return the updated product
    }


    /**
     * Endpoint to handle a bulk purchase (from cart.html).
     * POST /products/purchase
     * Receives a list of BuyRequestDTOs.
     */
    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProducts(@RequestBody List<BuyRequestDTO> purchaseItems) { // Alterado para List<BuyRequestDTO>
        System.out.println("DEBUG: ProductController - Received bulk purchase request for " + purchaseItems.size() + " items.");
        List<String> errors = new ArrayList<>();

        for (BuyRequestDTO item : purchaseItems) { // Iterar sobre BuyRequestDTO
            try {
                Integer productId = item.getId(); // Obter ID do DTO
                Integer quantity = item.getQuantity(); // Obter quantidade do DTO

                if (productId == null || quantity == null || quantity <= 0) {
                    errors.add("Invalid item data: ID or quantity missing/invalid for an item.");
                    continue;
                }

                Product product = productService.getProductById(productId);
                if (product == null) {
                    errors.add("Product with ID " + productId + " not found.");
                    continue;
                }

                if (product.getStock() == null || product.getStock() < quantity) {
                    errors.add("Insufficient stock for product " + product.getTitle() + " (ID: " + productId + "). Available: " + product.getStock() + ", Requested: " + quantity + ".");
                    continue;
                }

                // Decrement stock
                product.setStock(product.getStock() - quantity);
                productService.updateProduct(productId, product); // Use updateProduct to save the new stock
                System.out.println("DEBUG: ProductController - Decremented stock for product " + product.getTitle() + " (ID: " + productId + ") by " + quantity + ". New stock: " + product.getStock());

            } catch (Exception e) {
                errors.add("Error processing item: " + e.getMessage());
                System.err.println("ERROR: ProductController - General error in bulk purchase: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (errors.isEmpty()) {
            return ResponseEntity.ok("Purchase completed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
    }
}
