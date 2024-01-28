package com.sp.my_iot_application;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ScheduledToastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MyIOTChannel";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve the stored actuator command
        SharedPreferences preferences = context.getSharedPreferences("ScheduledData", Context.MODE_PRIVATE);
        String actuatorCommand = preferences.getString("actuatorCommand", "");

        // Display a toast message based on the actuator command
        String message = String.format("Scheduled API call: Actuator Command %s", actuatorCommand);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        // Check if the actuator command is not empty and create HttpPostRequestTask
        if (!actuatorCommand.isEmpty()) {

            HttpPostRequestTask httpPostRequestTask = new HttpPostRequestTask(actuatorCommand, context);
            httpPostRequestTask.execute();
        }
    }



}
