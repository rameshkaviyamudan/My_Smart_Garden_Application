package com.sp.my_iot_application;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DashboardActivity extends AppCompatActivity {

    private TextView temperatureText, humidityText, moistureText, potentiometerText, ldrText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize TextViews
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        moistureText = findViewById(R.id.moistureText);
        potentiometerText = findViewById(R.id.potentiometerText);
        ldrText = findViewById(R.id.ldrText);

        // Fetch sensor data
        fetchSensorData();
    }

    private void fetchSensorData() {
        new Thread(() -> {
            try {
                SharedPreferences preferences = getSharedPreferences("ServerDetails", MODE_PRIVATE);
                String serverIpAddress = preferences.getString("serverIpAddress", null);
                int serverPort = preferences.getInt("serverPort", 0);

                if (serverIpAddress == null || serverPort == 0) {
                    runOnUiThread(() -> showFailureToast("Server IP or Port not set"));
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
                runOnUiThread(() -> showFailureToast("Error fetching data: " + e.getMessage()));
            }
        }).start();
    }

    private void updateDashboard(JSONObject sensorData) {
        runOnUiThread(() -> {
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
        Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
