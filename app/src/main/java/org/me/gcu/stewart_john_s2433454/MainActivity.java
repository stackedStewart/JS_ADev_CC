/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 _________________
// Student ID           _________________
// Programme of Study   _________________
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.stewart_john_s2433454;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView rawDataDisplay;
    private Button startButton;
    private String result;
    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";

    // NEW: changed to separate full + filtered lists
    private List<CurrencyItem> allCurrencyItems = new ArrayList<>();
    private List<CurrencyItem> filteredCurrencyItems = new ArrayList<>();
    // NEW
    private RecyclerView recyclerView;
    private CurrencyAdapter currencyAdapter;
    private SearchView searchView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        recyclerView = findViewById(R.id.recyclerViewCurrencies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currencyAdapter = new CurrencyAdapter(currencyItems);
        recyclerView.setAdapter(currencyAdapter);

        // when a row is clicked, open Converter Activity
        currencyAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, ConverterActivity.class);
            intent.putExtra("code", item.getCurrencyCode());
            intent.putExtra("rate", item.getRate());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        });
    }

    @Override
    public void onClick(View v) {
        startProgress();
    }

    public void startProgress() {
        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    }

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable {

        private String url;

        public Task(String aurl) {
            url = aurl;
        }

        @Override
        public void run() {

            // Clear previous result
            result = "";

            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine;

            Log.d("MyTask", "in run");

            try {
                Log.d("MyTask", "in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    result = result + inputLine;
                }
                in.close();
            } catch (IOException ae) {
                Log.e("MyTask", "ioexception: " + ae);
            }

            // Clean up any leading garbage characters
            int i = result.indexOf("<?"); // initial tag
            if (i >= 0) {
                result = result.substring(i);
            }

            // Clean up any trailing garbage at the end of the file
            int endIndex = result.indexOf("</rss>"); // final tag
            if (endIndex >= 0) {
                result = result.substring(0, endIndex + 6);
            }

            // Now that you have the xml data into result, you can parse it
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(result));

                int eventType = xpp.getEventType();

                // local list to build on this background thread
                List<CurrencyItem> parsedItems = new ArrayList<>();

                CurrencyItem currentItem = null;
                String currentText = "";
                String lastBuildDate = "";

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    switch (eventType) {

                        case XmlPullParser.START_TAG:
                            String startTag = xpp.getName();
                            if ("item".equalsIgnoreCase(startTag)) {
                                // new currency entry
                                currentItem = new CurrencyItem();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            currentText = xpp.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            String endTag = xpp.getName();

                            if ("lastBuildDate".equalsIgnoreCase(endTag)) {
                                // overall feed timestamp
                                lastBuildDate = currentText.trim();
                            }

                            if (currentItem != null && endTag != null) {
                                if ("title".equalsIgnoreCase(endTag)) {
                                    currentItem.setTitle(currentText.trim());
                                } else if ("description".equalsIgnoreCase(endTag)) {
                                    currentItem.setDescription(currentText.trim());
                                } else if ("pubDate".equalsIgnoreCase(endTag)) {
                                    currentItem.setPubDate(currentText.trim());
                                } else if ("link".equalsIgnoreCase(endTag)) {
                                    currentItem.setLink(currentText.trim());
                                } else if ("item".equalsIgnoreCase(endTag)) {
                                    // finished one <item>
                                    parsedItems.add(currentItem);
                                    currentItem = null;
                                }
                            }
                            break;
                    }

                    eventType = xpp.next();
                }

                currencyItems = parsedItems;


                // Update UI from the background thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("UI thread", "I am the UI thread (RecyclerView view)");
                        currencyAdapter.updateData(currencyItems);
                    }
                });

            } catch (XmlPullParserException e) {
                Log.e("Parsing", "EXCEPTION " + e);
            } catch (Exception e) {
                // catch-all just in case something else goes wrong while parsing
                Log.e("Parsing", "Unexpected exception " + e);
            }
        }
    }
}