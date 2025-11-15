package org.me.gcu.stewart_john_s2433454;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

    private List<CurrencyItem> items = new ArrayList<>();

    public CurrencyAdapter(List<CurrencyItem> items) {
        if (items != null) {
            this.items = items;
        }
    }

    public void updateData(List<CurrencyItem> newItems) {
        if (newItems == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = newItems;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_currency_item, parent, false);
        return new CurrencyViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        CurrencyItem item = items.get(position);

        String code = item.getCurrencyCode();
        if (code == null || code.isEmpty()) {
            code = "N/A";
        }

        holder.textCode.setText(code);
        holder.textRate.setText(String.format(Locale.UK, "%.4f", item.getRate()));
        holder.textDescription.setText(item.getTitle());

        // Simple colour coding based on rate (can tweak ranges later)
        double rate = item.getRate();
        int bgColor;
        if (rate < 1.0) {
            bgColor = Color.parseColor("#FFCDD2"); // light red
        } else if (rate < 5.0) {
            bgColor = Color.parseColor("#FFF9C4"); // light yellow
        } else if (rate < 10.0) {
            bgColor = Color.parseColor("#C8E6C9"); // light green
        } else {
            bgColor = Color.parseColor("#BBDEFB"); // light blue
        }
        holder.itemView.setBackgroundColor(bgColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder {

        TextView textCode;
        TextView textRate;
        TextView textDescription;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            textCode = itemView.findViewById(R.id.textCode);
            textRate = itemView.findViewById(R.id.textRate);
            textDescription = itemView.findViewById(R.id.textDescription);
        }
    }
}
