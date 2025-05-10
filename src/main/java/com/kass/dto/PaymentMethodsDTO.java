package com.kass.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class PaymentMethodsDTO {
    @NonNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("discount")
    private int discount;

    @JsonProperty("limit")
    private float limit;
}
