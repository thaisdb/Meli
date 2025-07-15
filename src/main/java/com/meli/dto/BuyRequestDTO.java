// package: com.meli.dto
package com.meli.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
/*
 * Data Transfer Object used to carry data between processes
 * Data -> Backend
 * Controler -> Service layer
 * Shaped for the resquest, it helps decouple data model from external APIs
 */
public class BuyRequestDTO {
    @NotNull(message = "BuyRequestDTO: Quantidade é obrigatória.")
    @Min(value = 1, message = "BuyRequestDTO: Quantidade deve ser pelo menos 1.")
    private int quantity;

    /*
     * Default constructor used by Jackson
     */
    public BuyRequestDTO() {
    }

    public BuyRequestDTO(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}