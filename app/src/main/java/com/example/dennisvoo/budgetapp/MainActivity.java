package com.example.dennisvoo.budgetapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    TextView todayDateTV;
    TextView moneyLeftTV;

    private double moneyLeft;
    private String moneyLeftFormatted;

    EditText amountSpentET;
    EditText categoryET;

    Button expendituresButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab current date on system
        long date = System.currentTimeMillis();

        // Format the current date into a standard format: Day of Week, Month Day, Year
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM. d, yyyy");
        String todayDate = dateFormat.format(date);

        // Set text to our TextView object
        todayDateTV = findViewById(R.id.tv_todays_date);
        todayDateTV.setText(todayDate);

        // placeholder for money left in month. real amount is taken from current month's save amount
        moneyLeft = 10000.00;
        // formats our money_left string to display remaining funds
        moneyLeftFormatted = getString(R.string.money_left, moneyLeft);
        moneyLeftTV = findViewById(R.id.tv_money_left);
        moneyLeftTV.setText(moneyLeftFormatted);

        // disable expendituresButton on start as we need inputs to submit
        expendituresButton = findViewById(R.id.expenditures_button);
        expendituresButton.setEnabled(false);

        amountSpentET = findViewById(R.id.et_amount);
        categoryET = findViewById(R.id.et_category);
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
    private void submitExpenditures(View view) {
        double dollarAmount = Double.parseDouble(amountSpentET.getText().toString());

        moneyLeft = moneyLeft - dollarAmount;
        moneyLeftFormatted = getString(R.string.money_left, moneyLeft);
        moneyLeftTV.setText(moneyLeftFormatted);


        amountSpentET.setText("");
        categoryET.setText("");

        // do something with the category
    }
}
