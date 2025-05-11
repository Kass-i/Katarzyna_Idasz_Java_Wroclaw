package com.kass.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
public class SolutionDTO {
    Map<String, Float> solution = new HashMap<>();


    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for(String id : solution.keySet())
            output
                .append(id)
                .append(" ")
                .append(solution.get(id))
                .append("\n");
        return output.toString();
    }
}
