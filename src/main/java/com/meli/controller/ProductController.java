/**
 * Centralized entry point that handles web requests ans responses
 */
package com.meli.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.meli.dto.BuyRequestDTO;
import com.meli.model.Product;
import com.meli.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // This endpoint receives the 'tags' query parameter from the frontend
    @GetMapping
    public List<Product> getAllProducts(@RequestParam Optional<String> tags) {
        System.out.println("BACKEND: ProductController.getAllProducts() called. Tags param: " + tags.orElse("N/A"));
        // It delegates the filtering logic entirely to the ProductService
        return productService.getAllProducts(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") int id) {
        // ProductService.getProductById now throws ResponseStatusException (HttpStatus.NOT_FOUND)
        // if the product is not found. Spring will automatically translate this
        // into an HTTP 404 response. No need for explicit if/else here.
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product); // Returns 200 OK with the product
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Returns HTTP 201 Created on success
    public Product addProduct(@RequestBody Product product) {
        // ProductService.addProduct now handles ID generation and saving
        return productService.addProduct(product);
    }

    /**
     * Put method used to update products
     * * @param id product's unique id
     * @param updatedProduct Product with updated information
     * @return HTTP 200 OK with the updated product if successful,
     * 404 Not Found if product ID is not found (handled by service throwing ResponseStatusException),
     * or 500 Internal Server Error if persistence failed (handled by service throwing ResponseStatusException).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product updatedProduct) {
        // ProductService.updateProduct now returns the updated Product or throws
        // ResponseStatusException (e.g., 404 NOT_FOUND or 500 INTERNAL_SERVER_ERROR).
        // Spring will handle these exceptions automatically.
        Product resultProduct = productService.updateProduct(id, updatedProduct);
        return ResponseEntity.ok(resultProduct); // Returns 200 OK with the updated product
    }

    /*
     * Why Post instead of Put:
     * You're not just updating the product; you're performing a specific action (a "purchase") on it,
     * which has its own logic and side effects:
     * You're not replacing the entire product.
     * The action is contextual: it depends on business logic (stock check, validation).
     * You’re not sending the whole product, only a quantity — that's not a full update.
     */
    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buyProduct(@PathVariable int id, @RequestBody BuyRequestDTO request) {
        try {
            int quantity = request.getQuantity();
            Product updated = productService.buyProduct(id, quantity);
            return ResponseEntity.ok(updated); // Returns 200 OK with the updated product
        } catch (IllegalArgumentException e) {
            // This exception is for business validation (e.g., invalid quantity, insufficient stock)
            return ResponseEntity.badRequest().body(e.getMessage()); // Returns 400 Bad Request
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Returns HTTP 204 No Content on successful deletion
    public void deleteProduct(@PathVariable int id) {
        // ProductService.deleteProductById now throws ResponseStatusException (HttpStatus.NOT_FOUND)
        // if the product is not found. Spring will automatically translate this
        // into an HTTP 404 response.
        productService.deleteProductById(id);
    }
}
