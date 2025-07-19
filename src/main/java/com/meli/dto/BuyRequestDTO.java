// package: com.meli.dto
package com.meli.dto;

/*
 * Data Transfer Object used to carry data between processes
 * Data -> Backend
 * Controler -> Service layer
 * Shaped for the resquest, it helps decouple data model from external APIs
 */
public class BuyRequestDTO {
    private Integer id;
    private Integer quantity;

    // Construtor sem argumentos para Jackson
    public BuyRequestDTO() {
    }

    public BuyRequestDTO(Integer id, Integer quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
