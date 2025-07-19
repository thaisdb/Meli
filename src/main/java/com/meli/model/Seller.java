package com.meli.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("seller") // This links to the "seller" type in User's JsonSubTypes
public class Seller extends User {

    private double walletBalance = 0;
    // <productId, quantity>
    private Map<Integer, Integer> inventory = new HashMap<>(); 

    public Seller() {
        super();
    }

    public Seller(String name, String email, String cpf, String password, String address) {
        super(name, email, cpf, password, address);
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

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
            // Remove if quantity is zero or less
            this.inventory.remove(productId);
        } else {
            this.inventory.put(productId, newQuantity);
        }
    }

    public Integer getProductQuantity(Integer productId) {
        return this.inventory.getOrDefault(productId, 0);
    }

    @Override
    public String getType() {
        return "seller";
    }
}
