package com.meli.model;

import java.util.Objects;

public class Product {
    private int id;
    private String title;
    private Double price;
    private String description;
    private String imageUrl;
    private String brand;
    private Integer stock;
    private String category;
    private int sellerId;

    // IMPORTANT: No-argument constructor is ESSENTIAL for Jackson deserialization
    public Product() {
    }

    // Parameterized constructor
    public Product(int id, String title, Double price, String description, String imageUrl, String brand, Integer stock, String category, int sellerId) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.stock = stock;
        this.category = category; // Usando o novo campo category
        this.sellerId = sellerId;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", price=" + price +
               ", stock=" + stock +
               ", imageUrl='" + imageUrl + '\'' +
               ", brand='" + brand + '\'' +
               ", category='" + category + '\'' + // AGORA É CATEGORY
               ", sellerId=" + sellerId +
               '}';
    }
}
