package com.sp.my_iot_application;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class BarChartDialogFragment extends DialogFragment {

    private static final String ARG_SENSOR_TYPE = "sensorType";
    private static final String ARG_ENTRIES = "entries";

    public static BarChartDialogFragment newInstance(String sensorType, ArrayList<BarEntry> entries) {
        BarChartDialogFragment fragment = new BarChartDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SENSOR_TYPE, sensorType);
        args.putSerializable(ARG_ENTRIES, entries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_barchart, null);
        builder.setView(view);

        // Get the entries and sensor type from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String sensorType = bundle.getString(ARG_SENSOR_TYPE);
            List<BarEntry> entries = (ArrayList<BarEntry>) bundle.getSerializable(ARG_ENTRIES);

            // Set the title dynamically
            builder.setTitle(sensorType + " Chart");

            // Initialize and customize the appropriate chart based on the sensor type
            BarChart chart = view.findViewById(R.id.barChart);
            initializeChart(chart, sensorType);

            // Update the chart with entries
            updateChart(chart, entries);
        } else {
            // Handle error if entries are not available
            Toast.makeText(requireContext(), "Error: No data available", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return builder.create();
    }

    private void initializeChart(BarChart chart, String sensorType) {
        // Initialize and customize the chart based on the sensor type
        // For example, you can configure legends, axes, etc.
        if (sensorType.equals("moisture")) {
            // Customize BarChart for moisture data
            // (e.g., set specific colors, axis labels, etc.)
        } else {
            // Customize BarChart for other sensor data
            // (e.g., set different colors, axis labels, etc.)
        }
    }

    private void updateChart(BarChart chart, List<BarEntry> entries) {
        // Update the chart with the provided entries
        BarDataSet dataSet = new BarDataSet(entries, "Data");
        BarData data = new BarData(dataSet);
        chart.setData(data);

        // Invalidate the chart to refresh
        chart.invalidate();
    }
}
