package com.example.dennisvoo.budgetapp.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class CategoryList extends RealmObject {

    @Required
    private String name;
    private RealmList<Purchase> purchases;

    // Constructors
    public CategoryList() {
        name = "";
        purchases = new RealmList<>();
    }

    public CategoryList(String name) {
        this.name = name;
        purchases = new RealmList<>();
    }

    // Getters and setters for the member variables in model
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(RealmList<Purchase> purchases) { this.purchases = purchases; }

    public void addToPurchases(Purchase purchase) { this.purchases.add(purchase); }
}