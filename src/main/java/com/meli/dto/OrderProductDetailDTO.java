package com.meli.dto;

import java.util.Objects;

/**
 * DTO para representar os detalhes de um produto específico dentro de um pedido,
 * para ser usado em OrderSummaryDTO.
 */
public class OrderProductDetailDTO {
    private int productId;
    private String title;
    private int quantity;
    private String imageUrl; // Opcional: para exibir a imagem do produto

    public OrderProductDetailDTO() {
    }

    public OrderProductDetailDTO(int productId, String title, int quantity, String imageUrl) {
        this.productId = productId;
        this.title = title;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getProductId() { return productId; }
    public String getTitle() { return title; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setProductId(int productId) { this.productId = productId; }
    public void setTitle(String title) { this.title = title; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderProductDetailDTO that = (OrderProductDetailDTO) o;
        return productId == that.productId &&
               quantity == that.quantity &&
               Objects.equals(title, that.title) &&
               Objects.equals(imageUrl, that.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, title, quantity, imageUrl);
    }

    @Override
    public String toString() {
        return "OrderProductDetailDTO{" +
               "productId=" + productId +
               ", title='" + title + '\'' +
               ", quantity=" + quantity +
               ", imageUrl='" + imageUrl + '\'' +
               '}';
    }
}
