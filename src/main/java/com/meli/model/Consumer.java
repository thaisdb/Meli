package com.meli.model;

import java.util.HashMap;


public class Consumer extends User {
    /* HashMap<Product, quantity> cart */
    
    private HashMap<Product, Integer> cart = new HashMap<>();
    public HashMap<Product, Integer> getCart() {
        return cart;
    }
    public void insertIntoCart(Product product, Integer quantity) {
        this.cart.put(product, quantity);
    }
    public void removeFromCart(Product product) {
        this.cart.remove(product);
    }
}
