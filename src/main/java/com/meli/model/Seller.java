package com.meli.model;

import java.util.Vector;


public class Seller extends User {
    private double walletBalance = 0;
    private Vector<Product> inventory = new Vector<>();
    /*
     * Initializes retings. Retings[0] will storage the total number of evaluations
     * The other indexes will store the number of 
     */
    public Seller() {
    }
    public double getWalletBalance() {
        return walletBalance;
    }
    public void updateWalletBalance(int soldValue) {
        walletBalance += soldValue;
    }
    public void setWalletBalance(double wallet) {
        this.walletBalance = wallet;
    }
    public Vector<Product> getInventory() {
        return inventory;
    }
    public void addToInventory(Product product) {
        inventory.add(product);
    }
}
