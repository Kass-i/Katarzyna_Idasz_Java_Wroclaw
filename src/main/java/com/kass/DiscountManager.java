package com.kass;

import com.kass.dto.OrdersDTO;
import com.kass.dto.PaymentMethodsDTO;
import com.kass.loaders.OrdersLoader;
import com.kass.loaders.PaymentMethodsLoader;

import java.util.List;


public class DiscountManager {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("You have to provide 2 json files!");
            return;
        }

        List<OrdersDTO> orders = new OrdersLoader().load(args[0]);
        List<PaymentMethodsDTO> paymentMethods = new PaymentMethodsLoader().load(args[1]);

        System.out.println("Orders:\n" + orders);
        System.out.println("Payments:\n" + paymentMethods);
    }
}