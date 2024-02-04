
// DashboardFragment.java
package com.sp.my_iot_application;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView temperatureText, humidityText, moistureText, potentiometerText, ldrText;
    private LineChart lineChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize TextViews
        temperatureText = view.findViewById(R.id.temperatureText);
        humidityText = view.findViewById(R.id.humidityText);
        moistureText = view.findViewById(R.id.moistureText);
        potentiometerText = view.findViewById(R.id.potentiometerText);
        ldrText = view.findViewById(R.id.ldrText);
        // Initialize LineChart
        // Set click listeners for cards
        view.findViewById(R.id.temperatureCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle temperature card click
                fetchHistoricalSensorData("temperature");
            }
        });
        view.findViewById(R.id.humidityCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle temperature card click
                fetchHistoricalSensorData("humidity");
            }
        });
        view.findViewById(R.id.MoistureCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle temperature card click
                fetchHistoricalSensorData("moisture");
            }
        });
        view.findViewById(R.id.PotentiometerCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle temperature card click
                fetchHistoricalSensorData("potentiometer");
            }
        });
        view.findViewById(R.id.LDRCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle temperature card click
                fetchHistoricalSensorData("ldr");
            }
        });
        // Fetch sensor data
        fetchSensorData();

        return view;
    }
    public void onLdrCardClick(View view) {
        // Fetch historical data for LDR
        fetchHistoricalSensorData("ldr");
    }

    private void fetchSensorData() {
        new Thread(() -> {
            try {
                SharedPreferences preferences = requireActivity().getSharedPreferences("ServerDetails", requireActivity().MODE_PRIVATE);
                String serverIpAddress = preferences.getString("serverIpAddress", null);
                int serverPort = preferences.getInt("serverPort", 0);

                if (serverIpAddress == null || serverPort == 0) {
                    requireActivity().runOnUiThread(() -> showFailureToast("Server IP or Port not set"));
                    return;
                }

                URL url = new URL("http://" + serverIpAddress + ":" + serverPort + "/sensor_data");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    updateDashboard(new JSONObject(stringBuilder.toString()));
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showFailureToast("Error fetching data: " + e.getMessage()));
            }
        }).start();
    }

    private void updateDashboard(JSONObject sensorData) {
        requireActivity().runOnUiThread(() -> {
            try {
                temperatureText.setText("Temperature: " + sensorData.getDouble("temperature"));
                humidityText.setText("Humidity: " + sensorData.getDouble("humidity"));
                moistureText.setText("Moisture: " + sensorData.getString("moisture"));
                potentiometerText.setText("Potentiometer: " + sensorData.getInt("potentiometer"));
                ldrText.setText("LDR: " + sensorData.getInt("ldr"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void showFailureToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
    private void showGraph(String dataType) {
        // Create a dialog or another fragment to show the graph
        GraphDialogFragment graphDialogFragment = GraphDialogFragment.newInstance(dataType);
        graphDialogFragment.show(getChildFragmentManager(), "graph_dialog");
    }

    private void fetchHistoricalSensorData(String dataType) {
        new Thread(() -> {
            try {
                SharedPreferences preferences = requireActivity().getSharedPreferences("ServerDetails", requireActivity().MODE_PRIVATE);
                String serverIpAddress = preferences.getString("serverIpAddress", null);
                int serverPort = preferences.getInt("serverPort", 0);

                if (serverIpAddress == null || serverPort == 0) {
                    requireActivity().runOnUiThread(() -> showFailureToast("Server IP or Port not set"));
                    return;
                }

                // Make sure to use GET method
                URL url = new URL("http://" + serverIpAddress + ":" + serverPort + "/historical_sensor_data?dataType=" + URLEncoder.encode(dataType, "UTF-8"));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    // Set up the request properties
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    // Read the response
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    // Process and update the dashboard with historical data
                    showGraph(dataType); // Pass the historical data as a string
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showFailureToast("Error fetching historical data: " + e.getMessage()));
            }
        }).start();
    }



// ... Your DashboardFragment class ...



// ... The rest of your DashboardFragment class ...

}
