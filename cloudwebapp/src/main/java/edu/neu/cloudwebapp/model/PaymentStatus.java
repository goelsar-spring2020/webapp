package edu.neu.cloudwebapp.model;

public enum PaymentStatus {
    paid("paid"),
    due("due"),
    past_due("past_due"),
    no_payment_required("no_payment_required");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String url() {
        return status;
    }
}
