package com.kass.services.DpMethod;

import com.kass.dto.PaymentMethodDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@AllArgsConstructor
@Getter
public class PaymentApplier {
    private Wallet currentLimits;
    private float orderCost;

    public boolean payWithDiscount(PaymentMethodDTO paymentMethod) {
        float limit = paymentMethod.getLimit();
        float discount = (float) paymentMethod.getDiscount() / 100;
        if (limit < orderCost) {
            // The limit is too small
            System.out.println("\t\t\tLimit too small");
            return false;
        }

        // Update limits
        orderCost *= (1.0f - discount);
        limit -= orderCost;
        paymentMethod.setLimit(limit);

        // Update wallet
        currentLimits.getPaymentMethods().replace(paymentMethod.getId(), paymentMethod);
        return true;
    }

    public boolean payWithPointsAndCardDiscount() {
        float pointsLimit = currentLimits.getPaymentMethods().get("PUNKTY").getLimit();
        if(pointsLimit == 0.0f || pointsLimit < orderCost * 0.10f) {
            // No points or less than 10% order cost - partial payment with discount is impossible
            System.out.println("\t\tNO POINTS!!!");
            return false;
        }

        float points = orderCost * 0.10f;
        PaymentMethodDTO updatedPoints = currentLimits.getPaymentMethods().get("PUNKTY").clone();
        updatedPoints.setLimit(currentLimits.getPaymentMethods().get("PUNKTY").getLimit() - points);
        float discount = 0.10f;

        TreeMap<Float, PaymentMethodDTO> backupList = new TreeMap<>();
        // Find a card with enough money
        for(PaymentMethodDTO paymentMethod : currentLimits.getPaymentMethods().values()) {
            float limit = paymentMethod.getLimit();
            if(limit + points < orderCost) {
                // The limit is too small

                if(!paymentMethod.getId().equals("PUNKTY") && limit + pointsLimit >= orderCost) {
                    // We can use more points
                    backupList.put(orderCost - limit, paymentMethod);
                }
                continue;
            }

            System.out.println("\tWE PAY WITH " + paymentMethod.getId());
            // Update limits
            orderCost *= (1.0f - discount);
            limit -= (orderCost - points);
            PaymentMethodDTO updatedPaymentMethod = paymentMethod.clone();
            updatedPaymentMethod.setLimit(limit);

            // Update wallet
            currentLimits.getPaymentMethods().replace(updatedPaymentMethod.getId(), updatedPaymentMethod);
            currentLimits.getPaymentMethods().replace("PUNKTY", updatedPoints);
            return true;
        }
        // Check backupList
        if(backupList.isEmpty())
            return false;

        // Take first one - smallest amount of points
        Map.Entry<Float, PaymentMethodDTO> firstEntry = backupList.firstEntry();
        points = firstEntry.getKey();
        updatedPoints.setLimit(currentLimits.getPaymentMethods().get("PUNKTY").getLimit() - points);
        PaymentMethodDTO paymentMethod = firstEntry.getValue().clone();
        System.out.println("\tWE SPENT MORE POINTS - WE PAY WITH " + paymentMethod.getId());
        // Update limits
        float limit = paymentMethod.getLimit();
        orderCost *= (1.0f - discount);
        limit -= (orderCost - points);
        PaymentMethodDTO updatedPaymentMethod = paymentMethod.clone();
        updatedPaymentMethod.setLimit(limit);

        // Update wallet
        currentLimits.getPaymentMethods().replace(updatedPaymentMethod.getId(), updatedPaymentMethod);
        currentLimits.getPaymentMethods().replace("PUNKTY", updatedPoints);
        return true;
    }

    public boolean payWithoutDiscount() {
        // Find a card with enough money
        for(PaymentMethodDTO paymentMethod : currentLimits.getPaymentMethods().values()) {
            float limit = paymentMethod.getLimit();
            if(limit < orderCost) {
                // The limit is too small
                continue;
            }

            // Update limits
            limit -= orderCost;
            PaymentMethodDTO updatedPaymentMethod = paymentMethod.clone();
            updatedPaymentMethod.setLimit(limit);

            // Update wallet
            currentLimits.getPaymentMethods().replace(updatedPaymentMethod.getId(), updatedPaymentMethod);
            return true;
        }

        return false;
    }
}
