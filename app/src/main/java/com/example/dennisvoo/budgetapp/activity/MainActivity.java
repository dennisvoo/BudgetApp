package com.example.dennisvoo.budgetapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.model.BudgetMonth;
import com.example.dennisvoo.budgetapp.model.Purchase;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.*;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

    private Realm realm;
    BudgetMonth currMonth;

    TextView todayDateTV;
    TextView moneyLeftTV;

    private double moneyLeft;
    private String moneyLeftFormatted;

    EditText amountSpentET;
    EditText categoryET;

    Button expendituresButton;

    double dollarAmount;

    Spinner summarySpinner;
    ArrayList<String> summaries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.dennisvoo.budgetapp.R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        // Grab current date on system
        Date date = new Date();

        // Format the current date into a standard format: Day of Week, Month Day, Year
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM. d, yyyy");
        String todayDate = dateFormat.format(date);

        // Set up TextView objects
        todayDateTV = findViewById(com.example.dennisvoo.budgetapp.R.id.tv_todays_date);
        todayDateTV.setText(todayDate);
        moneyLeftTV = findViewById(com.example.dennisvoo.budgetapp.R.id.tv_money_left);

        // disable expendituresButton on start as we need inputs to submit
        expendituresButton = findViewById(com.example.dennisvoo.budgetapp.R.id.expenditures_button);
        expendituresButton.setEnabled(false);

        amountSpentET = findViewById(com.example.dennisvoo.budgetapp.R.id.et_amount);
        categoryET = findViewById(com.example.dennisvoo.budgetapp.R.id.et_category);
        // add TextWatcher and onFocusChangeListener to our ET's
        amountSpentET.addTextChangedListener(textWatcher);
        categoryET.addTextChangedListener(textWatcher);

        // add touch listener to parent layout of activity and to buttons
        findViewById(R.id.main_layout).setOnTouchListener(touchListen);
        findViewById(R.id.input_button).setOnTouchListener(touchListen);
        findViewById(R.id.progress_button).setOnTouchListener(touchListen);
        expendituresButton.setOnTouchListener(touchListen);
    }

    /*
     * We override onResume to make sure that our Money Left for Month is always updated when we go
     * back to MainActivity
     */
    @Override
    protected void onResume() {
        super.onResume();
        currMonth = findCurrentMonth();
        if (currMonth == null) {
            // if we haven't made a BudgetMonth for current month, we'll just default to 0.00
            moneyLeft = 0.00;
        } else {
            // otherwise we take the spending amount available for the current month
            moneyLeft = currMonth.getSpendingAmount();
        }

        // formats our money_left string to display remaining funds
        moneyLeftFormatted = getString(com.example.dennisvoo.budgetapp.R.string.money_left, moneyLeft);
        moneyLeftTV.setText(moneyLeftFormatted);

        // set up spinner of summary months and adjust its pop-up window
        createSummarySpinner();
        adjustPopUpWindow();
    }

    /*
     * This method, paired with android:onClick in the xml file, will direct us to the InputActivity
     * on click.
     */
    public void inputMe(View view) {
        Intent inputIntent = new Intent(this, InputActivity.class);
        startActivity(inputIntent);
    }

    /*
     * This method, paired with the progress_button via android:onClick, directs us to the
     * ProgressActivity on click.
     */
    public void checkProgress(View view) {
        currMonth = findCurrentMonth();
        
        if (currMonth == null) {
            Toast.makeText(this,
                    "Need to add current month first, click 'Set a budget for the week' button",
                    Toast.LENGTH_LONG).show();
        } else {
            Intent progressIntent = new Intent(this, ProgressActivity.class);
            startActivity(progressIntent);
        }
    }

    // override textWatcher to check if EditText is empty or not
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
                // button is being enabled when just one editText changes
            }
        }
    };

    // override OnTouchListener to hide ET keyboard when user clicks away from keyboard
    OnTouchListener touchListen = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (amountSpentET.isFocused()) {
                amountSpentET.clearFocus();
                hideKeyboard(amountSpentET);
            }
            if (categoryET.isFocused()) {
                categoryET.clearFocus();
                hideKeyboard(categoryET);
            }
            return false;
        }
    };

    /*
     * This method allows us to take data in amountSpentET and categoryET and store them as a
     * pair. This will affect our budget and decrease our moneyLeft as well.
     */
    public void submitExpenditures(View view) {
        dollarAmount = Double.parseDouble(amountSpentET.getText().toString());

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        String dateEntered = dateFormat.format(date);

        // check if we have this month in budgetMonth database
        currMonth = findCurrentMonth();

        // if current month is not in database, we cannot add the purchase
        if (currMonth == null) {
            Toast.makeText(this,
                    "Need to add current month first, click 'Set a budget for the week' button",
                    Toast.LENGTH_LONG).show();
        } else {
            // we can add the purchase otherwise

            // update how much money is left to spend for the month
            moneyLeft = moneyLeft - dollarAmount;
            moneyLeftFormatted = getString(R.string.money_left, moneyLeft);
            moneyLeftTV.setText(moneyLeftFormatted);

            // then update on Realm
            realm.executeTransaction((realm) -> {
                String cat = categoryET.getText().toString().trim();
                Purchase purchase = new Purchase(dateEntered, dollarAmount, cat);

                // update spending amount for the month
                currMonth.setSpendingAmount(moneyLeft);
                // we can add our newest purchase to this month's RealmList<Purchase>
                currMonth.addToPurchases(purchase);
            });

            // clear out fields after entering
            amountSpentET.setText("");
            categoryET.setText("");
        }

    }

    /*
     * This method creates spinner for the summary of all BudgetMonths in database.
     */
    private void createSummarySpinner() {
        summarySpinner = findViewById(R.id.select_summary);

        // gives us a list of months from submitNewMonth and sorts them in chronological order
        RealmResults<BudgetMonth> listOfMonths =
                realm.where(BudgetMonth.class).sort("monthNumber").findAll();

        // clear list on each method call so we avoid repeats
        summaries.clear();
        summaries.add("");
        for (int i = 0; i < listOfMonths.size(); i++) {
            summaries.add(listOfMonths.get(i).getName());
        }

        // now create ArrayAdapter for budget months spinner
        ArrayAdapter<String> summaryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, summaries);
        summaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        summarySpinner.setAdapter(summaryAdapter);
        summarySpinner.setOnItemSelectedListener(this);
    }

    // override onItemSelected handle sending user to SummaryActivity
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String summarySelection = parent.getSelectedItem().toString().trim();

        if (summarySelection != "") {
            Intent summaryIntent = new Intent(this, SummaryActivity.class);
            summaryIntent.putExtra("name", summarySelection);
            startActivity(summaryIntent);
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {}

    /*
     * Try catch block to limit size of popup window from the spinner and allowing
     * user to scroll through options
     */
    private void adjustPopUpWindow() {
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow summaryPopUp =
                    (android.widget.ListPopupWindow) popup.get(summarySpinner);

            // Set popupWindow height to 750px or WRAP_CONTENT if that is smaller
            if (summaryPopUp.WRAP_CONTENT < 750) {
                summaryPopUp.setHeight(summaryPopUp.WRAP_CONTENT);
            } else {
                summaryPopUp.setHeight(750);
            }

        }
        catch (NoClassDefFoundError | ClassCastException |
                NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }

    /*
     * private helper method that combines current month+year number to give our budgetMonth an ID
     */
    private BudgetMonth findCurrentMonth() {
        int monthNum = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int yearNum = Calendar.getInstance().get(Calendar.YEAR);

        String m = Integer.toString(monthNum);
        String y = Integer.toString(yearNum);
        String monthYearNum = y + m;

        // search for current month's BudgetMonth realm object in database
        return realm.where(BudgetMonth.class)
                .equalTo("monthNumber",monthYearNum).findFirst();
    }

    /*
     * method used to hide keyboard from EditTexts when user clicks out of keyboard
     */
    public void hideKeyboard(View view) {
        InputMethodManager iMM =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        iMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
