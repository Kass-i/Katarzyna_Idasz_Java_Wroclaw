package com.kass.services.DpMethod;

import com.kass.dto.PaymentMethodDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.TreeMap;

@AllArgsConstructor
@Getter
public class PaymentApplier {
    @NonNull
    private Wallet currentLimits;
    private float orderCost;
    private boolean isLastOrder;  // If it is a last order, spent points (only when discount is not reduced)

    public boolean payWithDiscount(PaymentMethodDTO paymentMethod) {
        float limit = paymentMethod.getLimit();
        float discount = (float) paymentMethod.getDiscount() / 100;
        if (limit < orderCost) {
            // The limit is too small
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
            return false;
        }

        float points = orderCost * 0.10f;
        // Last order -> spend all points
        if (isLastOrder)
            points = pointsLimit;
        PaymentMethodDTO updatedPoints = currentLimits.getPaymentMethods().get("PUNKTY").clone();
        updatedPoints.setLimit(currentLimits.getPaymentMethods().get("PUNKTY").getLimit() - points);
        float discount = 0.10f;

        TreeMap<Float, PaymentMethodDTO> backupList = new TreeMap<>();
        // Find a card with enough money
        for(PaymentMethodDTO paymentMethod : currentLimits.getPaymentMethods().values()) {
            float limit = paymentMethod.getLimit();
            // The limit is too small
            if(limit + points < orderCost) {
                // We can use more points
                if(!paymentMethod.getId().equals("PUNKTY") && limit + pointsLimit >= orderCost) {
                    backupList.put(orderCost - limit, paymentMethod);
                }
                continue;
            }

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

        // Take the first one - smallest amount of points
        Map.Entry<Float, PaymentMethodDTO> firstEntry = backupList.firstEntry();
        points = firstEntry.getKey();
        updatedPoints.setLimit(currentLimits.getPaymentMethods().get("PUNKTY").getLimit() - points);
        PaymentMethodDTO paymentMethod = firstEntry.getValue().clone();

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
        // Last order -> spend all points
        if (isLastOrder)
            orderCost -= currentLimits.getPaymentMethods().get("PUNKTY").getLimit();

        // Find a card with enough money
        for(PaymentMethodDTO paymentMethod : currentLimits.getPaymentMethods().values()) {
            float limit = paymentMethod.getLimit();
            // The limit is too small
            if(limit < orderCost) {
                continue;
            }

            // Update limits
            limit -= orderCost;
            PaymentMethodDTO updatedPaymentMethod = paymentMethod.clone();
            updatedPaymentMethod.setLimit(limit);

            // Update wallet
            currentLimits.getPaymentMethods().replace(updatedPaymentMethod.getId(), updatedPaymentMethod);
            if (isLastOrder)
                currentLimits.getPaymentMethods().get("PUNKTY").setLimit(0.0f);
            return true;
        }

        return false;
    }
}
