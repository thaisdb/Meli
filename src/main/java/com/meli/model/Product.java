package com.meli.model;

public class Product {
    private int id;
    private String title;
    private double price;
    private String description;
    private String imageUrl;
    private String brand;
    private int stock;

    public Product() {}

    /*
     * Get unique Id used to identify products
     * @return Id
     */
    public int getId() { return id; }

    /*
     * Set unique Id used to identify products
     */
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
