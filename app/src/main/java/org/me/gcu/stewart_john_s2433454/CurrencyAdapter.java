package org.me.gcu.stewart_john_s2433454;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

    private List<CurrencyItem> items;

    public CurrencyAdapter(List<CurrencyItem> items) {
        this.items = items;
    }


    // Added a click listener interface + field *Added 15 Nov
    // NEW: click listener
    public interface OnItemClickListener {
        void onItemClick(CurrencyItem item);
    }

    private OnItemClickListener listener;

    // Setter added for listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void updateData(List<CurrencyItem> newItems) {
        if (newItems == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = newItems;
        }
        notifyDataSetChanged();
    }

    private int getFlagResource(String code) {
        if (code == null) {
            return R.drawable.flag_unknown;
        }

        switch (code.toUpperCase()) {
            case "GBP":
                return R.drawable.flag_gbp;
            case "USD":
                return R.drawable.flag_usd;
            case "EUR":
                return R.drawable.flag_eur;
            case "JPY":
                return R.drawable.flag_jpy;
            case "AED":
                return R.drawable.flag_aed;
            case "AUD":
                return R.drawable.flag_aud;
            case "CAD":
                return R.drawable.flag_cad;
            case "CHF":
                return R.drawable.flag_chf;
            case "CNY":
                return R.drawable.flag_cny;
            case "INR":
                return R.drawable.flag_inr;
            case "BRL":
                return R.drawable.flag_brl;
            case "ZAR":
                return R.drawable.flag_zar;
            case "NZD":
                return R.drawable.flag_nzd;
            // add more cases if you have more flags
            default:
                return R.drawable.flag_unknown;
        }
    }


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
        holder.imageFlag.setImageResource(getFlagResource(code));

        // Hooked the click into onBindViewHandler
        // NEW: handling clicks * added nov 15
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageFlag;
        TextView textCode;
        TextView textRate;
        TextView textDescription;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFlag = itemView.findViewById(R.id.imageFlag);
            textCode = itemView.findViewById(R.id.textCode);
            textRate = itemView.findViewById(R.id.textRate);
            textDescription = itemView.findViewById(R.id.textDescription);
        }
    }
}
