package edu.neu.cloudwebapp.model;

public enum PaymentStatus {
    PAID("paid"),
    DUE("due"),
    PAST_DUE("past_due"),
    NO_PAYMENT_REQUIRED("no_payment_required");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String url() {
        return status;
    }
}
