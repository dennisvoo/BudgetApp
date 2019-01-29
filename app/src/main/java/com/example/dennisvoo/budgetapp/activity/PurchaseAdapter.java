package com.example.dennisvoo.budgetapp.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dennisvoo.budgetapp.R;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.DayAdapterViewHolder> {

    private String[] purchaseInfoList;

    /**
     * Inner class that caches children views for a purchase list item.
     */
    public class DayAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView purchaseTextView;

        public DayAdapterViewHolder(View view) {
            super(view);
            purchaseTextView = view.findViewById(R.id.tv_purchase);
        }
    }

    /**
     * Called each time a ViewHolder is created. When RecyclerView loads, this will create enough
     * ViewHolders to display info and scroll.
     */

    @Override
    public DayAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.purchase_item;
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
        String purchaseInfo = purchaseInfoList[position];
        holder.purchaseTextView.setText(purchaseInfo);
    }

    /**
     * Returns number of items to display.
     */
    @Override
    public int getItemCount() { return purchaseInfoList.length; }

    /**
     * Method to set up month's list of days with budget information.
     */
    public void setListOfPurchases(String[] purchaseInfoList) {
        this.purchaseInfoList = purchaseInfoList;
        notifyDataSetChanged();
    }
}
