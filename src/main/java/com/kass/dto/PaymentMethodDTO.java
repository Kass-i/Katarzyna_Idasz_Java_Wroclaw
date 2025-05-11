package com.kass.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Setter
public class PaymentMethodDTO implements Cloneable {
    @NonNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("discount")
    private int discount;

    @JsonProperty("limit")
    private float limit;

    public PaymentMethodDTO(PaymentMethodDTO other) {
        this.id = other.id;
        this.discount = other.discount;
        this.limit = other.limit;
    }

    @Override
    public PaymentMethodDTO clone() {
        return new PaymentMethodDTO(this);
    }
}
