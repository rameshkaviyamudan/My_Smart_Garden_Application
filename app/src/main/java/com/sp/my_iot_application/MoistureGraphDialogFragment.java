package com.sp.my_iot_application;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoistureGraphDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bar_dialog, container, false);

        // Get the entries from the bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Get the sensor type from the bundle
            String sensorType = bundle.getString("sensorType", "");

            // Set the title dynamically and capitalize it
            String capitalizedTitle = sensorType.substring(0, 1).toUpperCase() + sensorType.substring(1);
            TextView titleTextView = view.findViewById(R.id.messageTextView2);
            titleTextView.setText(capitalizedTitle + " Graph");

            List<Entry> entries = (ArrayList<Entry>) bundle.getSerializable("entries");

            // Convert Entry objects to BarEntry objects
            List<BarEntry> barEntries = convertToBarEntries(entries);

            // Set up BarChart for moisture data
            BarChart barChart = view.findViewById(R.id.dialogBarChart);
            configureBarChart(barChart);
            updateBarChart(barChart, barEntries);
        } else {
            // Handle error if entries are not available
            Toast.makeText(requireContext(), "Error: No data available", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return view;
    }

    // Convert Entry objects to BarEntry objects
    private List<BarEntry> convertToBarEntries(List<Entry> entries) {
        List<BarEntry> barEntries = new ArrayList<>();
        for (Entry entry : entries) {
            float value = entry.getY(); // Use the Y-value of the Entry
            barEntries.add(new BarEntry(entry.getX(), value));
        }
        return barEntries;
    }

    private void configureBarChart(BarChart barChart) {
        // Add any additional configurations specific to BarChart
        // Configure legend
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Set legend labels
        legend.setExtra(Arrays.asList(
                new LegendEntry("True", Legend.LegendForm.SQUARE, 8f, 8f, null, ContextCompat.getColor(requireContext(), R.color.black)),
                new LegendEntry("False", Legend.LegendForm.SQUARE, 8f, 8f, null, ContextCompat.getColor(requireContext(), R.color.purple_200))
        ));
    }

    private void updateBarChart(BarChart barChart, List<BarEntry> entries) {
        BarDataSet barDataSet = new BarDataSet(entries, "Moisture Data");

        // Set color for true and false values
        int trueColor = ContextCompat.getColor(requireContext(), R.color.black); // Change R.color.colorTrue to your true color resource
        int falseColor = ContextCompat.getColor(requireContext(), R.color.purple_200); // Change R.color.colorFalse to your false color resource

        barDataSet.setColors(new int[]{trueColor, falseColor});

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Configure BarChart
        configureBarChart(barChart);

        // Invalidate the chart to refresh
        barChart.invalidate();
    }

}
