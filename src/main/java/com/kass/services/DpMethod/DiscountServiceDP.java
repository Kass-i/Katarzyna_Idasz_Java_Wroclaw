package com.kass.services.DpMethod;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import lombok.NonNull;

import java.util.*;

public class DiscountServiceDP {
    @NonNull
    List<OrderDTO> orders;

    @NonNull
    Wallet wallet;

    @NonNull
    List<DpState> dp;
    int dpSize;


    public DiscountServiceDP(List<OrderDTO> orders, List<PaymentMethodDTO> paymentMethods) {
        // Sort by the most expensive
        this.orders = orders;
        orders.sort(java.util.Comparator.comparing(OrderDTO::getValue).reversed());

        wallet = new Wallet(paymentMethods);

        dpSize = 1 << orders.size();
        this.dp = new ArrayList<>(dpSize);

        // Initialize start state for every dp element
        for(int i = 0; i < dpSize; i++) {
            DpState startState = new DpState(Integer.MAX_VALUE, wallet.clone());
            dp.add(startState);
        }

        System.out.println("\t\tDP: " + dp);
    }

    public void calculate() {
        for (int mask = 0; mask < dpSize; mask++) {
            System.out.println("WE ARE IN MASK = " + mask);
            for (int order = 0; order < orders.size(); order++) {
                System.out.println("IN ORDER = " + order);
                if ((mask & (1 << order)) != 0) {
                    // The order was already paid
                    System.out.println("PASSED^");
                    continue;
                }

                Wallet currentLimits = dp.get(mask).getWallet().clone();
                System.out.println("\tCURRENT LIMITS:");
                System.out.println(currentLimits);
                float orderCost = orders.get(order).getValue();
                System.out.println("\tOrder = " + order + ", COST = " + orderCost);

                // Points
                PaymentMethodDTO pointsMethod = currentLimits.getPaymentMethods().get("PUNKTY").clone();
                System.out.println("\tMETHOD = " + pointsMethod);
                if (applyDiscount(currentLimits, orderCost, mask, order, pointsMethod)) {
                    // Paid with points
                    System.out.println("\t\tPaid points");
                    continue;
                }

                // Points + card discount
                if(applyPointsAndCardDiscount(currentLimits, orderCost, mask, order)) {
                    // Paid with points and card
                    System.out.println("\t\tPaid points and card");
                    continue;
                }

                // Card discount
                List<String> promotions = orders.get(order).getPromotions();
                boolean paid = false;
                System.out.println(promotions);
                if(promotions != null) {
                    for (String promotion : promotions) {
                        PaymentMethodDTO paymentMethod = currentLimits.getPaymentMethods().get(promotion).clone();
                        if (applyDiscount(currentLimits, orderCost, mask, order, paymentMethod)) {
                            // Paid with discount card
                            paid = true;
                            System.out.println("\t\tPaid card " + promotion);
                            break;
                        }
                    }
                }
                if (paid) {
                    continue;
                }

                // Card with no discount
                if(!payWithoutDiscount(currentLimits, orderCost, mask, order)) {
                    // Not Paid with card
                    System.out.println("\t\tNO PAYMENT!!!!!");
                    continue;
                }
                System.out.println("\t\tPaid card regular");

            }
        }

        // Find solution
        Map<String, Float> solution = new LinkedHashMap<>();
        System.out.println("\t\tSPENT: " + dp.get(dpSize - 1).getCost());
        for(PaymentMethodDTO paymentMethod : dp.get(dpSize - 1).getWallet().getPaymentMethods().values()) {
            float startLimit = wallet.getPaymentMethods().get(paymentMethod.getId()).getLimit();
            System.out.println(startLimit);
            float moneyLeft = paymentMethod.getLimit();
            System.out.println(moneyLeft);
            solution.put(paymentMethod.getId(), startLimit - moneyLeft);
        }

        System.out.println(solution);
    }

    private boolean applyDiscount(Wallet currentLimits, float orderCost, int mask, int order, PaymentMethodDTO paymentMethod) {
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
        updateDP(currentLimits, orderCost, mask, order);
        return true;
    }

    private boolean applyPointsAndCardDiscount(Wallet currentLimits, float orderCost, int mask, int order) {
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
            updateDP(currentLimits, orderCost, mask, order);
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
        updateDP(currentLimits, orderCost, mask, order);
        return true;
    }

    private boolean payWithoutDiscount(Wallet currentLimits, float orderCost, int mask, int order) {
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
            updateDP(currentLimits, orderCost, mask, order);
            return true;
        }

        return false;
    }

    private void updateDP(Wallet currentLimits, float orderCost, int mask, int order) {
        float oldCost = dp.get(mask).getCost();
        float newCost = orderCost;
        if(oldCost < Integer.MAX_VALUE) {
            newCost += oldCost;
        }

        int newMask = mask | (1 << order);  // Mark this order as paid

        if (dp.get(newMask).getCost() > newCost) {
            DpState newState = new DpState(newCost, currentLimits);
            dp.set(newMask, newState);
            System.out.println("\t\tNEW dp: " + dp.get(newMask));
        }
    }

    private boolean isLastOrder(int mask) {
        int inverted = ~mask;
        return (inverted & (inverted - 1)) == 0 && inverted != 0;
    }
}
