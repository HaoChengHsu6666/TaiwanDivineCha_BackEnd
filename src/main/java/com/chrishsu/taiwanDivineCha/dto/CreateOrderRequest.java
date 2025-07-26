package com.chrishsu.taiwanDivineCha.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String deliveryMethod;
    private DeliveryDetails deliveryDetails;
    private PickupDetails pickupDetails;
    private String paymentMethod;
    private AtmPayment atmPayment;
    private CreditCardPayment creditCardPayment;

    @Data
    public static class DeliveryDetails {
        private String recipientName;
        private String recipientEmail;
        private String recipientPhone;
        private String deliveryAddress;
        private String deliveryNotes;
    }

    @Data
    public static class PickupDetails {
        private String recipientName;
        private String recipientEmail;
        private String recipientPhone;
        private String pickupStore;
        private String deliveryNotes;
    }

    @Data
    public static class AtmPayment {
        private String bank;
        private String accountNumber;
        private String name;
        private String email;
        private String phone;
    }

    @Data
    public static class CreditCardPayment {
        private String email;
        private String cardCode;
        private String cardNumber;
        private String cardExpiry;
        private String cardCvc;
    }
}
