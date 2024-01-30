// DashboardFragment.java
package com.sp.my_iot_application;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DashboardFragment extends Fragment {

    private TextView temperatureText, humidityText, moistureText, potentiometerText, ldrText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        // Initialize TextViews
        temperatureText = view.findViewById(R.id.temperatureText);
        humidityText = view.findViewById(R.id.humidityText);
        moistureText = view.findViewById(R.id.moistureText);
        potentiometerText = view.findViewById(R.id.potentiometerText);
        ldrText = view.findViewById(R.id.ldrText);

        // Fetch sensor data
        fetchSensorData();

        return view;
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
}
