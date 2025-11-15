package org.me.gcu.stewart_john_s2433454;

public class CurrencyItem {

    // Raw text fields from the RSS
    private String title;       // e.g. "British Pound Sterling(GBP)/United Arab Emirates Dirham(AED)"
    private String description; // e.g. "1 British Pound Sterling = 4.9471 United Arab Emirates Dirham"
    private String pubDate;     // e.g. "Wed Aug 27 2025 2:00:45 UTC"
    private String link;        // e.g. "https://www.fx-exchange.com/gbp/aed.html"

    // Processed fields we will extract with String methods
    private String currencyCode; // e.g. "AED"
    private double rate;         // e.g. 4.9471

    public CurrencyItem() {
        // empty default constructor
    }

    // --- Getters and setters ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        // every time we set title, we can try to derive the currency code
        extractCurrencyCodeFromTitle();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        // every time we set description, we can try to derive the rate
        extractRateFromDescription();
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getRate() {
        return rate;
    }

    // --- Helper methods to process strings ---

    // Example title:
    // "British Pound Sterling(GBP)/United Arab Emirates Dirham(AED)"
    // We want "AED"
    private void extractCurrencyCodeFromTitle() {
        if (title == null) return;

        String trimmed = title.trim();
        int open = trimmed.lastIndexOf('(');
        int close = trimmed.lastIndexOf(')');

        if (open != -1 && close != -1 && close > open) {
            currencyCode = trimmed.substring(open + 1, close).trim();
        }
    }

    // Example description:
    // "1 British Pound Sterling = 4.9471 United Arab Emirates Dirham"
    // We want 4.9471 as a double
    private void extractRateFromDescription() {
        if (description == null) return;

        String trimmed = description.trim();
        int equalsIndex = trimmed.indexOf('=');

        if (equalsIndex != -1 && equalsIndex + 1 < trimmed.length()) {
            String rightSide = trimmed.substring(equalsIndex + 1).trim(); // "4.9471 United Arab Emirates Dirham"
            String[] parts = rightSide.split("\\s+");

            if (parts.length > 0) {
                try {
                    rate = Double.parseDouble(parts[0]);
                } catch (NumberFormatException e) {
                    rate = 0.0;
                }
            }
        }
    }

    @Override
    public String toString() {
        return currencyCode + " : " + rate;
    }
}
