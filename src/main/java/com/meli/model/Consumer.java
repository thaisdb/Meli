package com.meli.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.HashMap;
import java.util.Map;

// This links to the "consumer" type in User's JsonSubTypes
@JsonTypeName("consumer")
public class Consumer extends User {

    private Map<Integer, Integer> cart = new HashMap<>();
    private PaymentMethod preferredPaymentMethod = PaymentMethod.PIX;

    // No-argument constructor is essential for Jackson deserialization
    public Consumer() {
        // Call the no-arg constructor of the User superclass
        super();
        // cart = new HashMap<>();
        // preferredPaymentMethod = PaymentMethod.PIX;
    }

    // Parameterized constructor for convenience
    public Consumer(String name, String email, String cpf, String password, String address) {
        super(name, email, cpf, password, address);
    }

    public Map<Integer, Integer> getCart() {
        return cart;
    }

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

    @Override
    public String getType() {
        return "consumer";
    }

    public PaymentMethod getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(PaymentMethod preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }

}