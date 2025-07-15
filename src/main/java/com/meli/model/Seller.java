package com.meli.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("seller") // This links to the "seller" type in User's JsonSubTypes
public class Seller extends User {

    private double walletBalance = 0;
    // <productId, quantity>
    private Map<Integer, Integer> inventory = new HashMap<>(); 

    // Constructor
    public Seller() {
        super(); // Call the constructor of the User superclass
    }

    // You might need a constructor to initialize fields from superclass if not already done
    public Seller(String name, String email, String password, String address) {
        super(name, email, password, address);
    }

    // Getter for walletBalance
    public double getWalletBalance() {
        return walletBalance;
    }

    // Setter for walletBalance
    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    // Setter for inventory
    public void setInventory(Map<Integer, Integer> inventory) {
        this.inventory = inventory;
    }

    // Methods to manage inventory quantity
    public void addProductToInventory(Integer productId, int quantity) {
        this.inventory.put(productId, this.inventory.getOrDefault(productId, 0) + quantity);
    }

    public void removeProductFromInventory(Integer productId) {
        this.inventory.remove(productId);
    }

    public void updateProductQuantity(Integer productId, int newQuantity) {
        if (newQuantity <= 0) {
            this.inventory.remove(productId); // Remove if quantity is zero or less
        } else {
            this.inventory.put(productId, newQuantity);
        }
    }

    public Integer getProductQuantity(Integer productId) {
        return this.inventory.getOrDefault(productId, 0);
    }

    @Override // IMPORTANT: This annotation tells the compiler we are overriding a superclass method
    public String getType() {
        return "seller";
    }
}
