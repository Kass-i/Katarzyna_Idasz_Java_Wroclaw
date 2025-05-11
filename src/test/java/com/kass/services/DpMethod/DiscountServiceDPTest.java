package com.kass.services.DpMethod;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.dto.SolutionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
class DiscountServiceDPTest {
    private List<OrderDTO> orders;
    private List<PaymentMethodDTO> paymentMethods;

    @BeforeEach
    void setUp() {
        orders = new ArrayList<>(List.of(
                new OrderDTO("ORDER1", 100, new ArrayList<>(List.of("mZysk"))),
                new OrderDTO("ORDER2", 200, new ArrayList<>(List.of("BosBankrut"))),
                new OrderDTO("ORDER3", 150, new ArrayList<>(List.of("mZysk", "BosBankrut"))),
                new OrderDTO("ORDER4", 50, null)
        ));
        paymentMethods = new ArrayList<>(List.of(
                new PaymentMethodDTO("PUNKTY",     15, 100),
                new PaymentMethodDTO("mZysk",      10, 180),
                new PaymentMethodDTO("BosBankrut", 5,  200)
        ));
    }

    @Test
    void calculate() {
        DiscountServiceDP discountServiceDP = new DiscountServiceDP(orders, paymentMethods);
        SolutionDTO actual = discountServiceDP.calculate();

        SolutionDTO expected = new SolutionDTO();
        expected.setSolution(new HashMap<>(Map.of(
                "PUNKTY", 100.0f,
                "BosBankrut", 180.0f,
                "mZysk", 170.0f
        )));

        assertEquals(expected.getSolution().entrySet(), actual.getSolution().entrySet());
    }
}