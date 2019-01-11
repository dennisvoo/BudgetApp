package com.example.dennisvoo.budgetapp.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;
import java.util.Date;

public class Purchase extends RealmObject {

    private String date;
    private double purchaseAmount;
    @Required
    private String category;

    // Getters and setters for the member variables in model
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(double purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
