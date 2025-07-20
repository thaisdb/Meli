package com.meli.dto;

import com.meli.model.OrderStatus;
import com.meli.model.PaymentMethod;

import java.time.ZonedDateTime;
import java.util.List; // Mudar para List
import java.util.Objects;

/**
 * DTO para representar um resumo de pedido para exibição no frontend.
 * Contém informações essenciais do pedido, a contagem total de itens e uma lista de detalhes dos produtos.
 */
public class OrderSummaryDTO {
    private int id;
    private int consumerId;
    private int sellerId;
    private List<OrderProductDetailDTO> productsDetails;
    private int itemCount;
    private String shippingAddress;
    private Double totalAmount;
    private Double shippingCost;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    private ZonedDateTime timestamp;

    public OrderSummaryDTO() {
    }

    // Construtor ajustado para incluir productsDetails e itemCount
    public OrderSummaryDTO(int id, int consumerId, int sellerId, List<OrderProductDetailDTO> productsDetails, int itemCount, String shippingAddress, Double totalAmount, Double shippingCost, PaymentMethod paymentMethod, OrderStatus status, ZonedDateTime timestamp) {
        this.id = id;
        this.consumerId = consumerId;
        this.sellerId = sellerId;
        this.productsDetails = productsDetails;
        this.itemCount = itemCount;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.shippingCost = shippingCost;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public int getConsumerId() { return consumerId; }
    public int getSellerId() { return sellerId; }
    public List<OrderProductDetailDTO> getProductsDetails() { return productsDetails; } // Getter para productsDetails
    public int getItemCount() { return itemCount; }
    public String getShippingAddress() { return shippingAddress; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getShippingCost() { return shippingCost; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public ZonedDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setConsumerId(int consumerId) { this.consumerId = consumerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public void setProductsDetails(List<OrderProductDetailDTO> productsDetails) { this.productsDetails = productsDetails; } // Setter para productsDetails
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public void setShippingCost(Double shippingCost) { this.shippingCost = shippingCost; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderSummaryDTO that = (OrderSummaryDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderSummaryDTO{" +
               "id=" + id +
               ", consumerId=" + consumerId +
               ", sellerId=" + sellerId +
               ", productsDetails=" + productsDetails + // Incluindo productsDetails no toString
               ", itemCount=" + itemCount +
               ", shippingAddress='" + shippingAddress + '\'' +
               ", totalAmount=" + totalAmount +
               ", shippingCost=" + shippingCost +
               ", paymentMethod=" + paymentMethod +
               ", status=" + status +
               ", timestamp=" + timestamp +
               '}';
    }
}
