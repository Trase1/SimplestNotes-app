package com.traseapps.simplestNotes;


import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class GoatTracker {
    private static final String BASE_URL = "https://simplestnotes.goatcounter.com/count";

    public static void trackEvent(String path) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "?p=" + path);
                Log.d("GoatTracker", "Sending: " + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.getInputStream().close();  // fire-and-forget
            } catch (Exception e) {
                Log.e("GoatTracker", "Failed to track " + path, e);
            }
        }).start();
    }
}
