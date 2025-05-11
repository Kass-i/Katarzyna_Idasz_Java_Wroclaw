package com.kass.services.DpMethod;

import com.kass.dto.PaymentMethodDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PaymentApplierTest {
    private PaymentApplier paymentApplier;
    Wallet wallet;

    @BeforeEach
    void setUp() {
        List<PaymentMethodDTO> methods = new ArrayList<>(List.of(
                new PaymentMethodDTO("PUNKTY", 15, 100.0f),
                new PaymentMethodDTO("mZysk", 10, 180.0f)
        ));

        wallet = new Wallet(methods);
    }

    static Stream<Arguments> payWithDiscountData() {
        return Stream.of(
                Arguments.of("PUNKTY", 15.0f),
                Arguments.of("mZysk", 90.0f)
        );
    }

    @ParameterizedTest
    @MethodSource("payWithDiscountData")
    void payWithDiscountTest_sufficientLimit_shouldReturnTrue(String payment, float expected) {
        paymentApplier = new PaymentApplier(wallet, 100.0f, false);
        PaymentMethodDTO paymentMethod = wallet.getPaymentMethods().get(payment);
        boolean actual = paymentApplier.payWithDiscount(paymentMethod);
        float newLimit = paymentMethod.getLimit();

        assertTrue(actual);
        assertEquals(expected, newLimit);
    }

    @ParameterizedTest
    @MethodSource("payWithDiscountData")
    void payWithDiscountTest_insufficientLimit_shouldReturnFalse(String payment, float expected) {
        paymentApplier = new PaymentApplier(wallet, 180.1f, false);
        PaymentMethodDTO paymentMethod = wallet.getPaymentMethods().get(payment);
        boolean actual = paymentApplier.payWithDiscount(paymentMethod);

        assertFalse(actual);
    }

    @Test
    void payWithPointsAndCardDiscountTest_sufficientLimit_shouldReturnTrue() {
        paymentApplier = new PaymentApplier(wallet, 150.0f, false);
        boolean actual = paymentApplier.payWithPointsAndCardDiscount();
        float newLimitmZysk = wallet.getPaymentMethods().get("mZysk").getLimit();
        float newLimitPoints = wallet.getPaymentMethods().get("PUNKTY").getLimit();

        assertTrue(actual);
        assertEquals(60.0f, newLimitmZysk);
        assertEquals(85.0f, newLimitPoints);
    }

    @Test
    void payWithPointsAndCardDiscountTest_insufficientLimit_sufficientPoints_shouldReturnTrue() {
        paymentApplier = new PaymentApplier(wallet, 150.0f, false);
        wallet.getPaymentMethods().get("mZysk").setLimit(100.0f);
        boolean actual = paymentApplier.payWithPointsAndCardDiscount();
        float newLimitmZysk = wallet.getPaymentMethods().get("mZysk").getLimit();
        float newLimitPoints = wallet.getPaymentMethods().get("PUNKTY").getLimit();

        assertTrue(actual);
        assertEquals(15.0f, newLimitmZysk);
        assertEquals(50.0f, newLimitPoints);
    }

    @Test
    void payWithPointsAndCardDiscountTest_insufficientLimit_shouldReturnFalse() {
        paymentApplier = new PaymentApplier(wallet, 150.0f, false);
        wallet.getPaymentMethods().get("mZysk").setLimit(100.0f);
        wallet.getPaymentMethods().get("PUNKTY").setLimit(10.0f);
        boolean actual = paymentApplier.payWithPointsAndCardDiscount();
        float newLimitmZysk = wallet.getPaymentMethods().get("mZysk").getLimit();
        float newLimitPoints = wallet.getPaymentMethods().get("PUNKTY").getLimit();

        assertFalse(actual);
        assertEquals(100.0f, newLimitmZysk);
        assertEquals(10.0f, newLimitPoints);
    }

    @Test
    void payWithoutDiscountTest_notLastOrder_shouldReturnTrue() {
        paymentApplier = new PaymentApplier(wallet, 180.0f, false);
        boolean actual = paymentApplier.payWithoutDiscount();
        float newLimitmZysk = wallet.getPaymentMethods().get("mZysk").getLimit();
        float newLimitPoints = wallet.getPaymentMethods().get("PUNKTY").getLimit();

        assertTrue(actual);
        assertEquals(0.0f, newLimitmZysk);
        assertEquals(100.0f, newLimitPoints);
    }

    @Test
    void payWithoutDiscountTest_lastOrder_shouldReturnTrue() {
        paymentApplier = new PaymentApplier(wallet, 180.0f, true);
        wallet.getPaymentMethods().get("PUNKTY").setLimit(10.0f);
        boolean actual = paymentApplier.payWithoutDiscount();
        float newLimitmZysk = wallet.getPaymentMethods().get("mZysk").getLimit();
        float newLimitPoints = wallet.getPaymentMethods().get("PUNKTY").getLimit();

        assertTrue(actual);
        assertEquals(10.0f, newLimitmZysk);
        assertEquals(0.0f, newLimitPoints);
    }

    @Test
    void payWithoutDiscountTest_insufficientLimit_shouldReturnFalse() {
        paymentApplier = new PaymentApplier(wallet, 200.0f, false);
        boolean actual = paymentApplier.payWithoutDiscount();
        float newLimitmZysk = wallet.getPaymentMethods().get("mZysk").getLimit();
        float newLimitPoints = wallet.getPaymentMethods().get("PUNKTY").getLimit();

        assertFalse(actual);
        assertEquals(180.0f, newLimitmZysk);
        assertEquals(100.0f, newLimitPoints);
    }
}