package com.example.dennisvoo.budgetapp.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.example.dennisvoo.budgetapp.R;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayAdapterViewHolder> {

    private String[] monthProgress;

    // On click handler allows for interaction with RecyclerView
    private DayAdapterOnClickHandler clickHandler;

    // Interface receiving onClick messages
    public interface DayAdapterOnClickHandler {
        void onClick(String dayData);
    }

    /**
     * Constructor for DayAdapter that accepts clickHandler and stores it
     */
    public DayAdapter(DayAdapterOnClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    /**
     * Inner class that caches children views for a day list item.
     */
    public class DayAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView dayTextView;

        public DayAdapterViewHolder(View view) {
            super(view);
            dayTextView = view.findViewById(R.id.tv_day);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String dayData = monthProgress[getAdapterPosition()];
            clickHandler.onClick(dayData);
        }
    }

    /**
     * Called each time a ViewHolder is created. When RecyclerView loads, this will create enough
     * ViewHolders to display info and scroll.
     */

    @Override
    public DayAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.day_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new DayAdapterViewHolder(view);
    }

    /**
     * Called by RecyclerView to display items from ViewHolders at the position.
     */
    @Override
    public void onBindViewHolder(DayAdapterViewHolder holder, int position) {
        String dayData = monthProgress[position];
        holder.dayTextView.setText(dayData);
    }

    /**
     * Returns number of items to display.
     */
    @Override
    public int getItemCount() { return monthProgress.length; }

    /**
     * Method to set up month's list of days with budget information.
     */
    public void setMonthProgress(String[] monthProgress) {
        this.monthProgress = monthProgress;
        notifyDataSetChanged();
    }
}
