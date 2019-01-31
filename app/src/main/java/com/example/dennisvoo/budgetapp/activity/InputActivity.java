package com.example.dennisvoo.budgetapp.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.model.BudgetMonth;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity implements OnItemSelectedListener {

    private Realm realm;

    EditText savingMoneyET;
    EditText spendingMoneyET;

    Spinner budgetMonthSpinner;
    Spinner newMonthSpinner;
    Spinner newYearSpinner;

    Button submitBudget;
    Button submitMonth;

    Boolean budgetMonthChosen = false; // will correspond to first spinner from left
    Boolean textEntered = false; // corresponds to the two EditTexts
    Boolean newMonthChosen = false; // corresponds to second spinner from left
    Boolean newYearChosen = false; // corresponds to final spinner from left

    ArrayList<String> budgetMonths = new ArrayList<>();
    ArrayList<String> years = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        realm = Realm.getDefaultInstance();

        // add touch listener to parent layout of activity
        findViewById(R.id.input_layout).setOnTouchListener(touchListen);

        // Set up the spinners
        createBudgetMonthSpinner();
        createNewMonthSpinner();
        createNewYearSpinner();

        // Adjust the size of the window when you open the spinners
        adjustPopUpWindow();

        submitMonth = findViewById(R.id.month_button);
        submitMonth.setOnTouchListener(touchListen);
        submitBudget = findViewById(R.id.submit_budget_button);

        // Call checkButtons to see if buttons should be enabled (will be disabled to start)
        checkButtons();

        savingMoneyET = findViewById(R.id.et_saving);
        spendingMoneyET = findViewById(R.id.et_budget);
        // add TextWatcher to our EditTexts
        savingMoneyET.addTextChangedListener(textWatcher);
        spendingMoneyET.addTextChangedListener(textWatcher);

    }

    // creates spinner of budgetMonths using an Arraylist of budgetMonth realm objects
    private void createBudgetMonthSpinner() {
        budgetMonthSpinner = findViewById(R.id.select_budget_month);

        // gives us a list of months from submitNewMonth and sorts them in chronological order
        RealmResults<BudgetMonth> listOfMonths =
                realm.where(BudgetMonth.class).sort("monthNumber").findAll();

        // clear list on each method call so we avoid repeats
        budgetMonths.clear();
        budgetMonths.add("");
        for (int i = 0; i < listOfMonths.size(); i++) {
            budgetMonths.add(listOfMonths.get(i).getName());
        }

        // now create ArrayAdapter for budget months spinner
        ArrayAdapter<String> budgetMonthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, budgetMonths);
        budgetMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        budgetMonthSpinner.setAdapter(budgetMonthAdapter);
        budgetMonthSpinner.setOnItemSelectedListener(this);
        // add a click listener to spinner
        budgetMonthSpinner.setOnTouchListener(touchListen);
    }

    // creates spinner of months using an array of month strings
    private void createNewMonthSpinner() {
        newMonthSpinner = findViewById(R.id.select_new_month);

        // Create ArrayAdapter from months array in strings.xml
        ArrayAdapter<CharSequence> newMonthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months, android.R.layout.simple_spinner_item);
        newMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newMonthSpinner.setAdapter(newMonthAdapter);
        newMonthSpinner.setOnItemSelectedListener(this);
        newMonthSpinner.setOnTouchListener(touchListen);
    }

    // creates spinner of recent years made from iterating an arraylist from 2018 to present year
    private void createNewYearSpinner() {
        newYearSpinner = findViewById(R.id.select_new_year);

        years.add("");
        // populate years ArrayList with years from 2018 to whatever the current year is
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2018; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        // now create adapter for years spinner
        ArrayAdapter<String> newYearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        newYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newYearSpinner.setAdapter(newYearAdapter);
        newYearSpinner.setOnItemSelectedListener(this);
        newYearSpinner.setOnTouchListener(touchListen);
    }

    // code to adjust the popup window made when user opens up a spinner
    private void adjustPopUpWindow() {
        /*
         * Try catch block to limit size of popup window from selecting each spinner and allowing
         * user to scroll through options
         */
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow monthYearPopUp =
                    (android.widget.ListPopupWindow) popup.get(budgetMonthSpinner);

            // Set popupWindow height to 750px or WRAP_CONTENT if that is smaller
            if (monthYearPopUp.WRAP_CONTENT < 750) {
                monthYearPopUp.setHeight(monthYearPopUp.WRAP_CONTENT);
            } else {
                monthYearPopUp.setHeight(750);
            }

            android.widget.ListPopupWindow newMonthPopUp =
                    (android.widget.ListPopupWindow) popup.get(newMonthSpinner);

            // Leave height at 750px for this spinner since we know content is larger
            newMonthPopUp.setHeight(750);

            android.widget.ListPopupWindow newYearPopUp =
                    (android.widget.ListPopupWindow) popup.get(newYearSpinner);

            if (newYearPopUp.WRAP_CONTENT < 750) {
                newYearPopUp.setHeight(newYearPopUp.WRAP_CONTENT);
            } else {
                newYearPopUp.setHeight(750);
            }
        }
        catch (NoClassDefFoundError | ClassCastException |
                NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }

    // override textWatcher to see if entry has been made in both ET's
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        // Allow our expenditure submission button to be pressed
        @Override
        public void afterTextChanged(Editable editable) {
            // Turn out entries into strings
            String moneySaveEntry = savingMoneyET.getText().toString().trim();
            String moneySpendEntry = spendingMoneyET.getText().toString().trim();

            // Check if entries are made before enabling button
            textEntered = !(moneySaveEntry.equals("") || moneySpendEntry.equals(""));

            // call checkButtons each time we edit the text
            checkButtons();

        }
    };

    // override OnTouchListener to hide ET keyboard when user clicks away from keyboard
    OnTouchListener touchListen = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (savingMoneyET.isFocused()) {
                savingMoneyET.clearFocus();
                hideKeyboard(savingMoneyET);
            }
            if (spendingMoneyET.isFocused()) {
                spendingMoneyET.clearFocus();
                hideKeyboard(spendingMoneyET);
            }
            return false;
        }
    };

    // override onItemSelected to check if a selection has been made in each spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        switch (parent.getId()) {

            case R.id.select_budget_month:
                String monthSelection = budgetMonthSpinner.getSelectedItem().toString().trim();

                budgetMonthChosen = !monthSelection.equals("");
                break;

            case R.id.select_new_month:
                String newMonthSelection = parent.getItemAtPosition(pos).toString();

                newMonthChosen = !newMonthSelection.equals("");
                break;

            case R.id.select_new_year:
                String newYearSelection = parent.getItemAtPosition(pos).toString();

                newYearChosen = !newYearSelection.equals("");
                break;
        }

        // call checkButtons on each onItemSelected instance
        checkButtons();
    }
    public void onNothingSelected(AdapterView<?> parent) {}

    /*
     * This method will control the enabling and disabling of both buttons in the activity.
     * If the corresponding booleans/flags are toggled true in our EditText and Spinner checks,
     * we can toggle the specific button on.
     */
    private void checkButtons() {
        // Enable buttons if both these booleans are true (will be false on create)
        if (budgetMonthChosen && textEntered) {
            submitBudget.setEnabled(true);
        } else {
            submitBudget.setEnabled(false);
        }

        if (newMonthChosen && newYearChosen) {
            submitMonth.setEnabled(true);
        } else {
            submitMonth.setEnabled(false);
        }
    }

    /*
     * This method submits a new Month + Year combination (from the 2nd and 3rd spinners) on click.
     * This combination is then added to the list of selections in the first spinner.
     */
    public void submitNewMonth(View view) {
        // take getSelectedItem().toString() for each spinner and combine, then add combo to first
        // spinner selection *database*

        String chosenMonth = newMonthSpinner.getSelectedItem().toString();
        int monthNumber = newMonthSpinner.getSelectedItemPosition();
        String chosenYear = newYearSpinner.getSelectedItem().toString();

        int yearNum = Integer.parseInt(chosenYear);
        String monthYearNum = combineMonthYear(monthNumber, yearNum);

        // search if we have entered our month yet
        RealmResults<BudgetMonth> budgetMonthResults = realm.where(BudgetMonth.class).
                equalTo("name", chosenMonth + " " + chosenYear).findAll();

        // if search returns an entry we notify user the month has already been added
        if (budgetMonthResults.size() > 0) {
            Toast.makeText(this, "Month already added!", Toast.LENGTH_SHORT).show();
        } else {
            // otherwise we add month to our database
            realm.executeTransaction((realm) -> {
                BudgetMonth budgetMonth = realm.createObject(BudgetMonth.class);
                budgetMonth.setName(chosenMonth + " " + chosenYear);
                budgetMonth.setMonthNumber(monthYearNum);
                budgetMonth.setAmountSaved(0.00);
                budgetMonth.setSpendingAmount(0.00);
                budgetMonth.setPurchases(new RealmList<>());
            });

            // reset the spinners involved
            newMonthSpinner.setSelection(0);
            newYearSpinner.setSelection(0);
        }

        // call this every time we add a month so we can update our budget month spinner
        createBudgetMonthSpinner();

    }

    /*
     * This method takes the month (including year) in the first spinner
     */
    public void submitBudget(View view) {
        // get current entries from budgetMonthSpinner and the two EditTexts
        String budgetMonthName = budgetMonthSpinner.getSelectedItem().toString();
        double saveAmount = Double.parseDouble(savingMoneyET.getText().toString());
        double spendAmount = Double.parseDouble(spendingMoneyET.getText().toString());

        // search for the budgetMonth in database that corresponds to our currBudgetMonth string
        BudgetMonth currMonth = realm.where(BudgetMonth.class)
                .equalTo("name", budgetMonthName).findFirst();
        // update amount saved and spending amount for the current month
        realm.executeTransaction((realm) -> {
            currMonth.setAmountSaved(currMonth.getAmountSaved() + saveAmount);
            currMonth.setSpendingAmount(currMonth.getSpendingAmount() + spendAmount);
        });

        // reset budgetMonth spinner and EditTexts
        budgetMonthSpinner.setSelection(0);
        savingMoneyET.setText("");
        spendingMoneyET.setText("");
    }

    // private helper method that combines a month and year number to give our budgetMonth an ID
    private String combineMonthYear(int month, int year) {
        String m = Integer.toString(month);
        String y = Integer.toString(year);
        return y + "" + m;
    }

    // method used to hide keyboard from EditTexts when user clicks out of keyboard
    public void hideKeyboard(View view) {
        InputMethodManager iMM =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        iMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
