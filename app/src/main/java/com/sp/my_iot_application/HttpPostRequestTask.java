package com.sp.my_iot_application;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPostRequestTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = HttpPostRequestTask.class.getSimpleName();
    private String actuatorCommand;

    private Context context;

    public HttpPostRequestTask(String actuatorCommand, Context context) {
        this.context = context;

        this.actuatorCommand = actuatorCommand;
    }
    @Override
    protected Void doInBackground(String... params) {
        try {
            // Retrieve server details from SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences("ServerDetails", Context.MODE_PRIVATE);
            String serverIpAddress = preferences.getString("serverIpAddress", null);
            int serverPort = preferences.getInt("serverPort", 0);
            // Show a Toast message with retrieved address and port
            showToast("Server Address: " + serverIpAddress + "\nServer Port: " + serverPort);

            // Construct the URL for your API endpoint with user-entered server details
            String apiUrl = "http://" + serverIpAddress + ":" + serverPort + "/control_actuator";

            // Create a URL object
            URL url = new URL(apiUrl);

            // Create an HttpURLConnection object
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                // Set request method to POST
                urlConnection.setRequestMethod("POST");

                // Set request headers if needed
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Enable input/output streams
                urlConnection.setDoOutput(true);

                // Construct the request payload
                String payload = String.format("{\"command\": \"%s\"}", actuatorCommand);

                // Write the payload to the output stream
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(payload);
                writer.flush();
                writer.close();
                outputStream.close();

                // Get the response from the server (if needed)
                int responseCode = urlConnection.getResponseCode();
                // Read and process the response if needed

            } finally {
                // Disconnect the HttpURLConnection
                urlConnection.disconnect();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error making HTTP POST request", e);
        }

        return null;
    }
    // Helper method to show a Toast message
    private void showToast(final String message) {
        // Ensure the Toast is displayed on the UI thread
        if (context instanceof MainActivity) {
            ((MainActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
