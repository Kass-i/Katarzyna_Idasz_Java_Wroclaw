package com.kass.services.DpMethod;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.dto.SolutionDTO;
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

    public SolutionDTO calculate() {
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
                PaymentApplier paymentApplier = new PaymentApplier(currentLimits, orderCost, isLastOrder(mask));

                // Points
                PaymentMethodDTO pointsMethod = currentLimits.getPaymentMethods().get("PUNKTY").clone();
                System.out.println("\tMETHOD = " + pointsMethod);
                if (paymentApplier.payWithDiscount(pointsMethod)) {
                    // Paid with points
                    System.out.println("\t\tPaid points");
                    updateDP(currentLimits, paymentApplier.getOrderCost(), mask, order);
                    continue;
                }

                // Points + card discount
                if(paymentApplier.payWithPointsAndCardDiscount()) {
                    // Paid with points and card
                    System.out.println("\t\tPaid points and card");
                    updateDP(currentLimits, paymentApplier.getOrderCost(), mask, order);
                    continue;
                }

                // Card discount
                List<String> promotions = orders.get(order).getPromotions();
                boolean paid = false;
                System.out.println(promotions);
                if(promotions != null) {
                    for (String promotion : promotions) {
                        PaymentMethodDTO paymentMethod = currentLimits.getPaymentMethods().get(promotion).clone();
                        if (paymentApplier.payWithDiscount(paymentMethod)) {
                            // Paid with discount card
                            paid = true;
                            System.out.println("\t\tPaid card " + promotion);
                            updateDP(currentLimits, paymentApplier.getOrderCost(), mask, order);
                            break;
                        }
                    }
                }
                if (paid) {
                    continue;
                }

                // Card with no discount
                if(paymentApplier.payWithoutDiscount()) {
                    System.out.println("\t\tPaid card regular");
                    updateDP(currentLimits, paymentApplier.getOrderCost(), mask, order);
                    continue;
                }
                // Not Paid with card
                System.out.println("\t\tNO PAYMENT!!!!!");
            }
        }

        // Find solution
        return findSolution();
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

    private SolutionDTO findSolution() {
        SolutionDTO solution = new SolutionDTO();
        System.out.println("\t\tSPENT: " + dp.get(dpSize - 1).getCost());
        for(PaymentMethodDTO paymentMethod : dp.get(dpSize - 1).getWallet().getPaymentMethods().values()) {
            float startLimit = wallet.getPaymentMethods().get(paymentMethod.getId()).getLimit();
            System.out.println(startLimit);
            float moneyLeft = paymentMethod.getLimit();
            System.out.println(moneyLeft);
            solution.getSolution().put(paymentMethod.getId(), startLimit - moneyLeft);
        }

        return solution;
    }

    protected boolean isLastOrder(int mask) {
        String binary = Integer.toBinaryString(mask);
        int missingBits = orders.size() - binary.length();
        long zeroCount = binary.chars().filter(ch -> ch == '0').count();
        zeroCount += missingBits;
        return zeroCount == 1;
    }
}
