package com.kass.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class OrderDTO {
    @NonNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private float value;

    @JsonProperty("promotions")
    List<String> promotions;
}
