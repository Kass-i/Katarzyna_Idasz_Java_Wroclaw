package com.kass.loaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kass.dto.OrderDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OrdersLoader implements JsonLoader<OrderDTO> {
    @Override
    public List<OrderDTO> load(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<List<OrderDTO>>() {});
        } catch (IOException e) {
            System.err.println("Error reading orders JSON file: " + e.getMessage());
            return null;
        }
    }
}
