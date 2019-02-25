# Budget App

## Installation
Download included apk file on an android device and allow permissions for the app.

## About This App
This app allows the user to track their income on a day to day basis. Each expenditure is tracked and can be checked by looking
at a month's progress. The user can also add money to be saved and spent every time they receive income. The data is saved
using the Realm database. Each BudgetMonth the user holds money to be saved, spent, and a list of Purchases. Each Purchase
holds a date, category, and purchase amount.

## Features
From the Main Screen, the user has 4 options. The user can set a budget, look at the day-to-day progress for the month,
submit an expenditure, or look at a monthly summary. 
  - Setting a budget takes us to the Input Screen. There the user chooses the month and sets a spending and saving amount
  split for their most recent income. If no month is present in spinner, then user must add new month from the 2nd set of 
  spinners. Then, the user can submit the budget and add to the month's spending and saving totals. 
  - The Progress Screen contains a recycler view of each day in the current month. This screen also tells the user how much
  they've spent this month and also indicates if the user is on budget, based off their spending amount for the month. 
    - Clicking on an individual day card will take the user to an Expenditure Screen which holds a list of all the 
    expenditures for that day. 
  - Each expenditure requires an amount and category to be submitted and takes the
  current day as the date. 
  - Finally, selecting a summary month from the spinner at the bottom of the Main Screen will give an
  overview of that month. From the Summary Screen, the user can check the day-to-day progress for that month or view a list
  of all the expenditures for that month.

## Screenshots
<img src="https://raw.githubusercontent.com/dennisvoo/BudgetApp/master/screenshots/MainActivity.png" 
width="320" height="600" />
<img src="https://raw.githubusercontent.com/dennisvoo/BudgetApp/master/screenshots/InputActivity.png" 
width="320" height="600" />
<img src="https://raw.githubusercontent.com/dennisvoo/BudgetApp/master/screenshots/ProgressActivity.png" 
width="320" height="600" />
<img src="https://raw.githubusercontent.com/dennisvoo/BudgetApp/master/screenshots/ExpenditureActivity.png" 
width="320" height="600" />
<img src="https://raw.githubusercontent.com/dennisvoo/BudgetApp/master/screenshots/SummaryActivity.png" 
width="320" height="600" />
