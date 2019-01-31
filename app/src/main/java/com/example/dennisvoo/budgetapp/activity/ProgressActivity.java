package com.example.dennisvoo.budgetapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.dennisvoo.budgetapp.R;
import com.example.dennisvoo.budgetapp.model.BudgetMonth;
import com.example.dennisvoo.budgetapp.model.Purchase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class ProgressActivity extends AppCompatActivity implements DayAdapter.DayAdapterOnClickHandler {

    private RecyclerView dayList;
    private DayAdapter dayAdapter;
    LinearLayoutManager layoutManager;

    String[] monthProgress;
    Double[] dailyAmountSpent;

    private Realm realm;

    // vars needed to set up Amount Spent This Month: header
    TextView moneySpentMonthTV;
    private double moneySpentMonth;
    private String moneySpentMonthFormatted;

    // vars needed to set up On Budget: header
    TextView onBudgetTV;
    private boolean onBudget;
    private String onBudgetFormatted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        realm = realm.getDefaultInstance();

        dayList = findViewById(R.id.recyclerview_days);

        layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        dayList.setLayoutManager(layoutManager);
        dayList.setHasFixedSize(true);

        dayAdapter = new DayAdapter(this);
        dayList.setAdapter(dayAdapter);

        loadMonthProgress();
    }

    /**
     *  Populates dayList after creating an array monthProgress of necessary info
     */
    private void loadMonthProgress() {
        // set a calendar to the first day of current month and get that date
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        date = cal.getTime();

        // format date into a string format
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");

        // get maximum number of days in month to set size of monthProgress array
        int currMonth = cal.get(Calendar.MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        monthProgress = new String[daysInMonth];

        // create an array of doubles w/ same size to hold total amount spent on each day
        dailyAmountSpent = new Double[daysInMonth];

        // iterate through days of month to fill monthProgress array
        while (currMonth == cal.get(Calendar.MONTH)) {
            // set up date string to match dates of Purchase RealmObject
            String todayStr = dateFormat.format(date);

            // search for purchases with dates on same day as our date string
            RealmResults<Purchase> purchasesInDay =
                    realm.where(Purchase.class).contains("date", todayStr).findAll();

            int indexOfDay = cal.get(Calendar.DAY_OF_MONTH) - 1;

            dailyAmountSpent[indexOfDay] =
                    purchasesInDay.sum("purchaseAmount").doubleValue();
            monthProgress[indexOfDay] =
                    todayStr + " " + "Amount spent: $" + dailyAmountSpent[indexOfDay];
            cal.add(Calendar.DAY_OF_MONTH, 1);
            date = cal.getTime();
        }

        // determine if user is on budget with array of daily amount spent
        determineIfUserOnBudget(dailyAmountSpent);

        dayAdapter.setMonthProgress(monthProgress);
        // set scroll position to current day
        setToToday();
    }

    /**
     * On click, this method handles click on a ViewHolder by sending user to that day's list of
     * purchases
     */
    public void onClick(String dayData) {
        // Cut string at end of date portion
        String clickedDate = dayData.split(" Amount")[0];

        // create intent to send to ExpendituresActivity with date clickedDate string as extra
        Intent expendIntent = new Intent(this, ExpendituresActivity.class);
        expendIntent.putExtra("date", clickedDate);
        // start intent whenever user clicks a DayViewHolder
        startActivity(expendIntent);

    }

    /**
     * Creates our menu button at top right
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.progress, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * handles what happens when user clicks refresh menu button. reset recyclerview to current day
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_jump) {
            setToToday();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * helper method that sets RecyclerView scroll position to the current day
     */
    private void setToToday() {
        Calendar cal = Calendar.getInstance();
        int currDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
        layoutManager.scrollToPositionWithOffset(currDay,0);
    }

    /**
     * Takes in the amount of doubles corresponding to daily amount spent. Adds up the doubles in
     * array and displays in header the amount calculated and whether or not the user is on budget.
     */
    private void determineIfUserOnBudget(Double[] amounts) {
        moneySpentMonth = 0.00;

        // iterate through array and add up double amounts
        for (double tmp : amounts) {
            moneySpentMonth = moneySpentMonth + tmp;
        }

        // Set up first TextView in header with string formatting and moneySpentMonth
        moneySpentMonthTV = findViewById(R.id.tv_money_spent_month);
        moneySpentMonthFormatted = getString(R.string.money_spent_month, moneySpentMonth);
        moneySpentMonthTV.setText(moneySpentMonthFormatted);

        // now calculate whether user was on budget by finding available spending for month and
        // calculating how much money user could have spent by current day and still be on budget

        // find current month
        Calendar cal = Calendar.getInstance();
        int monthNum = cal.get(Calendar.MONTH) + 1;
        int yearNum = cal.get(Calendar.YEAR);

        String m = Integer.toString(monthNum);
        String y = Integer.toString(yearNum);
        String monthYearNum = y + m;
        BudgetMonth currMonth = realm.where(BudgetMonth.class)
                .equalTo("monthNumber",monthYearNum).findFirst();

        // now calculate total spending money for current month
        double totalBudgetForMonth = currMonth.getSpendingAmount() + moneySpentMonth;

        // find daily pace by dividing by number of days in month
        double dailyPace = totalBudgetForMonth / cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // check if user is on budget by calculating how much user could have spent by current day
        // then compare to amount spent for month to see if user is on budget
        int currDay = cal.get(Calendar.DAY_OF_MONTH);
        if ((dailyPace * currDay) >= moneySpentMonth) {
            onBudget = true;
        } else {
            onBudget = false;
        }

        // then set up on budget TextView
        onBudgetTV = findViewById(R.id.tv_on_budget);
        if (onBudget) {
            onBudgetFormatted = getString(R.string.on_budget_state, "Yes");
        } else {
            onBudgetFormatted = getString(R.string.on_budget_state, "No");
        }
        onBudgetTV.setText(onBudgetFormatted);
    }

}
