package com.example.dennisvoo.budgetapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.model.BudgetMonth;
import com.example.dennisvoo.budgetapp.model.CategoryList;
import com.example.dennisvoo.budgetapp.model.Purchase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class SummaryActivity extends AppCompatActivity {

    private Realm realm;

    private String name;
    TextView summaryTitleTV;

    TextView amountSavedTV;
    TextView totalAmountSavedTV;
    TextView amountSpentTV;
    TextView netSpendingTV;

    GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        realm = realm.getDefaultInstance();

        summaryTitleTV = findViewById(R.id.tv_summary_title);

        Intent summaryIntent = getIntent();
        Bundle bundle = summaryIntent.getExtras();
        name = bundle.getString("name");

        summaryTitleTV.setText(name);

        // get month for Summary
        BudgetMonth summMonth =
                realm.where(BudgetMonth.class).contains("name", name).findFirst();

        setUpSummaryText(summMonth);
        createTop3List(summMonth);
    }

    /**
     * Method fills in all TextViews in activity
     * @param summMonth used to find details about month we are summarizing
     */
    private void setUpSummaryText(BudgetMonth summMonth) {
        // Go through current month's purchases and sum up to find how much user spent this month
        amountSpentTV = findViewById(R.id.tv_amt_spent_month);

        RealmList<Purchase> purchasesInMonth = summMonth.getPurchases();
        double monthSpent = purchasesInMonth.sum("purchaseAmount").doubleValue();
        String spentMonthFormat = getString(R.string.amt_spent_month, monthSpent);
        amountSpentTV.setText(spentMonthFormat);

        // Give the net spending amount for month, indicating if user went over budget or not
        netSpendingTV = findViewById(R.id.tv_net_spend_month);

        double netSpend = summMonth.getSpendingAmount();
        String netSpendFormat = getString(R.string.net_spend_month, netSpend);
        netSpendingTV.setText(netSpendFormat);

        // find amount saved in current month
        amountSavedTV = findViewById(R.id.tv_amt_saved_month);

        // if summary month has passed we can add net spending amount to savings, so user either
        // adds to savings or dips into them
        Calendar cal = Calendar.getInstance();
        int monthNum = cal.get(Calendar.MONTH) + 1;
        int yearNum = cal.get(Calendar.YEAR);
        String m = Integer.toString(monthNum);
        String y = Integer.toString(yearNum);

        double monthSaved = summMonth.getAmountSaved();

        if (!summMonth.getMonthNumber().equals(y+m)) {
            realm.executeTransaction((realm) ->
                    summMonth.setAmountSaved(monthSaved + netSpend));
        }

        String savedMonthFormat = getString(R.string.amt_saved_month, summMonth.getAmountSaved());
        amountSavedTV.setText(savedMonthFormat);

        // go through all BudgetMonths and add up money saved
        totalAmountSavedTV = findViewById(R.id.tv_amt_saved_total);

        RealmResults<BudgetMonth> results = realm.where(BudgetMonth.class).findAll();
        double totalSaved = results.sum("amountSaved").doubleValue();
        String savedTotalFormat = getString(R.string.amt_saved_total, totalSaved);
        totalAmountSavedTV.setText(savedTotalFormat);
    }

    /**
     * Go through purchases in the summary month and add up every categories amounts to display top
     * 3 categories user spent on.
     * @param summMonth used to find current month's purchases
     */
    private void createTop3List(BudgetMonth summMonth) {
        RealmList<Purchase> listOfPurchases = summMonth.getPurchases();
        // size of array is six since we have 3 categories and 3 purchase amounts
        String[] top3Array = new String[6];

        // sort RealmList by category and then convert the results to an ArrayList
        RealmResults<Purchase> sortedPurchases = listOfPurchases.sort("category");

        ArrayList<Purchase> listCopy = new ArrayList<>();
        listCopy.addAll(realm.copyFromRealm(sortedPurchases));

        realm.executeTransaction((realm) -> {
            CategoryList catList = realm.where(CategoryList.class)
                    .equalTo("name", summMonth.getName()).findFirst();

            if (catList == null) {
                CategoryList newCatList = new CategoryList(summMonth.getName());
                catList = newCatList;
                realm.copyToRealm(catList);
            }
            // creating a new catList to account for any new purchases
            RealmList<Purchase> rList = new RealmList<>();
            catList.setPurchases(rList);
            // clear out all purchases associated with previous catList
            RealmResults<Purchase> oldPurchases = realm.where(Purchase.class).isNull("date").findAll();
            oldPurchases.deleteAllFromRealm();
        });

        /*
         * This while loop runs while first RealmList is nonempty. It takes the first category and
         * uses it to search for all Purchases with that category. Sum up all the purchase amounts
         * associated with that category and store that category and purchase amount in a new
         * Purchase object. Add this object to a new RealmList. Then, delete all instances of that
         * category from first RealmList. After end of while loop, listOfPurchase(1st) will be empty
         * and newRList(2nd) will have each unique category with total purchase amounts.
         */

        CategoryList catList = realm.where(CategoryList.class)
                .equalTo("name", summMonth.getName()).findFirst();

        while (listCopy.size() != 0) {
            String category = listCopy.get(0).getCategory();
            RealmResults<Purchase> results =
                    realm.where(Purchase.class).equalTo("category", category).findAll();

            double totalCatAmount = results.sum("purchaseAmount").doubleValue();
            Purchase purchase = new Purchase(totalCatAmount, category);

            realm.executeTransaction((realm) -> catList.addToPurchases(purchase));

            // have to iterate through listCopy to delete all instances of same category or else
            // listCopy will not decrease in size
            for (int i = 0; i < listCopy.size(); i++) {
                Purchase currPurchase = listCopy.get(i);
                if (currPurchase.getCategory().equals(category)) {
                    listCopy.remove(currPurchase);
                    i--;
                }
            }
        }

        RealmResults<Purchase> finalPurchaseOrder =
                catList.getPurchases().sort("purchaseAmount", Sort.DESCENDING);

        // fill array for case where user had less than 3 categories and case where user
        // had 3 or more categories
        int upperBound;
        if (finalPurchaseOrder.size() < 3) {
            upperBound = finalPurchaseOrder.size() * 2;
        } else {
            upperBound = 6;
        }

        for (int i = 0; i < upperBound; i++) {
            Purchase tmp = finalPurchaseOrder.get(i/2);
            top3Array[i] = tmp.getCategory();
            // use big decimal for rounding doubles when printing out purchase amount
            Double doubleAmount = tmp.getPurchaseAmount();
            BigDecimal bd = new BigDecimal(doubleAmount).setScale(2, RoundingMode.HALF_UP);
            doubleAmount = bd.doubleValue();
            // use decimal format to ensure two decimal places
            DecimalFormat df = new DecimalFormat("0.00");
            String dollarForm = df.format(doubleAmount);
            top3Array[++i] = "$" + dollarForm;
        }

        if (finalPurchaseOrder.size() < 3) {
            for (int i = upperBound; i < 6; i++) {
                String emptyString = "";
                top3Array[i] = emptyString;
            }
        }

        grid = findViewById(R.id.top3);

        ArrayAdapter<String> top3Adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, top3Array);
        grid.setAdapter(top3Adapter);
    }

    /**
     * This sends user to progress activity for that specific month.
     */
    public void dailyBreakdown(View view) {
        BudgetMonth summMonth =
                realm.where(BudgetMonth.class).contains("name", name).findFirst();

        Intent progressIntent = new Intent(this, ProgressActivity.class);
        // give month number to progressIntent to display correct month's progress
        progressIntent.putExtra("monthNum", summMonth.getMonthNumber());
        startActivity(progressIntent);
    }

    /**
     * This allows user to view all purchases within month.
     */
    public void viewMonthPurchases(View view) {
        BudgetMonth summMonth =
                realm.where(BudgetMonth.class).contains("name", name).findFirst();

        Intent expendIntent = new Intent(this, ExpendituresActivity.class);
        expendIntent.putExtra("date", summMonth.getName());
        expendIntent.putExtra("wholeMonth", true);
        startActivity(expendIntent);
    }
}
