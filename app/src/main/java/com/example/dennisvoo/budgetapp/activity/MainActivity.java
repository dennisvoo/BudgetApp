package com.example.dennisvoo.budgetapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.activity.InputActivity;
import com.example.dennisvoo.budgetapp.model.BudgetMonth;
import com.example.dennisvoo.budgetapp.model.Purchase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.*;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    TextView todayDateTV;
    TextView moneyLeftTV;

    private double moneyLeft;
    private String moneyLeftFormatted;

    EditText amountSpentET;
    EditText categoryET;

    Button expendituresButton;

    double dollarAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.dennisvoo.budgetapp.R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        // Grab current date on system
        Date date = new Date();

        // Format the current date into a standard format: Day of Week, Month Day, Year
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM. dd, yyyy");
        String todayDate = dateFormat.format(date);

        // Set text to our TextView object
        todayDateTV = findViewById(com.example.dennisvoo.budgetapp.R.id.tv_todays_date);
        todayDateTV.setText(todayDate);

        // placeholder for money left in month. real amount is taken from current month's save amount

        moneyLeft = 0.00;
        // formats our money_left string to display remaining funds
        moneyLeftFormatted = getString(com.example.dennisvoo.budgetapp.R.string.money_left, moneyLeft);
        moneyLeftTV = findViewById(com.example.dennisvoo.budgetapp.R.id.tv_money_left);
        moneyLeftTV.setText(moneyLeftFormatted);

        // disable expendituresButton on start as we need inputs to submit
        expendituresButton = findViewById(com.example.dennisvoo.budgetapp.R.id.expenditures_button);
        expendituresButton.setEnabled(false);

        amountSpentET = findViewById(com.example.dennisvoo.budgetapp.R.id.et_amount);
        categoryET = findViewById(com.example.dennisvoo.budgetapp.R.id.et_category);
        // add TextWatcher to our EditTexts
        amountSpentET.addTextChangedListener(textWatcher);
        categoryET.addTextChangedListener(textWatcher);
    }

    /*
     * This method, paired with android:onClick in the xml file, will direct us to the InputActivity
     * on click.
     */
    public void inputMe(View view) {
        Intent inputIntent = new Intent(this, InputActivity.class);
        startActivity(inputIntent);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        // Allow our expenditure submission button to be pressed
        @Override
        public void afterTextChanged(Editable editable) {
            // get text entered in both EditTexts and turn them into strings (excluding whitespace)
            String amountEntry = amountSpentET.getText().toString().trim();
            String categoryEntry = categoryET.getText().toString().trim();

            // Check if our EditTexts are not empty (or just whitespace) before enabling button
            if (amountEntry.equals("") || categoryEntry.equals("")) {
                expendituresButton.setEnabled(false);
            } else {
                expendituresButton.setEnabled(true);
                // button is being enabled when just one edittext changes
            }
        }
    };

    /*
     * This method allows us to take data in amountSpentET and categoryET and store them as a
     * pair. This will affect our budget and decrease our moneyLeft as well.
     */
    public void submitExpenditures(View view) {
        dollarAmount = Double.parseDouble(amountSpentET.getText().toString());

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss z");
        String dateEntered = dateFormat.format(date);

        int monthNum = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int yearNum = Calendar.getInstance().get(Calendar.YEAR);

        String monthYearNum = combineMonthYear(monthNum,yearNum);

        // check if we have this month in budgetMonth database
        RealmResults<BudgetMonth> budgetMonthResults = realm.where(BudgetMonth.class).
                equalTo("monthNumber", monthYearNum).findAll();

        // if user has not input this month, we cannot add the purchase
        if (budgetMonthResults.size() == 0) {
            Toast.makeText(this,
                    "Need to add current month first, click 'Set a budget for the week' button",
                    Toast.LENGTH_LONG).show();
        } else {
            // we can add the purchase otherwise
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Purchase purchase = realm.createObject(Purchase.class);
                    purchase.setDate(dateEntered);
                    purchase.setPurchaseAmount(dollarAmount);
                    purchase.setCategory(categoryET.getText().toString());
                    // search for current month's BudgetMonth realm object in database
                    BudgetMonth currentMonth = realm.where(BudgetMonth.class)
                            .equalTo("monthNumber",monthYearNum).findFirst();
                    // then we can add our newest purchase to that month's RealmList<Purchase>
                    currentMonth.addToPurchases(purchase);
                }
            });

            // update how much money is left to spend for the month
            moneyLeft = moneyLeft - dollarAmount;
            moneyLeftFormatted = getString(R.string.money_left, moneyLeft);
            moneyLeftTV.setText(moneyLeftFormatted);

            // clear out fields after entering
            amountSpentET.setText("");
            categoryET.setText("");
        }

    }

    // private helper method that combines a month and year number to give our budgetMonth an ID
    private String combineMonthYear(int month, int year) {
        String m = Integer.toString(month);
        String y = Integer.toString(year);
        return y + "" + m;
    }
}
