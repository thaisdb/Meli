package com.meli.controller;

import com.meli.model.Consumer;
import com.meli.service.CartService;
import com.meli.dto.BuyRequestDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap; // Para Mapas de resposta

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Adiciona uma quantidade de um produto ao carrinho do consumidor.
     * POST /cart/add
     * Body: { "id": productId, "quantity": quantityToAdd }
     */
    @PostMapping("/add")
    public ResponseEntity<?> addProductToCart(@RequestBody BuyRequestDTO item,
                                              @RequestHeader("X-User-Id") int loggedInConsumerId) {
        if (item.getId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Dados do produto inválidos (ID ou quantidade)."));
        }

        Optional<Consumer> updatedConsumer = cartService.addProductToCart(loggedInConsumerId, item.getId(), item.getQuantity());

        if (updatedConsumer.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Produto adicionado ao carrinho com sucesso.", "cart", updatedConsumer.get().getCart()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Falha ao adicionar produto ao carrinho. Verifique o ID do consumidor, estoque ou dados do produto."));
        }
    }

    /**
     * Define a quantidade total de um produto no carrinho do consumidor.
     * PUT /cart/set-quantity
     * Body: { "id": productId, "quantity": newTotalQuantity }
     */
    @PutMapping("/set-quantity")
    public ResponseEntity<?> setProductQuantityInCart(@RequestBody BuyRequestDTO item,
                                                      @RequestHeader("X-User-Id") int loggedInConsumerId) {
        if (item.getId() == null || item.getQuantity() == null) { // newQuantity pode ser 0 para remover
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Dados do produto inválidos (ID ou quantidade)."));
        }

        Optional<Consumer> updatedConsumer = cartService.setProductQuantityInCart(loggedInConsumerId, item.getId(), item.getQuantity());

        if (updatedConsumer.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Quantidade do produto atualizada no carrinho.", "cart", updatedConsumer.get().getCart()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Falha ao atualizar quantidade do produto no carrinho. Verifique o ID do consumidor, estoque ou dados do produto."));
        }
    }

    /**
     * Remove um produto do carrinho do consumidor.
     * DELETE /cart/remove/{productId}
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeProductFromCart(@PathVariable int productId,
                                                   @RequestHeader("X-User-Id") int loggedInConsumerId) {
        Optional<Consumer> updatedConsumer = cartService.removeProductFromCart(loggedInConsumerId, productId);

        if (updatedConsumer.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Produto removido do carrinho com sucesso.", "cart", updatedConsumer.get().getCart()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Falha ao remover produto do carrinho. Consumidor não encontrado ou produto não estava no carrinho."));
        }
    }

    /**
     * Obtém todos os produtos no carrinho de um consumidor, com detalhes completos do produto.
     * GET /cart
     */
    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("X-User-Id") int loggedInConsumerId) {
        Optional<List<Map<String, Object>>> detailedCartItems = cartService.getDetailedCart(loggedInConsumerId);

        if (detailedCartItems.isPresent()) {
            return ResponseEntity.ok(detailedCartItems.get()); // Retorna a lista de itens detalhados do carrinho
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Carrinho não encontrado ou vazio para o consumidor com ID " + loggedInConsumerId + "."));
        }
    }

    /**
     * Limpa todos os produtos do carrinho de um consumidor.
     * DELETE /cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("X-User-Id") int loggedInConsumerId) {
        Optional<Consumer> updatedConsumer = cartService.clearCart(loggedInConsumerId);

        if (updatedConsumer.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Carrinho limpo com sucesso para o consumidor " + loggedInConsumerId + ".", "cart", updatedConsumer.get().getCart()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Falha ao limpar carrinho. Consumidor não encontrado."));
        }
    }
}
