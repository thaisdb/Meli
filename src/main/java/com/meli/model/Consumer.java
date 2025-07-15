package com.meli.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.HashMap;
import java.util.Map; // Import Map interface

@JsonTypeName("consumer") // This links to the "consumer" type in User's JsonSubTypes
public class Consumer extends User {

    // Corrected: Using HashMap<Integer, Integer> for cart
    private Map<Integer, Integer> cart = new HashMap<>();

    // No-argument constructor is essential for Jackson deserialization
    public Consumer() {
        super(); // Call the no-arg constructor of the User superclass
    }

    // Parameterized constructor for convenience
    public Consumer(String name, String email, String password, String address) {
        super(name, email, password, address);
    }

    // Getter for cart
    public Map<Integer, Integer> getCart() {
        return cart;
    }

    // Setter for cart
    public void setCart(Map<Integer, Integer> cart) {
        this.cart = cart;
    }

    // Methods to manage cart (using Integer productId)
    public void addProductToCart(Integer productId, int quantity) {
        this.cart.put(productId, this.cart.getOrDefault(productId, 0) + quantity);
    }

    public void removeProductFromCart(Integer productId) {
        this.cart.remove(productId);
    }

    @Override // IMPORTANT: This annotation tells the compiler we are overriding a superclass method
    public String getType() {
        return "consumer";
    }

}