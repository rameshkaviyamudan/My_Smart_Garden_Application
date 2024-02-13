package com.sp.my_iot_application;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static final String TAG = NotificationManager.class.getSimpleName();
    private Context context;

    public NotificationManager(Context context) {
        this.context = context;
    }

    public interface NotificationListener {
        void onNotificationsReceived(List<Notification> notifications);
    }

    public static class Notification {
        private String timestamp;
        private String message;

        public Notification(String timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }
    }

    public void fetchNotifications(NotificationListener listener) {
        SharedPreferences preferences = context.getSharedPreferences("ServerDetails", Context.MODE_PRIVATE);
        String serverIpAddress = preferences.getString("serverIpAddress", null);
        int serverPort = preferences.getInt("serverPort", 0);
        new FetchNotificationsTask(listener).execute(serverIpAddress, String.valueOf(serverPort));
    }

    private static class FetchNotificationsTask extends AsyncTask<String, Void, List<Notification>> {
        private NotificationListener listener;

        public FetchNotificationsTask(NotificationListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<Notification> doInBackground(String... params) {
            String serverIpAddress = params[0];
            int serverPort = Integer.parseInt(params[1]);
            List<Notification> notifications = new ArrayList<>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("http://" + serverIpAddress + ":" + serverPort + "/get_notifications");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                JSONArray jsonArray = new JSONArray(buffer.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String timestamp = jsonObject.getString("timestamp");
                    String message = jsonObject.getString("message");
                    notifications.add(new Notification(timestamp, message));
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error fetching notifications", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
            }

            return notifications;
        }

        @Override
        protected void onPostExecute(List<Notification> notifications) {
            super.onPostExecute(notifications);
            if (listener != null) {
                listener.onNotificationsReceived(notifications);
            }
        }
    }
}
