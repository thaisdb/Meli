package com.meli.controller;

import com.meli.model.Order; // MUDANÇA AQUI: Usar Order (singular)
import com.meli.service.OrderService;
import com.meli.dto.SellerOrderDTO;
import com.meli.dto.OrderSummaryDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
}
