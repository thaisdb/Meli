/**
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<?> getProductById(@PathVariable("id") int id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Produto com ID " + id + " não foi encontrado.");
        }
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product createdProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Put method used to update products
     * 
     * @param id product's unique id
     * @param updatedProduct Product with updated information
     * @return HTTP 200 OK if product is succefully updated,
     *         400 Not Found if product ID is not found
     *         or 500 Internal Server Error if product failed to update
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable int id, @RequestBody Product updatedProduct) {
        try {
            boolean updated = productService.updateProduct(id, updatedProduct);
            if (updated) {
                return ResponseEntity.ok("Produto atualizado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UpdateProduct: Produto não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("UpdateProduct: Failed to update product.");
        }
    }

    @PutMapping("/products/{id}/buy")
    public ResponseEntity<?> buyProduct(@PathVariable int id, @RequestParam int quantity) {
        try {
            Product updated = productService.buyProduct(id, quantity);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable int id) {
        productService.deleteById(id);
    }
}
