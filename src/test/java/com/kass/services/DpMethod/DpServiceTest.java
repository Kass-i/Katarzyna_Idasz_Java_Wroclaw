package com.kass.services.DpMethod;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.dto.SolutionDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
class DpServiceTest {
    static DpService dpService;

    @BeforeAll
    static void setUp() {
        List<OrderDTO> orders = new ArrayList<>(List.of(
                new OrderDTO("ORDER1", 100, new ArrayList<>(List.of("mZysk"))),
                new OrderDTO("ORDER2", 200, new ArrayList<>(List.of("BosBankrut"))),
                new OrderDTO("ORDER3", 150, new ArrayList<>(List.of("mZysk", "BosBankrut"))),
                new OrderDTO("ORDER4", 50, null)
        ));
        List<PaymentMethodDTO> paymentMethods = new ArrayList<>(List.of(
                new PaymentMethodDTO("PUNKTY",     15, 100),
                new PaymentMethodDTO("mZysk",      10, 180),
                new PaymentMethodDTO("BosBankrut", 5,  200)
        ));
        dpService = new DpService(orders, paymentMethods);
    }

    @Test
    void calculateTest() {
        SolutionDTO actual = dpService.calculate();

        SolutionDTO expected = new SolutionDTO();
        expected.setSolution(new HashMap<>(Map.of(
                "PUNKTY", 100.0f,
                "BosBankrut", 180.0f,
                "mZysk", 170.0f
        )));

        assertEquals(expected.getSolution().entrySet(), actual.getSolution().entrySet());
    }

    @ParameterizedTest
    @ValueSource(ints = {0b0111, 0b1011, 0b1101, 0b1011, 0b111})
    void isLastOrderTest_shouldReturnTrue(int mask) {
        assertTrue(dpService.isLastOrder(mask));
    }

    @ParameterizedTest
    @ValueSource(ints = {0b0011, 0b1001, 0b0000, 0b0010, 0b1, 0b101})
    void isLastOrderTest_shouldReturnFalse(int mask) {
        assertFalse(dpService.isLastOrder(mask));
    }
}