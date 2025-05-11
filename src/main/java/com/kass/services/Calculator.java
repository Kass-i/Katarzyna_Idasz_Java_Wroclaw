package com.kass.services;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.dto.SolutionDTO;

import java.util.List;

public interface Calculator {
    SolutionDTO calculate(List<OrderDTO> orders, List<PaymentMethodDTO> paymentMethods);
}
