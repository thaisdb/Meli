package com.meli.controller;

import com.meli.model.Order; // MUDANÇA AQUI: Usar Order (singular)
import com.meli.service.OrderService;
import com.meli.dto.SellerOrderDTO;
import com.meli.dto.BuyRequestDTO;
import com.meli.dto.OrderSummaryDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Controlador REST lida com as requisições HTTP relacionadas às operações de pedidos.
 * Ele atua como a interface entre o frontend e a lógica de negócio do OrderService
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Get all orders.
     * GET /orders
     */
    @GetMapping
    public List<Order> getAllOrders() { // MUDANÇA AQUI: Usar Order
        return orderService.getAllOrders();
    }

    /**
     * Get order by ID.
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable int id) { // MUDANÇA AQUI: Usar Order
        Optional<Order> order = orderService.getOrderById(id); // MUDANÇA AQUI: Usar Order
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Get orders by consumer ID.
     * GET /orders/consumer/{consumerId}
     */
    @GetMapping("/consumer/{consumerId}")
    public List<OrderSummaryDTO> getOrdersByConsumerId(@PathVariable int consumerId) {
        return orderService.getOrdersByConsumerId(consumerId);
    }

    /**
     * Get orders that contain products from a specific seller.
     * GET /orders/seller
     * Requires X-User-Id header for sellerId.
     */
    @GetMapping("/seller")
    public ResponseEntity<List<SellerOrderDTO>> getOrdersForSeller(@RequestHeader("X-User-Id") int sellerId) {
        System.out.println("DEBUG: OrderController - Recebendo requisição para pedidos do vendedor ID: " + sellerId);
        List<SellerOrderDTO> sellerOrders = orderService.getOrdersBySellerId(sellerId);
        if (sellerOrders.isEmpty()) {
            System.out.println("DEBUG: OrderController - Nenhum pedido encontrado para o vendedor ID: " + sellerId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        }
        return ResponseEntity.ok(sellerOrders);
    }

    /**
     * NOVO ENDPOINT: Cria um novo pedido
     *
     * @param userId O ID do usuário logado (consumerId).
     * @param items A lista de BuyRequestDTO representando os itens e suas quantidades.
     * @return ResponseEntity com o pedido criado ou uma mensagem de erro.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader("X-User-Id") int userId, // consumerId
            @RequestBody List<BuyRequestDTO> items) { 
        
        System.out.println("DEBUG: OrderController: Recebida requisição POST /orders/simple-purchase para userId: " + userId + " com " + items.size() + " itens.");
        try {
            // Chama a versão simplificada do OrderService (sem desconto)
            // Assumindo que OrderService.createProductOrder(List<BuyRequestDTO>, int) existe
            Optional<Order> newOrderOptional = orderService.createProductOrder(items, userId); 
            
            if (newOrderOptional.isPresent()) {
                Order newOrder = newOrderOptional.get();
                System.out.println("DEBUG: OrderController: Pedido simplificado criado com sucesso: " + newOrder.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Falha ao criar o pedido simplificado. Verifique os dados fornecidos.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: OrderController: Erro ao criar pedido simplificado: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("ERROR: OrderController: Erro interno ao criar pedido simplificado: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro interno do servidor ao criar pedido simplificado.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
