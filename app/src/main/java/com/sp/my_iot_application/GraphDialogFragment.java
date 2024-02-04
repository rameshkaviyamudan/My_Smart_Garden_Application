package com.sp.my_iot_application;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphDialogFragment extends DialogFragment {

    private String dataType;
    private String historicalDataString;

    public static GraphDialogFragment newInstance(String dataType) {
        GraphDialogFragment fragment = new GraphDialogFragment();
        Bundle args = new Bundle();
        args.putString("dataType", dataType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataType = getArguments().getString("dataType");
            historicalDataString = getArguments().getString("historicalDataString");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph_dialog, container, false);

        // Display the dataType in a Toast
        Toast.makeText(requireActivity(), "Data Type: " + dataType, Toast.LENGTH_SHORT).show();

        // Initialize LineChart in the dialog and populate it with historical data
        LineChart lineChart = view.findViewById(R.id.dialogLineChart);
        setupLineChart(lineChart);

        // Fetch and display historical data
        fetchHistoricalData();

        return view;
    }
    private void fetchHistoricalData() {
        new Thread(() -> {
            try {
                // Fetch historical data based on dataType

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

                } finally {
                    urlConnection.disconnect();
                }
                // Process and display the graph
                requireActivity().runOnUiThread(() -> processAndDisplayGraph(historicalDataString));
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showFailureToast("Error fetching historical data: " + e.getMessage()));
            }
        }).start();
    }
    private void processAndDisplayGraph(String historicalDataString) {
        try {
            JSONObject historicalData = new JSONObject(historicalDataString);
            showGraphWithData(dataType, historicalData);
        } catch (JSONException e) {
            e.printStackTrace();
            showFailureToast("Error processing historical data: " + e.getMessage());
        }
    }
    private void setupLineChart(LineChart lineChart) {
        // Customize the appearance of the line chart in the dialog
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // Add other customizations as needed
        XAxis xAxis = lineChart.getXAxis();
        //xAxis.setValueFormatter(new TimestampAxisValueFormatter());
    }

    private void showGraphWithData(String dataType, JSONObject historicalData) {
        try {
            JSONArray data = historicalData.getJSONArray("data");

            // Extract values and timestamps
            List<Entry> entries = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                JSONObject entry = data.getJSONObject(i);

                if (!entry.has("message")) {
                    long timestamp = getTimestampFromString(entry.getString("timestamp"));
                    float value = (float) entry.getInt(dataType.toLowerCase()); // Assuming value key matches data type
                    entries.add(new Entry(timestamp, value));
                }
            }

            // Display the graph
            updateGraph(entries);
        } catch (JSONException e) {
            e.printStackTrace();
            showFailureToast("Error processing historical data: " + e.getMessage());
        }
    }

    private void updateGraph(List<Entry> entries) {
        requireActivity().runOnUiThread(() -> {
            if (getView() != null) {
                LineDataSet dataSet = new LineDataSet(entries, "Historical Data");
                LineData lineData = new LineData(dataSet);
                LineChart lineChart = getView().findViewById(R.id.dialogLineChart);
                lineChart.setData(lineData);

                // Notify the chart data has changed
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }
        });
    }


    private long getTimestampFromString(String timestampString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            Date date = sdf.parse(timestampString);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showFailureToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
