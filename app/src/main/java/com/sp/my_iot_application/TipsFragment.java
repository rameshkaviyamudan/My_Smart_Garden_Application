package com.sp.my_iot_application;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TipsFragment extends Fragment {
    private LinearLayout tipsContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        // Initialize the TextView
        tipsContainer = view.findViewById(R.id.tipsContainer);

        // Call the method to display hardcoded tips
        generateTips();

        return view;
    }

    private void fetchSensorData() {
        new Thread(() -> {
            try {
                SharedPreferences preferences = requireActivity().getSharedPreferences("ServerDetails", requireActivity().MODE_PRIVATE);
                String serverIpAddress = preferences.getString("serverIpAddress", null);
                int serverPort = preferences.getInt("serverPort", 0);

                if (serverIpAddress == null || serverPort == 0) {
                    //requireActivity().runOnUiThread(() -> showFailureToast("Server IP or Port not set"));
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
                    // Update UI with fetched sensor data
                    updateUI(new JSONObject(stringBuilder.toString()));
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUI(JSONObject sensorData) {
        try {
            // Fetch the latest sensor data
            float temperature = (float) sensorData.getDouble("temperature");
            float humidity = (float) sensorData.getDouble("humidity");
            float ldr = (float) sensorData.getDouble("ldr");
            int potentiometer = sensorData.getInt("EC Level");
            String moisture = sensorData.getString("moisture");

            // Call the method to generate and display tips
            //generateTips(temperature, humidity, ldr, potentiometer, moisture);
            generateTips();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void generateTips() {
        // Hardcoded sensor data values
        float temperature = 25.5f;
        float humidity = 45.7f;
        float ldr = 65.3f;
        int potentiometer = 75;
        String moisture = "Normal";

        addTipCard("Temperature Tip", generateTemperatureTip(temperature), Color.parseColor("#FFE0B2")); // Light Orange
        addTipCard("Humidity Tip", generateHumidityTip(humidity), Color.parseColor("#C8E6C9")); // Light Green
        addTipCard("Lighting Tip", generateLDRTip(ldr), Color.parseColor("#D1C4E9")); // Light Purple
        addTipCard("EC Tip", generatePotentiometerTip(potentiometer), Color.parseColor("#FFE0B2")); // Light Orange
        addTipCard("pH Tip", generateMoistureTip(moisture), Color.parseColor("#C8E6C9")); // Light Green
    }


    private void addTipCard(String title, String tip, int cardColor) {
        // Create a new CardView
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 40); // Add margin for spacing between cards
        cardView.setLayoutParams(layoutParams);
        cardView.setCardBackgroundColor(cardColor); // Set the background color of the card
        cardView.setCardElevation(4); // Customize the elevation as needed

        // Create a new TextView for the tip content
        TextView tipTextView = new TextView(requireContext());
        tipTextView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tipTextView.setPadding(16, 16, 16, 16); // Add padding for better appearance
        tipTextView.setText(title + "\n\n" + tip);

        // Add the TextView to the CardView
        cardView.addView(tipTextView);

        // Add the CardView to the tipsContainer
        tipsContainer.addView(cardView);
    }

    private String generateTemperatureTip(float temperature) {
        // Implement logic to generate temperature tips
        // For example, you can have different tips for various temperature ranges
        String tip;
        if (temperature < 20) {
            tip = "It's quite cold. Consider wearing warm clothes.";
        } else if (temperature >= 20 && temperature < 30) {
            tip = "The temperature is moderate. Enjoy the pleasant weather.";
        } else {
            tip = "It's hot outside. Stay hydrated.";
        }
        return "Temperature Tip: " + tip;
    }

    private String generateHumidityTip(float humidity) {
        // Implement logic to generate humidity tips
        // For example, you can have different tips for various humidity levels
        String tip;
        if (humidity < 30) {
            tip = "Low humidity. Keep yourself hydrated.";
        } else if (humidity >= 30 && humidity < 60) {
            tip = "Moderate humidity. Enjoy the comfortable atmosphere.";
        } else {
            tip = "High humidity. Stay cool and use fans or air conditioning.";
        }
        return "Humidity Tip: " + tip;
    }

    private String generateLDRTip(float ldr) {
        // Implement logic to generate LDR tips
        // For example, you can have different tips for various light sensitivity levels
        String tip;
        if (ldr < 50) {
            tip = "Low light sensitivity. Consider turning on lights.";
        } else if (ldr >= 50 && ldr < 80) {
            tip = "Moderate light sensitivity. Enjoy the natural light.";
        } else {
            tip = "High light sensitivity. Be mindful of glare and direct sunlight.";
        }
        return "LDR Tip: " + tip;
    }

    private String generatePotentiometerTip(int potentiometer) {
        // Implement logic to generate potentiometer tips for soil nutrients level (EC)
        String tip;
        if (potentiometer < 50) {
            tip = "Low soil nutrients level (EC). Consider adding fertilizers.";
        } else if (potentiometer >= 50 && potentiometer < 80) {
            tip = "Moderate soil nutrients level (EC). The soil is balanced.";
        } else {
            tip = "High soil nutrients level (EC). Avoid over-fertilization.";
        }
        return "Soil Nutrients Level (EC) Tip: " + tip;
    }

    private String generateMoistureTip(String moisture) {
        // Implement logic to generate moisture tips for soil pH
        String tip;
        if (moisture.equals("Acidic")) {
            tip = "Acidic soil pH. Consider adding lime to raise pH.";
        } else if (moisture.equals("Neutral")) {
            tip = "Neutral soil pH. The soil pH is balanced.";
        } else {
            tip = "Alkaline soil pH. Consider adding sulfur to lower pH.";
        }
        return "Soil pH Tip: " + tip;
    }

}
