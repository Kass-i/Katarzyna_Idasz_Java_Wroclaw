package com.kass.services;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.services.DpMethod.CalculatorDP;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
public class DiscountService {
    @NonNull
    private List<OrderDTO> orders;

    @NonNull
    private List<PaymentMethodDTO> paymentMethods;

    public void calculateCostByDP() {
        Calculator calculatorDP = new CalculatorDP();
        calculatorDP.calculate(orders, paymentMethods);
    }
}
