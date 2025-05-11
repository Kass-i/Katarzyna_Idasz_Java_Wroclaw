package com.kass.services;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;

import java.util.List;

public interface Calculator {
    void calculate(List<OrderDTO> orders, List<PaymentMethodDTO> paymentMethods);
}
