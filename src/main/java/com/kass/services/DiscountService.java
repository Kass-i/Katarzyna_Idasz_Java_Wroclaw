package com.kass.services;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.dto.SolutionDTO;
import com.kass.services.DpMethod.CalculatorDP;

import java.util.List;

public class DiscountService {

    public SolutionDTO calculateCostByDP(List<OrderDTO> orders, List<PaymentMethodDTO> paymentMethods) {
        Calculator calculatorDP = new CalculatorDP();
        return calculatorDP.calculate(orders, paymentMethods);
    }
}
