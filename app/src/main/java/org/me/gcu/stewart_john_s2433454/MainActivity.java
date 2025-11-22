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
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView rawDataDisplay;
    private Button startButton;
    private String result;
    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";
    private List<CurrencyItem> allCurrencyItems = new ArrayList<>();
    private List<CurrencyItem> filteredCurrencyItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private CurrencyAdapter currencyAdapter;
    private SearchView searchView;


    private RecyclerView mainRecyclerView;
    private CurrencyAdapter mainCurrencyAdapter;

    private List<CurrencyItem> mainCurrencyItems = new ArrayList<>();

    // Threading and auto-update
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final long REFRESH_INTERVAL_MS = 10 * 60 * 1000; // this equals 10 minutes

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            startProgress(); // fetch latest data
            // Schedule for next refresh
            mainHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        searchView = findViewById(R.id.searchView);

        recyclerView = findViewById(R.id.recyclerViewCurrencies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currencyAdapter = new CurrencyAdapter(filteredCurrencyItems);
        recyclerView.setAdapter(currencyAdapter);

        // Added 18-11
        mainRecyclerView = findViewById(R.id.recyclerViewMainCurrencies);
        LinearLayoutManager mainLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mainRecyclerView.setLayoutManager(mainLayoutManager);

        mainCurrencyAdapter = new CurrencyAdapter(mainCurrencyItems);
        mainRecyclerView.setAdapter(mainCurrencyAdapter);

//        TextView swipeHint = findViewById(R.id.textSwipeHint);
//
//        mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            boolean hidden = false;
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                if (!hidden && dx != 0 && swipeHint != null) { // they scrolled sideways
//                    swipeHint.setVisibility(View.GONE);
//                    hidden = true;
//                }
//            }
//                                              });

        CurrencyAdapter.OnItemClickListener clickListener = item -> {
            Intent intent = new Intent(MainActivity.this, ConverterActivity.class);
            intent.putExtra("code", item.getCurrencyCode());
            intent.putExtra("rate", item.getRate());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        };

        // Added 18-11
        currencyAdapter.setOnItemClickListener(clickListener);
        mainCurrencyAdapter.setOnItemClickListener(clickListener);

        // set up search behaviour
        setupSearch();

        // Auto-load data when the app starts
        startProgress();

        // Scheduling periodic refresh
        mainHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL_MS);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCurrencies(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCurrencies(newText);
                return true;
            }

        });
    }


    private String expandCountryAliases(String query) {
        if (query == null) return "";
        String q = query.toLowerCase(Locale.ROOT).trim();

        // Map country nouns -> adjectives / common RSS words
        switch (q) {
            case "china":
                return "china chinese";
            case "japan":
                return "japan japanese";
            case "switzerland":
                return "switzerland swiss";
            case "united states":
            case "usa":
            case "america":
                return "united states usa american";
            case "united kingdom":
            case "uk":
            case "britain":
            case "england":
                return "united kingdom uk britain british";
            case "europe":
            case "eu":
                return "europe eu euro";
            case "uae":
            case "united arab emirates":
                return "uae emirates emirati";
            case "australia":
                return "australia australian";
            case "canada":
                return "canada canadian";
            case "india":
                return "india indian";
            case "brazil":
                return "brazil brazilian";
            case "south africa":
                return "south africa african";
            case "new zealand":
                return "new zealand zealand";
            default:
                return q; // no known alias, keep original
        }
    }


    private void filterCurrencies(String query) {
        if (allCurrencyItems == null) return;

        //String lowerQuery = query == null ? "" : query.toLowerCase(Locale.ROOT);

        String expanded = expandCountryAliases(query);
        String lowerQuery = expanded.toLowerCase(Locale.ROOT);

        filteredCurrencyItems.clear();

        if (lowerQuery.isEmpty()) {
            // no query -> show all
            filteredCurrencyItems.addAll(allCurrencyItems);
//        } else {
//            for (CurrencyItem item : allCurrencyItems) {
//                String code = item.getCurrencyCode() != null ? item.getCurrencyCode().toLowerCase(Locale.ROOT)
//                        : "";
//
//                String title = item.getTitle() != null
//                        ? item.getTitle().toLowerCase(Locale.ROOT)
//                        : "";
//
//                String description = item.getDescription() != null
//                        ? item.getDescription().toLowerCase(Locale.ROOT)
//                        : "";
//
//                // match by code, title or description (covers currency name & country)
//                if (code.contains(lowerQuery)
//                        || title.contains(lowerQuery)
//                        || description.contains(lowerQuery)) {
//                    filteredCurrencyItems.add(item);
//                }
//            }
//        }
        } else {
            String[] parts = lowerQuery.split("\\s+"); // split aliases into words for countries

            for (CurrencyItem item : allCurrencyItems) {

                String code = item.getCurrencyCode() != null
                        ? item.getCurrencyCode().toLowerCase(Locale.ROOT)
                        : "";

                String title = item.getTitle() != null
                        ? item.getTitle().toLowerCase(Locale.ROOT)
                        : "";

                String description = item.getDescription() != null
                        ? item.getDescription().toLowerCase(Locale.ROOT)
                        : "";

                boolean match = false;
                for (String p : parts) {
                    if (p.isEmpty()) continue;
                    if (code.contains(p) || title.contains(p) || description.contains(p)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    filteredCurrencyItems.add(item);
                }
            }
        }


        currencyAdapter.updateData(filteredCurrencyItems);
    }

    @Override
    public void onClick(View v) {
        startProgress();
    }

    public void startProgress() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        executorService.execute(new Task(urlSource));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    private void showErrorMessage(final String message) {
        if (mainHandler == null) return;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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

            Log.d("MyTask", "in run");
            BufferedReader in = null;

            try {
                Log.d("MyTask", "in try");
                URL aurl = new URL(url);
                URLConnection yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                String inputLine;
                result = "";
                while ((inputLine = in.readLine()) != null) {
                    result = result + inputLine;
                }

                // basic sanity check
                if (result == null || result.isEmpty()) {
                    showErrorMessage("No data received from server");
                    return;
                }

            } catch (IOException ae) {
                Log.e("MyTask", "ioexception: " + ae);
                showErrorMessage("Failed to download data. Please check your internet connection.");
                return;  // stop here so parsing doesn't run with empty result
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


                allCurrencyItems = parsedItems;
                filteredCurrencyItems = new ArrayList<>(allCurrencyItems);

                // Added 18-11
                List<String> mainCodes = new ArrayList<>();
                mainCodes.add("USD");
                mainCodes.add("EUR");
                mainCodes.add("JPY");

                List<CurrencyItem> mains = new ArrayList<>();

                for (String code : mainCodes) {
                    for (CurrencyItem item : allCurrencyItems) {
                        if (code.equalsIgnoreCase(item.getCurrencyCode())) {
                            mains.add(item);
                            break; // only first match per code
                        }
                    }
                }

                mainCurrencyItems = mains;

                // Update UI using Handler instead of runOnUiThread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        currencyAdapter.updateData(filteredCurrencyItems);
                        mainCurrencyAdapter.updateData(mainCurrencyItems);
                    }
                });

            } catch (XmlPullParserException e) {
                Log.e("MyTask", "XML parse error", e);
                showErrorMessage("Parse error in feed.");
            } catch (IOException e) {
                Log.e("MyTask", "Network error", e);
                showErrorMessage("Network error. Check connection.");
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
