package com.example.dennisvoo.budgetapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.dennisvoo.budgetapp.Adapter.PurchaseAdapter;
import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.model.Purchase;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExpendituresActivity extends AppCompatActivity {

    private RecyclerView purchaseList;
    private PurchaseAdapter purchaseAdapter;
    private GridLayoutManager layoutManager;

    private String date;
    private String[] purchaseInfoList;

    private Realm realm;

    TextView dateTitleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditures);

        realm = realm.getDefaultInstance();

        dateTitleTV = findViewById(R.id.tv_date_title);

        Intent expendIntent = getIntent();
        Bundle bundle = expendIntent.getExtras();
        if (bundle != null) {
            date = bundle.getString("date");
        }

        dateTitleTV.setText(date);

        purchaseList = findViewById(R.id.recyclerview_purchases);

        layoutManager = new GridLayoutManager(this, 2);
        purchaseList.setLayoutManager(layoutManager);
        purchaseList.setHasFixedSize(true);

        purchaseAdapter = new PurchaseAdapter();
        purchaseList.setAdapter(purchaseAdapter);

        loadPurchaseList(date);

    }

    private void loadPurchaseList(String date) {
        RealmResults<Purchase> purchasesInDay =
                realm.where(Purchase.class).contains("date", date).findAll();


        int resultsSize = purchasesInDay.size();
        int infoListSize = resultsSize * 2;
        purchaseInfoList = new String[infoListSize];

        for (int i = 0; i < resultsSize; i++) {
            Purchase currPurchase = purchasesInDay.get(i);
            int counter = i * 2;
            purchaseInfoList[counter] = currPurchase.getCategory();
            purchaseInfoList[counter + 1] = Double.toString(currPurchase.getPurchaseAmount());
        }

        purchaseAdapter.setListOfPurchases(purchaseInfoList);

    }


}
