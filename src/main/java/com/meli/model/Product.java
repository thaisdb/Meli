package com.meli.model;

import java.util.List;
// Removed Jackson annotations for custom serializer/deserializer as they are not needed for List<String> from JSON array

public class Product {
    private int id;
    private String title;
    private Double price;
    private String description;
    private String imageUrl;
    private String brand;
    private Integer stock;
    private List<String> tags; // Keep as List<String>

    private int sellerId;

    // IMPORTANT: No-argument constructor is ESSENTIAL for Jackson deserialization
    public Product() {
    }

    // Parameterized constructor
    public Product(int id, String title, Double price, String description, String imageUrl, String brand, Integer stock, List<String> tags, int sellerId) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.stock = stock;
        this.tags = tags;
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

    public void setPrice(Double price) { // Corrected access modifier and return type
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

    public List<String> getTags() { // Keep as List<String>
        return tags;
    }

    public void setTags(List<String> tags) { // Keep as List<String>
        this.tags = tags;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }
}
