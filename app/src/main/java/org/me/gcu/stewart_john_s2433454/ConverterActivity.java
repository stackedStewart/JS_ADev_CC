package org.me.gcu.stewart_john_s2433454;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;


import java.util.Locale;


public class ConverterActivity extends AppCompatActivity {

    private TextView textHeader;
    private TextView textRateInfo;
    private EditText editAmount;
    private RadioButton radioGbpToOther;
    private RadioButton radioOtherToGbp;
    private Button buttonConvert;
    //private Button backButton;
    private TextView textResult;

    private String currencyCode;
    private double rate; // 1 GBP = rate * OTHER

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        // Get Data from Intent
        currencyCode = getIntent().getStringExtra("code");
        rate = getIntent().getDoubleExtra("rate", 0.0);
        String title = getIntent().getStringExtra("title");

        // Find Views
        textHeader = findViewById(R.id.textHeader);
        textRateInfo = findViewById(R.id.textRateInfo);
        editAmount = findViewById(R.id.editAmount);
        radioGbpToOther = findViewById(R.id.radioGbpToOther);
        radioOtherToGbp = findViewById(R.id.radioOtherToGbp);
        buttonConvert = findViewById(R.id.buttonConvert);
        textResult = findViewById(R.id.textResult);


        if (currencyCode == null) {
            currencyCode = "###";
        }

        // Set labels that include the code
        radioGbpToOther.setText("GBP \u2192 " + currencyCode);
        radioOtherToGbp.setText(currencyCode + " \u2192 GBP");

        // Set initial text
        textHeader.setText("GBP \u2194 " + currencyCode);
        textRateInfo.setText(String.format(Locale.UK, "1 GBP = %.4f %s", rate, currencyCode));

        // Default direction: GBP > other
        radioGbpToOther.setChecked(true);

        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                performConversion();

            }
        });

        editAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });


        // Back Button to main activity
        ImageView backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());


    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        View v = getCurrentFocus();
//        if (v instanceof EditText) {
//            int[] coordinates = new int[2];
//            v.getLocationOnScreen(coordinates);
//
//            float x = ev.getRawX() + v.getLeft() - coordinates[0];
//            float y = ev.getRawY() + v.getTop() - coordinates[1];
//
//            if (ev.getAction() == MotionEvent.ACTION_UP
//                    && (x < v.getLeft() || x >= v.getRight() || y < v.getTop() || y > v.getBottom())) {
//
//                v.clearFocus();
//                hideKeyboard();
//            }
//        }
//
//        return super.dispatchTouchEvent(ev);
//    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void performConversion() {
        String amountStr = editAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;

        }

        double resultValue;
        String resultText;

        if (radioGbpToOther.isChecked()) {
            // GBP -> GBP
            resultValue = amount * rate;
            resultText = String.format(Locale.UK, "%.2f GBP = %.2f %s", amount, resultValue, currencyCode);
        } else {
            // OTHER -> GBP
            if (rate == 0.0) {
                Toast.makeText(this, "Rate is not available", Toast.LENGTH_SHORT).show();
                return;
            }
            resultValue = amount / rate;
            resultText = String.format(Locale.UK, "%.2f %s = %.2f GBP", amount, currencyCode, resultValue);
        }

        textResult.setText(resultText);
    }

}
