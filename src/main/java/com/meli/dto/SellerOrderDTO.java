package com.meli.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class SellerOrderDTO {
    private Integer orderId;
    private Integer consumerId; // ID do consumidor que fez o pedido
    private ZonedDateTime timestamp;
    private String status;
    private double total; // Valor total dos produtos DESTE VENDEDOR neste pedido
    private List<Map<String, Object>> sellerItems; // Lista de produtos DESTE VENDEDOR neste pedido

    public SellerOrderDTO(Integer orderId, Integer consumerId, ZonedDateTime timestamp, String status, double total, List<Map<String, Object>> sellerItems) {
        this.orderId = orderId;
        this.consumerId = consumerId;
        this.timestamp = timestamp;
        this.status = status;
        this.total = total;
        this.sellerItems = sellerItems;
    }

    // Getters e Setters
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Integer consumerId) {
        this.consumerId = consumerId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<Map<String, Object>> getSellerItems() {
        return sellerItems;
    }

    public void setSellerItems(List<Map<String, Object>> sellerItems) {
        this.sellerItems = sellerItems;
    }
}
