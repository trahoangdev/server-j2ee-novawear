package com.example.novawear.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    /** Payment method: PAYPAL or MOMO */
    private String paymentMethod;

    @Valid
    @NotEmpty(message = "Cart must not be empty")
    private List<CartAddRequest> items;
}
