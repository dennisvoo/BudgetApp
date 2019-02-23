package com.example.dennisvoo.budgetapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.dennisvoo.budgetapp.Adapter.PurchaseAdapter;
import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.model.BudgetMonth;
import com.example.dennisvoo.budgetapp.model.Purchase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import io.realm.Realm;
import io.realm.RealmList;
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
        RealmList<Purchase> purchasesInDay = new RealmList<>();

        if (getIntent().getBooleanExtra("wholeMonth",false) == false) {
            RealmResults<Purchase> results =
                    realm.where(Purchase.class).contains("date", date).findAll();
            purchasesInDay.addAll(results);
        } else {
            BudgetMonth thisMonth =
                    realm.where(BudgetMonth.class).contains("name", date).findFirst();
            purchasesInDay = thisMonth.getPurchases();
        }


        int infoListSize = purchasesInDay.size() * 2;
        purchaseInfoList = new String[infoListSize];

        for (int i = 0; i < infoListSize; i++) {
            Purchase currPurchase = purchasesInDay.get(i/2);
            purchaseInfoList[i] = currPurchase.getCategory();

            // use big decimal for rounding doubles when printing out purchase amount
            Double doubleAmount = currPurchase.getPurchaseAmount();
            BigDecimal bd = new BigDecimal(doubleAmount).setScale(2, RoundingMode.HALF_UP);
            doubleAmount = bd.doubleValue();
            // use decimal format to ensure two decimal places
            DecimalFormat df = new DecimalFormat("0.00");
            String dollarForm = df.format(doubleAmount);

            purchaseInfoList[++i] = "$" + dollarForm;
        }

        purchaseAdapter.setListOfPurchases(purchaseInfoList);

    }


}
