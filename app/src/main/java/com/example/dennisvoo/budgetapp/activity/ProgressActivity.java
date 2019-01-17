package com.example.dennisvoo.budgetapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dennisvoo.budgetapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

public class ProgressActivity extends AppCompatActivity implements DayAdapter.DayAdapterOnClickHandler {

    private RecyclerView dayList;
    private DayAdapter dayAdapter;
    LinearLayoutManager layoutManager;

    String[] monthProgress;

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
        cal.set(Calendar.MINUTE,0);
        date = cal.getTime();

        // format date into a string format
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");

        // get maximum number of days in month to set size of monthProgress array
        int currMonth = cal.get(Calendar.MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        monthProgress = new String[daysInMonth];

        // iterate through days of month to fill monthProgress array
        while (currMonth == cal.get(Calendar.MONTH)) {
            String currDay = dateFormat.format(date);
            monthProgress[cal.get(Calendar.DAY_OF_MONTH) -  1] = currDay + " " + "Amount spent:";
            cal.add(Calendar.DAY_OF_MONTH, 1);
            date = cal.getTime();
        }

        dayAdapter.setMonthProgress(monthProgress);
        // set scroll position to current day
        setToToday();
    }

    /**
     * On click, this method handles click on a ViewHolder by sending user to that day's list of
     * purchases
     */
    public void onClick(String dayData) {
        // set it to clickable. create intent and send to list of purchases.
        Toast.makeText(this, dayData,Toast.LENGTH_SHORT).show();
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
     * helper method that sets recyclerview scroll position to the current day
     */
    private void setToToday() {
        Calendar cal = Calendar.getInstance();
        int currDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
        layoutManager.scrollToPositionWithOffset(currDay,0);
    }
}
