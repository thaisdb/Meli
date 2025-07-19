package com.meli.controller;

import com.meli.model.Orders;
import com.meli.model.Product;
import com.meli.service.OrderService;
import com.meli.service.ProductService;
import com.meli.dto.BuyRequestDTO; // Agora com o campo 'id'
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/products") // Base path for all methods in this controller
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;

    public ProductController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
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
     * Endpoint unificado para lidar com a compra de um ou múltiplos produtos.
     * POST /products/purchase
     * Recebe uma lista de BuyRequestDTOs e o ID do consumidor logado.
     * Mantém a lógica de validação de estoque e decremento, e então chama OrderService para criar o pedido.
     */
    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProducts(@RequestBody List<BuyRequestDTO> purchaseItems,
                                            @RequestHeader("X-User-Id") int loggedInConsumerId) { 
        System.out.println("DEBUG: ProductController - Received purchase request for " + purchaseItems.size() + " items, by consumer ID: " + loggedInConsumerId);
        
        List<String> errors = new ArrayList<>();

        // Validação básica da lista de itens
        if (purchaseItems == null || purchaseItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Nenhum item selecionado para compra."));
        }

        // 1. Lógica de validação e decremento de estoque (MANTIDA AQUI)
        for (BuyRequestDTO item : purchaseItems) { 
            try {
                Integer productId = item.getId(); 
                Integer quantity = item.getQuantity(); 

                if (productId == null || quantity == null || quantity <= 0) {
                    errors.add("Dados de item inválidos: ID ou quantidade ausente/inválida para um item.");
                    continue; 
                }

                Product product = productService.getProductById(productId);
                if (product == null) {
                    errors.add("Produto com ID " + productId + " não encontrado.");
                    continue; 
                }

                if (product.getStock() == null || product.getStock() < quantity) {
                    errors.add("Estoque insuficiente para o produto " + product.getTitle() + " (ID: " + productId + "). Disponível: " + product.getStock() + ", Solicitado: " + quantity + ".");
                    continue; 
                }

                // Decrement stock
                product.setStock(product.getStock() - quantity);
                productService.updateProduct(productId, product); 
                System.out.println("DEBUG: ProductController - Estoque decrementado para o produto " + product.getTitle() + " (ID: " + productId + ") por " + quantity + ". Novo estoque: " + product.getStock());

            } catch (Exception e) {
                errors.add("Erro ao processar item: " + e.getMessage());
                System.err.println("ERROR: ProductController - Erro geral na compra de item: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 2. Se houver erros na validação ou processamento individual, retorna-os como JSON
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors); 
        }

        // 3. Se não houver erros, chama OrderService para criar o pedido
        try {
            Optional<Orders> createdOrder = orderService.createProductOrder(purchaseItems, loggedInConsumerId);

            if (createdOrder.isPresent()) {
                return ResponseEntity.ok(createdOrder.get()); // Retorna o objeto Orders criado (JSON)
            } else {
                // Se o OrderService retornar Optional.empty(), significa que houve um erro na criação do pedido
                // (ex: consumidor/vendedor não encontrado, ou validação interna do OrderService).
                // O OrderService já imprime logs de erro mais específicos.
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Falha ao criar o pedido. Verifique os dados do usuário ou a consistência dos produtos."));
            }
        } catch (Exception e) {
            System.err.println("ERROR: ProductController - Erro ao chamar OrderService para criar o pedido: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Arrays.asList("Erro interno ao finalizar a compra: " + e.getMessage()));
        }
    }
}
