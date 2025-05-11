package com.kass.services.DpMethod;

import com.kass.dto.PaymentMethodDTO;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString
public class Wallet implements Cloneable {
    private final Map<String, PaymentMethodDTO> paymentMethods;

    public Wallet(List<PaymentMethodDTO> paymentMethods) {
        paymentMethods.sort(java.util.Comparator.comparing(PaymentMethodDTO::getDiscount));  // Sort by the most expensive
        System.out.println("\t\tpaymentMethods" + paymentMethods);
        this.paymentMethods = new LinkedHashMap<>();
        for(PaymentMethodDTO paymentMethodDTO : paymentMethods) {
            this.paymentMethods.put(paymentMethodDTO.getId(), new PaymentMethodDTO(paymentMethodDTO));
        }

        System.out.println("\t\tSORTED CARDS: " + this.paymentMethods);
    }

    public Wallet(Wallet other) {
        paymentMethods = new LinkedHashMap<>();
        for(PaymentMethodDTO paymentMethodDTO : other.getPaymentMethods().values()) {
            paymentMethods.put(paymentMethodDTO.getId(), new PaymentMethodDTO(paymentMethodDTO));
        }
    }

    @Override
    public Wallet clone() {
        return new Wallet(this);
    }
}