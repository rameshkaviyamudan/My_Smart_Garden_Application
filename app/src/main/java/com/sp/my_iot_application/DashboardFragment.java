
// DashboardFragment.java
package com.sp.my_iot_application;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import androidx.fragment.app.DialogFragment;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardFragment extends Fragment {

    private TextView temperatureText, humidityText, moistureText, potentiometerText, ldrText;
    private LineChart lineChart;
    private ScheduledExecutorService scheduler;

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
                // Handle humidity card click
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
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> fetchSensorData(), 0, 1, TimeUnit.SECONDS);

        return view;
    }
    public void onLdrCardClick(View view) {
        // Fetch historical data for LDR
    }

    private void fetchSensorData() {
        if (!isAdded()) {
            // Fragment is not added to the activity, return
            return;
        }
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
            }
        }).start();
    }

    private void updateDashboard(JSONObject sensorData) {
        requireActivity().runOnUiThread(() -> {
            try {
                // Retrieve sensor data
                double temperature = sensorData.getDouble("temperature");
                double humidity = sensorData.getDouble("humidity");
                String moisture = sensorData.getString("moisture");
                int potentiometer = sensorData.getInt("potentiometer");
                int ldr = sensorData.getInt("ldr");

                // Update TextViews with sensor data
                temperatureText.setText("Temperature: " + temperature);
                humidityText.setText("Humidity: " + humidity);
                moistureText.setText("Moisture: " + moisture);
                potentiometerText.setText("Potentiometer: " + potentiometer);
                ldrText.setText("LDR: " + ldr);

                // Store the sensor data in SharedPreferences
                SharedPreferences preferences = requireActivity().getSharedPreferences("SensorData", requireActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putFloat("temperature", (float) temperature);
                editor.putFloat("humidity", (float) humidity);
                editor.putString("moisture", moisture);
                editor.putInt("potentiometer", potentiometer);
                editor.putInt("ldr", ldr);
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }


    private void showFailureToast(String message) {
        requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show());
    }



    private void fetchHistoricalSensorData(String sensorType) {
        new Thread(() -> {
            try {
                // Fetch historical data based on the selected sensor type
                SharedPreferences preferences = requireActivity().getSharedPreferences("ServerDetails", requireActivity().MODE_PRIVATE);
                String serverIpAddress = preferences.getString("serverIpAddress", null);
                int serverPort = preferences.getInt("serverPort", 0);

                if (serverIpAddress == null || serverPort == 0) {
                    requireActivity().runOnUiThread(() -> showFailureToast("Server IP or Port not set"));
                    return;
                }

                // Customize the URL based on the selected sensor type
                String historicalDataUrl = "http://" + serverIpAddress + ":" + serverPort + "/historical_sensor_data?type=" + sensorType;
                URL url = new URL(historicalDataUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    // Extract timestamp and values from historicalData
                    JSONObject historicalData = new JSONObject(stringBuilder.toString());


                                // For other sensor types, continue showing the GraphDialogFragment
                                updateGraph(historicalData, sensorType);

                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showFailureToast("Error fetching historical data: " + e.getMessage()));
            }
        }).start();
    }



    private void updateGraph(JSONObject historicalData, String sensorType) {
        // Extract timestamp and values from historicalData and pass it to appropriate dialog fragment
        try {
            JSONArray data = historicalData.getJSONArray("data");
            List<Entry> entries = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                JSONObject entry = data.getJSONObject(i);
                String timestampString = entry.getString("timestamp");

                float value;
                if (sensorType.equals("moisture")) {
                    // Handle moisture data
                    boolean moistureValue = entry.getBoolean("moisture");
                    value = moistureValue ? 1f : 0f;
                } else {
                    // Handle other sensor data
                    value = (float) entry.getDouble(sensorType);
                }

                // Parse timestamp to Date object
                Date timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).parse(timestampString);

                // Add entry to the list
                entries.add(new Entry(timestamp.getTime(), value));
            }

            // Create a bundle to pass data to the appropriate dialog fragment
            Bundle bundle = new Bundle();
            bundle.putString("sensorType", sensorType);
            bundle.putSerializable("entries", (ArrayList<Entry>) entries);

            // Decide which dialog fragment to show based on sensor type
            requireActivity().runOnUiThread(() -> {
                DialogFragment dialogFragment;
                if (sensorType.equals("moisture")) {
                    // Create and show MoistureGraphDialogFragment for moisture data
                    MoistureGraphDialogFragment moistureGraphDialogFragment = new MoistureGraphDialogFragment();
                    moistureGraphDialogFragment.setArguments(bundle);
                    dialogFragment = moistureGraphDialogFragment;
                } else {
                    // For other sensor types, continue showing the GraphDialogFragment
                    GraphDialogFragment graphDialogFragment = new GraphDialogFragment();
                    graphDialogFragment.setArguments(bundle);
                    dialogFragment = graphDialogFragment;
                }

                // Ensure dialogFragment is not null before showing
                if (dialogFragment != null) {
                    dialogFragment.show(getParentFragmentManager(), "graph_dialog");
                } else {
                    Log.e("UpdateGraph", "DialogFragment is null");
                }
            });

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            showFailureToast("Error parsing historical data: " + e.getMessage());
        }
    }








// ... Your DashboardFragment class ...



// ... The rest of your DashboardFragment class ...

}
