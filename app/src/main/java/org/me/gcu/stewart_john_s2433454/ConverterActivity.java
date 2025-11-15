package org.me.gcu.stewart_john_s2433454;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class ConverterActivity extends AppCompatActivity {

    private TextView textHeader;
    private TextView textRateInfo;
    private EditText editAmount;
    private RadioButton radioGbpToOther;
    private RadioButton radioOtherToGbp;
    private Button buttonConvert;
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

        // Set initial text
        textHeader.setText("GBP ↔ " + currencyCode);
        textRateInfo.setText(String.format(Locale.UK, "1 GBP = %.4f %s", rate, currencyCode));

        // Default direction: GBP > other
        radioGbpToOther.setChecked(true);

        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performConversion();
            }
        });
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
