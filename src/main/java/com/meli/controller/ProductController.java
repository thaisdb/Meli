package com.meli.controller;

import com.meli.model.Order;
import com.meli.model.Product;
import com.meli.model.Seller; // Importar Seller
import com.meli.model.Consumer; // Importar Consumer
import com.meli.model.User; // Importar User
import com.meli.service.OrderService;
import com.meli.service.ProductService;
import com.meli.service.UserService; // IMPORTANTE: Importar UserService
import com.meli.dto.BuyRequestDTO;

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
    private final UserService userService; // NOVO: Injetar UserService

    // CONSTRUTOR: Adicionar UserService
    public ProductController(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
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
        products.removeIf(p -> p.getStock() != null && p.getStock().equals(0)); // Adicionado null check para stock
        return ResponseEntity.ok(products);
    }

    /**
     * Get products belonging to a specific seller.
     * GET /products/seller/{sellerId}
     */
    @GetMapping("/seller/{sellerId}") // Correctly mapped to /products/seller/{sellerId}
    public ResponseEntity<?> getProductsBySeller(@PathVariable int sellerId) { // Tipo de retorno ResponseEntity<?>
        System.out.println("DEBUG: ProductController - Fetching products for sellerId: " + sellerId);
        // NOVO: Validação para garantir que o ID é de um vendedor
        User user = userService.getUserById(sellerId);
        if (user == null || !(user instanceof Seller)) {
            System.err.println("ERROR: ProductController.getProductsBySeller - ID " + sellerId + " não corresponde a um vendedor válido.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Acesso negado: ID do vendedor inválido ou não autorizado.");
        }

        List<Product> products = productService.getProductsBySellerId(sellerId);
        return ResponseEntity.ok(products);
    }

    /**
     * Get a single product by ID (no seller scope here, for general product viewing if needed)
     * This can be used by consumers or for internal lookups.
     * GET /products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) { // Tipo de retorno ResponseEntity<?>
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto com ID " + id + " não encontrado.");
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
        System.out.println("DEBUG: ProductController.addProduct - Recebendo requisição para adicionar produto. LoggedInUser ID do header: " + loggedInUserId);

        // NOVO: Validação: O usuário logado (loggedInUserId) deve ser um vendedor
        User user = userService.getUserById(loggedInUserId);
        System.out.println("DEBUG: ProductController.addProduct - Usuário encontrado por UserService.getUserById(" + loggedInUserId + "): " + (user != null ? user.getEmail() + " (Type: " + user.getType() + ")" : "null"));
        if (user == null || !(user instanceof Seller)) {
            System.err.println("ERROR: ProductController.addProduct - Usuário com ID " + loggedInUserId + " não é um vendedor.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Acesso negado: Você precisa estar logado como um vendedor para adicionar produtos.");
        }

        // Validação 2: O sellerId no corpo do produto deve corresponder ao loggedInUserId
        System.out.println("DEBUG: ProductController.addProduct - product.getSellerId(): " + product.getSellerId() + ", loggedInUserId: " + loggedInUserId);
        if (product.getSellerId() != loggedInUserId) {
            System.err.println("ERROR: ProductController.addProduct - Seller ID no corpo do produto (" + product.getSellerId() + ") não corresponde ao ID do usuário logado (" + loggedInUserId + ").");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Unauthorized: O ID do vendedor no produto deve corresponder ao seu ID de usuário logado.");
        }
        
        // Validação 3: Campos obrigatórios (incluindo sellerId != 0)
        if (product.getTitle() == null || product.getTitle().isEmpty() ||
            product.getDescription() == null || product.getDescription().isEmpty() ||
            product.getPrice() == null || product.getPrice() <= 0 ||
            product.getStock() == null || product.getStock() < 0 ||
            product.getSellerId() == 0) { // sellerId deve ser válido (não 0 para um novo produto)
            System.err.println("ERROR: ProductController.addProduct - Campos obrigatórios faltando ou inválidos. Product: " + product.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Todos os campos (título, descrição, preço, estoque, sellerId) são obrigatórios e válidos.");
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
        System.out.println("DEBUG: ProductController.updateProduct - Recebendo requisição para atualizar produto ID: " + id + ". LoggedInUser ID do header: " + loggedInUserId);

        // NOVO: Validação: O usuário logado (loggedInUserId) deve ser um vendedor
        User user = userService.getUserById(loggedInUserId);
        if (user == null || !(user instanceof Seller)) {
            System.err.println("ERROR: ProductController.updateProduct - Usuário com ID " + loggedInUserId + " não é um vendedor.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Acesso negado: Você precisa estar logado como um vendedor para atualizar produtos.");
        }

        // First, check if the product exists and get its original sellerId
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            System.err.println("ERROR: ProductController.updateProduct - Produto com ID " + id + " não encontrado para atualização.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        // Validate that the logged-in user owns this product
        if (existingProduct.getSellerId() != loggedInUserId) {
            System.err.println("ERROR: ProductController.updateProduct - Acesso negado: Produto ID " + id + " não pertence ao vendedor ID " + loggedInUserId + ".");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Unauthorized: Você não tem permissão para atualizar este produto.");
        }

        // NOVO: Validação de campos obrigatórios para atualização
        if (product.getTitle() == null || product.getTitle().isEmpty() ||
            product.getDescription() == null || product.getDescription().isEmpty() ||
            product.getPrice() == null || product.getPrice() <= 0 ||
            product.getStock() == null || product.getStock() < 0) {
            System.err.println("ERROR: ProductController.updateProduct - Campos obrigatórios faltando ou inválidos para atualização.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Todos os campos (título, descrição, preço, estoque) são obrigatórios e válidos.");
        }

        product.setId(id); 
        product.setSellerId(existingProduct.getSellerId()); 

        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        } else {
            System.err.println("ERROR: ProductController.updateProduct - Falha interna ao atualizar produto ID " + id + ".");
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
        System.out.println("DEBUG: ProductController.deleteProduct - Recebendo requisição para deletar produto ID: " + id + ". LoggedInUser ID do header: " + loggedInUserId);

        User user = userService.getUserById(loggedInUserId);
        if (user == null || !(user instanceof Seller)) {
            System.err.println("ERROR: ProductController.deleteProduct - Usuário com ID " + loggedInUserId + " não é um vendedor.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Acesso negado: Você precisa estar logado como um vendedor para excluir produtos.");
        }

        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            System.err.println("ERROR: ProductController.deleteProduct - Produto com ID " + id + " não encontrado para exclusão.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        if (existingProduct.getSellerId() != loggedInUserId) {
            System.err.println("ERROR: ProductController.deleteProduct - Acesso negado: Produto ID " + id + " não pertence ao vendedor ID " + loggedInUserId + ".");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Unauthorized: Você não tem permissão para excluir este produto.");
        }

        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            System.err.println("ERROR: ProductController.deleteProduct - Falha interna ao deletar produto ID " + id + ".");
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
        
        User user = userService.getUserById(loggedInConsumerId);
        if (user == null || !(user instanceof Consumer)) {
            System.err.println("ERROR: ProductController.purchaseProducts - Usuário com ID " + loggedInConsumerId + " não é um consumidor.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Acesso negado: Você precisa estar logado como um consumidor para realizar compras.");
        }

        List<String> errors = new ArrayList<>();

        if (purchaseItems == null || purchaseItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Nenhum item selecionado para compra."));
        }

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

                product.setStock(product.getStock() - quantity);
                productService.updateProduct(productId, product); 
                System.out.println("DEBUG: ProductController - Estoque decrementado para o produto " + product.getTitle() + " (ID: " + productId + ") por " + quantity + ". Novo estoque: " + product.getStock());

            } catch (Exception e) {
                errors.add("Erro ao processar item: " + e.getMessage());
                System.err.println("ERROR: ProductController - Erro geral na compra de item: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors); 
        }

        try {
            Optional<Order> createdOrder = orderService.createProductOrder(purchaseItems, loggedInConsumerId);

            if (createdOrder.isPresent()) {
                return ResponseEntity.ok(createdOrder.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Falha ao criar o pedido. Verifique os dados do usuário ou a consistência dos produtos."));
            }
        } catch (Exception e) {
            System.err.println("ERROR: ProductController - Erro ao chamar OrderService para criar o pedido: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Arrays.asList("Erro interno ao finalizar a compra: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para buscar produtos por termo de pesquisa geral (título, descrição, categoria, marca, tags).
     * GET /products/search?term={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String term) {
        System.out.println("DEBUG: ProductController.searchProducts - Recebendo termo de busca: '" + term + "'");
        List<Product> products = productService.searchProducts(term); // Chama o método de busca geral do serviço
        return ResponseEntity.ok(products);
    }
}
