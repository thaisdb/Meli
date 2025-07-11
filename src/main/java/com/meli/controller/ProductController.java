/*
 * Centralized entry point that handles web requests ans responses
 */
package com.meli.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meli.model.Product;
import com.meli.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") int id) {
        Product product = productService.getProductById(id);
        return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product createdProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /*
     * Put method used to update products
     * @param id product's unique id
     * @param updatedProduct Product with updated information
     * @return HTTP response ok if product is succefully updated
     * @return HTTP response HttpStatus.NOT_FOUND if product id is not found
     * @throw HttpStatus.INTERNAL_SERVE_ERROR
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable int id, @RequestBody Product updatedProduct) {
        try {
            boolean updated = productService.updateProduct(id, updatedProduct);
            if (updated) {
                return ResponseEntity.ok("Product updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Put: Failed to update product.");
        }
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable int id) {
        productService.deleteById(id);
    }
}
