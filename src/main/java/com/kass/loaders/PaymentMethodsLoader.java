package com.kass.loaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kass.dto.PaymentMethodsDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PaymentMethodsLoader implements JsonLoader<PaymentMethodsDTO> {
    @Override
    public List<PaymentMethodsDTO> load(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<List<PaymentMethodsDTO>>() {});
        } catch (IOException e) {
            System.err.println("Error reading payment methods JSON file: " + e.getMessage());
            return null;
        }
    }
}
