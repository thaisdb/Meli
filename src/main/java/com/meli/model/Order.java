package com.meli.model;

import java.time.ZonedDateTime; // Importar ZonedDateTime
import java.util.HashMap;
import java.util.Map;

public class Order { // Nome da classe é Order (singular)
    private int id;
    private int consumerId;
    private int sellerId;
    private Map<Integer, Integer> products = new HashMap<>(); // <productsId, quantity>
    private String shippingAddress;
    private String sendersAddress;
    private Double total;
    private Double shippingCost;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    private ZonedDateTime timestamp;

    // Construtor padrão é essencial para Jackson
    public Order() {
    }

    // Construtor completo (ajustado para incluir timestamp e todos os campos existentes)
    public Order(int id, int consumerId, int sellerId, Map<Integer, Integer> products, String shippingAddress, String sendersAddress, Double total, Double shippingCost, PaymentMethod paymentMethod, OrderStatus status, ZonedDateTime timestamp) {
        this.id = id;
        this.consumerId = consumerId;
        this.sellerId = sellerId;
        this.products = products;
        this.shippingAddress = shippingAddress;
        this.sendersAddress = sendersAddress;
        this.total = total;
        this.shippingCost = shippingCost;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = timestamp; // Inicializar timestamp
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(int consumerId) {
        this.consumerId = consumerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public Map<Integer, Integer> getProducts() {
        return products;
    }

    public void setProducts(Map<Integer, Integer> products) {
        this.products = products;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getSendersAddress() {
        return sendersAddress;
    }

    public void setSendersAddress(String sendersAddress) {
        this.sendersAddress = sendersAddress;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    // Getter e Setter para timestamp
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
