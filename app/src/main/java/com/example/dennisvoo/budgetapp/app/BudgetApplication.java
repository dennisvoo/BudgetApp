package com.example.dennisvoo.budgetapp.app;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class BudgetApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        //Realm.deleteRealm(config);
    }
}
