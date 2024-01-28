package com.sp.my_iot_application;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Import statements

public class PullDownMenuFragment extends Fragment {

    private static final String CHANNEL_ID = "MyIOTChannel";
    private static final int NOTIFICATION_ID = 1;

    private TimePicker timePicker;
    private Spinner deviceSpinner, statusSpinner;
    private Button scheduleButton;
    private String selectedDevice = "fan";  // Default device
    private String selectedStatus = "on";  // Default status

    // ... Existing code

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pull_down_menu_fragment, container, false);

        timePicker = view.findViewById(R.id.timePicker);
        deviceSpinner = view.findViewById(R.id.deviceSpinner);
        statusSpinner = view.findViewById(R.id.statusSpinner);
        scheduleButton = view.findViewById(R.id.scheduleButton);

        // Populate device and status spinners with data
        populateDeviceSpinner();
        populateStatusSpinner();

        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleNotification();
            }
        });

        return view;
    }

    private void populateDeviceSpinner() {
        // Replace with your device data (e.g., fan, lamp, etc.)
        List<String> devices = Arrays.asList("Fan", "UV Light","Pump");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                devices
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        deviceSpinner.setAdapter(adapter);
    }

    private void populateStatusSpinner() {
        // Replace with your status data (e.g., on, off, etc.)
        List<String> statuses = Arrays.asList("On", "Off");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        statusSpinner.setAdapter(adapter);
    }

    private void scheduleNotification() {
        try {
            // Get the selected time from TimePicker
            int hour, minute;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }
            // Update the selected device and status based on the spinners
            selectedDevice = deviceSpinner.getSelectedItem().toString();
            selectedStatus = statusSpinner.getSelectedItem().toString();

            // Determine the actuator command based on the selected device and status
            String actuatorCommand = "";

            if ("Fan".equals(selectedDevice)) {
                actuatorCommand = "on".equals(selectedStatus.toLowerCase()) ? "servo_forward" : "servo_backward";
            } else if ("UV Light".equals(selectedDevice)) {
                actuatorCommand = "on".equals(selectedStatus.toLowerCase()) ? "led_on" : "led_off";
            }else if ("Pump".equals(selectedDevice)) {
                actuatorCommand = "on".equals(selectedStatus.toLowerCase()) ? "led_on" : "led_off";
            }

            // Make the API call with the determined actuator command
// Make the API call with the determined actuator command
            if (!actuatorCommand.isEmpty()) {
                // Store the selected time, device, status, and actuatorCommand values
                String scheduledTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                String scheduledDevice = selectedDevice;
                String scheduledStatus = selectedStatus;

                // Store the values using SharedPreferences
                SharedPreferences preferences = requireContext().getSharedPreferences("ScheduledData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("scheduledTime", scheduledTime);
                editor.putString("scheduledDevice", scheduledDevice);
                editor.putString("scheduledStatus", scheduledStatus);
                editor.putString("actuatorCommand", actuatorCommand); // Store actuatorCommand
                editor.apply();

                // Create an Intent to be broadcasted at the scheduled time
                Intent alarmIntent = new Intent(requireContext(), ScheduledToastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        requireContext(),
                        0,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Set up AlarmManager to trigger the broadcast at the specified time
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                getTriggerTimeInMillis(hour, minute),
                                pendingIntent
                        );
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, getTriggerTimeInMillis(hour, minute), pendingIntent);
                    }
                }
            }


        } catch (SecurityException e) {
            // Handle the SecurityException appropriately
            e.printStackTrace();
            Toast.makeText(requireContext(), "SecurityException: Unable to schedule exact alarm", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeApiCall(int fieldNumber, int fieldValue, int hour, int minute) {
        // Replace the following line with your actual API call logic
        // Use fieldNumber and fieldValue in the API call parameters
        // Example: ApiClient.sendDataToThingspeak(fieldNumber, fieldValue, hour, minute);

        // For testing purposes, display a Toast message
        String message = String.format("Scheduled API call: Device %d, Status %d, Time %02d:%02d", fieldNumber, fieldValue, hour, minute);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    private long getTriggerTimeInMillis(int hour, int minute) {
        // Calculate the trigger time in milliseconds based on the selected hour and minute
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}



    // ... Existing code
