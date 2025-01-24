package com.firstapp.testdondaapp.models;

public class CollectFareRequest {
    private String phone;
    private String amount;

    // Constructor
    public CollectFareRequest(String phone, String amount) {
        this.phone = phone;
        this.amount = amount;
    }

}
