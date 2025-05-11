package com.kass;

import com.kass.dto.OrderDTO;
import com.kass.dto.PaymentMethodDTO;
import com.kass.dto.SolutionDTO;
import com.kass.loaders.OrdersLoader;
import com.kass.loaders.PaymentMethodsLoader;
import com.kass.services.DiscountService;

import java.util.List;


public class DiscountManager {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("You have to provide 2 json files!");
            return;
        }

        List<OrderDTO> orders = new OrdersLoader().load(args[0]);
        List<PaymentMethodDTO> paymentMethods = new PaymentMethodsLoader().load(args[1]);

        DiscountService discountService = new DiscountService(orders, paymentMethods);
        SolutionDTO solution = discountService.calculateCostByDP();
        System.out.println(solution.toString());
    }
}