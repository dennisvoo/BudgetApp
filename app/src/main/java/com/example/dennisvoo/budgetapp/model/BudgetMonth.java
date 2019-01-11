package com.example.dennisvoo.budgetapp.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class BudgetMonth extends RealmObject {

    @Required
    private String name;
    private String monthNumber;
    private double amountSaved;
    private double spendingAmount;
    private RealmList<Purchase> purchases;

    // Getters and Setters for member variables in model
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(String monthNumber) { this.monthNumber = monthNumber; }

    public double getAmountSaved() {
        return amountSaved;
    }

    public void setAmountSaved(double amountSaved) { this.amountSaved = amountSaved; }

    public double getSpendingAmount() {
        return spendingAmount;
    }

    public void setSpendingAmount(double spendingAmount) {
        this.spendingAmount = spendingAmount;
    }

    public RealmList<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(RealmList<Purchase> purchases) { this.purchases = purchases; }

    public void addToPurchases(Purchase purchase) { this.purchases.add(purchase); }

}
