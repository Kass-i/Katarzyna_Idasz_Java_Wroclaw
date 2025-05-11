package com.kass.services.DpMethod;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.services.Calculator;

import java.util.List;

public class CalculatorDP implements Calculator {

    @Override
    public void calculate(List<OrderDTO> orders, List<PaymentMethodDTO> paymentMethods) {
        DiscountServiceDP dp = new DiscountServiceDP(orders, paymentMethods);
        dp.calculate();
    }
}
