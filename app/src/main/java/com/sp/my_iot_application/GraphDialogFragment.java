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
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph_dialog, container, false);

        // Get the entries from the bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Get the sensor type from the bundle
            String sensorType = bundle.getString("sensorType", "");

            // Set the title dynamically
            TextView titleTextView = view.findViewById(R.id.messageTextView);
            titleTextView.setText(sensorType + " Graph");

            List<Entry> entries = (ArrayList<Entry>) bundle.getSerializable("entries");

            // Determine the appropriate chart type based on the sensor type
            Chart chart = createChart(view, sensorType);

            // Update the chart with entries
            updateChart(chart, entries, sensorType);
        } else {
            // Handle error if entries are not available
            Toast.makeText(requireContext(), "Error: No data available", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return view;
    }

    private void configureLineChart(LineChart lineChart) {
        // Configure LineChart appearance and behavior
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // Configure X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(30f); // Set the granularity to adjust the spacing between labels

    }

    private void updateLineChart(LineChart lineChart, List<Entry> entries, String sensorDataType) {
        if (sensorDataType.equals("moisture")) {
            // If moisture data, convert LineChart to BarChart
            convertToBarChart(lineChart, entries);
        } else {
            // For other sensor data, use a LineDataSet
            LineDataSet lineDataSet = new LineDataSet(entries, sensorDataType + " Data");
            LineData lineData = new LineData(lineDataSet);
            lineChart.setData(lineData);

            // Configure LineChart
            configureLineChart(lineChart);

            // Automatically move the viewport to the current time
            moveViewToCurrentTime(lineChart, entries);

            // Invalidate the chart to refresh
            lineChart.invalidate();
        }
    }
    private void configureBarChart(BarChart barChart) {
        // Add any additional configurations specific to BarChart
        // For example, you might want to adjust the bar width, add legends, etc.
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
    private void moveViewToCurrentTime(LineChart lineChart, List<Entry> entries) {
        long currentTimeMillis = System.currentTimeMillis();

        // Set an offset to provide some margin on the right side
        float offset = 100f; // You can adjust this value based on your preference

        // Set the minimum visible range to 1 (to ensure the rightmost entry is fully visible)
        lineChart.setVisibleXRangeMinimum(1);

        // Move the view to the current time
        lineChart.moveViewToX(currentTimeMillis + offset);
    }

    private static class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

        @Override
        public String getFormattedValue(float value) {
            // Convert timestamp to formatted date
            Date date = new Date((long) value);
            return dateFormat.format(date);
        }
    }

    private List<BarEntry> convertToBarEntries(List<Entry> entries) {
        List<BarEntry> barEntries = new ArrayList<>();
        for (Entry entry : entries) {
            barEntries.add(new BarEntry(entry.getX(), entry.getY()));
        }
        return barEntries;
    }
    private void convertToBarChart(LineChart lineChart, List<Entry> entries) {
        // Convert LineChart to BarChart
        BarChart barChart = new BarChart(requireContext());
        List<BarEntry> barEntries = convertToBarEntries(entries);

        BarDataSet barDataSet = new BarDataSet(barEntries, "Moisture Data");

        // Set color for true and false values
        int trueColor = ContextCompat.getColor(requireContext(), R.color.black); // Change R.color.colorTrue to your true color resource
        int falseColor = ContextCompat.getColor(requireContext(), R.color.purple_200); // Change R.color.colorFalse to your false color resource

        barDataSet.setColors(new int[]{trueColor, falseColor});

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Configure BarChart
        configureBarChart(barChart);

        // Replace the LineChart with BarChart in the layout
        ViewGroup parent = (ViewGroup) lineChart.getParent();
        int index = parent.indexOfChild(lineChart);
        parent.removeViewAt(index);
        parent.addView(barChart, index);
    }
    private Chart createChart(View view, String sensorDataType) {
        Chart chart;
        if (sensorDataType.equals("moisture")) {
            // For moisture data, use a BarChart
            chart = view.findViewById(R.id.dialogBarChart);
            configureBarChart((BarChart) chart);
        } else {
            // For other sensor data, use a LineChart
            chart = view.findViewById(R.id.dialogLineChart);
            configureLineChart((LineChart) chart);
        }
        return chart;
    }
    private void updateChart(Chart chart, List<Entry> entries, String sensorDataType) {
        if (sensorDataType.equals("moisture") && chart instanceof BarChart) {
            // For moisture data and BarChart, update the chart
            updateBarChart((BarChart) chart, entries);
        } else if (chart instanceof LineChart) {
            // For other sensor data and LineChart, update the chart
            updateLineChart((LineChart) chart, entries, sensorDataType);
        }
    }
    private void updateBarChart(BarChart barChart, List<Entry> entries) {
        List<BarEntry> barEntries = convertToBarEntries(entries);

        BarDataSet barDataSet = new BarDataSet(barEntries, "Moisture Data");

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
