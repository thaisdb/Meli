package com.meli.model;

import java.util.HashMap;
import java.util.Map;

public class Orders {
    private int id;
    private int consumerId;
    private int sellerId;
    // <productsId, quantity>
    private Map<Integer, Integer> products = new HashMap<>();
    private String shippingAddress;
    private String sendersAddress;
    private Double total;
    private Double shippingCost;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Map<Integer, Integer> getProducts() {
        return products;
    }
    public void setProducts(Map<Integer, Integer> products) {
        this.products = products;
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
}
                                                                             