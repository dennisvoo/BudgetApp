package com.example.dennisvoo.budgetapp;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

public class InputActivity extends AppCompatActivity implements OnItemSelectedListener {

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

    ArrayList<String> years = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // Set up the spinners
        createBudgetMonthSpinner();
        createNewMonthSpinner();
        createNewYearSpinner();

        // Adjust the size of the window when you open the spinners
        adjustPopUpWindow();

        submitMonth = findViewById(R.id.month_button);
        submitBudget = findViewById(R.id.submit_budget_button);

        // Call checkButtons to see if buttons should be enabled (will be disabled to start)
        checkButtons();

        savingMoneyET = findViewById(R.id.et_budget);
        spendingMoneyET = findViewById(R.id.et_saving);

        // add TextWatcher to our EditTexts
        savingMoneyET.addTextChangedListener(textWatcher);
        spendingMoneyET.addTextChangedListener(textWatcher);

    }

    private void createBudgetMonthSpinner() {
        budgetMonthSpinner = findViewById(R.id.select_budget_month);

        // Create ArrayAdapter from months array in strings.xml
        ArrayAdapter<CharSequence> budgetMonthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months, android.R.layout.simple_spinner_item);
        budgetMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        budgetMonthSpinner.setAdapter(budgetMonthAdapter);
        budgetMonthSpinner.setOnItemSelectedListener(this);
    }

    private void createNewMonthSpinner() {
        newMonthSpinner = findViewById(R.id.select_new_month);

        // Create ArrayAdapter from months array in strings.xml
        ArrayAdapter<CharSequence> newMonthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months, android.R.layout.simple_spinner_item);
        newMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newMonthSpinner.setAdapter(newMonthAdapter);
        newMonthSpinner.setOnItemSelectedListener(this);
    }

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
    }

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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        switch (parent.getId()) {

            case R.id.select_budget_month:
                Toast.makeText(this, parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
                String monthSelection = budgetMonthSpinner.getSelectedItem().toString();


                budgetMonthChosen = !monthSelection.equals("");
                break;

            case R.id.select_new_month:
                Toast.makeText(this, parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
                String newMonthSelection = parent.getItemAtPosition(pos).toString();

                newMonthChosen = !newMonthSelection.equals("");
                break;

            case R.id.select_new_year:
                Toast.makeText(this, parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
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

    }

    /*
     * This method takes the month (including year) in the first spinner
     */
    public void submitBudget(View view) {
        // take first spinner selection and amounts from each EditText and modify database
    }

}
