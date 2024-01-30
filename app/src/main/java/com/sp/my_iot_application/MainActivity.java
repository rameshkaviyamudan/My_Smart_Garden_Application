package com.sp.my_iot_application;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;
// Use the appropriate import statements
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.droidsonroids.gif.GifImageView;
public class MainActivity extends AppCompatActivity {
    private String serverIpAddress;
    private int serverPort;
    private LinearLayout pullDownMenu;
    private ImageView arrowIcon;

    private TextView field4TextView;


    private BottomNavigationView bottomNavigationView;
    public static final String CHANNEL_ID = "MyIOTChannel";
    private static final int NOTIFICATION_REQUEST_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        // Initialize views
        // Call a method to show the IP and port input dialog
        showIpAddressPortDialog();

        arrowIcon = findViewById(R.id.arrowIcon);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Assuming you have a GifImageView for fan and lamp icons
        GifImageView fanIcon = findViewById(R.id.fanIcon);
        GifImageView lampIcon = findViewById(R.id.lampIcon);



// Check if notification permission is granted
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            showNotificationPermissionDialog();
        }
        // Assuming you have a Button for fan and lamp buttons
        Button fanButton = findViewById(R.id.fanButton);
        Button lampButton = findViewById(R.id.lampButton);

        fanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the state
                boolean isChecked = !fanButton.isSelected();
                fanButton.setSelected(isChecked);

                // Change the background resource based on the toggle state
                int imageResource = isChecked ? R.drawable.ic_button_on : R.drawable.ic_button_off;
                fanButton.setBackgroundResource(imageResource);

                if (isChecked) {
                    int iconResource = R.drawable.ic_fan_on_anim;
                    Glide.with(MainActivity.this)
                            .asGif()
                            .load(iconResource)
                            .into(fanIcon);

                } if(!isChecked) {
                    int iconResource = R.drawable.ic_fan_off;
                    Glide.with(MainActivity.this)
                            .asGif()
                            .load(iconResource)
                            .into(fanIcon);
                }

                // Make the HTTP API call immediately when the state changes
                String actuatorCommand = isChecked ? "buzzer_on" : "buzzer_off";
                makeApiCall(actuatorCommand);
                Log.d("ButtonState", "isChecked: " + isChecked);
                Log.d("ApiCall", "Actuator Command: " + actuatorCommand);


            }
        });

        lampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the state
                boolean isChecked = !lampButton.isSelected();
                lampButton.setSelected(isChecked);

                // Change the background resource based on the toggle state
                int imageResource = isChecked ? R.drawable.ic_button_on : R.drawable.ic_button_off;
                lampButton.setBackgroundResource(imageResource);

                if (isChecked) {
                    int iconResource = R.drawable.ic_lamp_on_anim;
                    Glide.with(MainActivity.this)
                            .asGif()
                            .load(iconResource)
                            .into(lampIcon);
                } else {
                    int iconResource = R.drawable.ic_lamp_off;
                    Glide.with(MainActivity.this)
                            .asGif()
                            .load(iconResource)
                            .into(lampIcon);
                }

                // Make the HTTP API call immediately when the state changes
                String actuatorCommand = isChecked ? "led_on" : "led_off";
                makeApiCall(actuatorCommand);
            }
        });


        // Set onClickListener for the arrow icon
        arrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePullDownMenu();
            }
        });

        // Set up the bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        // Handle bottom navigation item clicks here
                        if (item.getItemId() == R.id.action_home) {
                            // Handle Home
                            return true;
                        } else if (item.getItemId() == R.id.action_tips) {
                            // Handle Tips
                            return true;
                        } else if (item.getItemId() == R.id.action_dashboard) {
                            // Handle Monitoring
                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                        return false;
                    }
                });
        // Retrieve server details from SharedPreferences
        retrieveServerDetails();



    }
    // Function to open PullDownMenuFragmentTest
    private void openPullDownMenuFragmentTest() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the existing fragment with PullDownMenuFragmentTest
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Method to toggle the visibility of the pull-down menu with animation
    private void togglePullDownMenu() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PullDownMenuFragment pullDownMenuFragment = (PullDownMenuFragment) fragmentManager.findFragmentByTag("pullDownMenu");
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        ImageView arrowIcon = findViewById(R.id.arrowIcon);

        if (pullDownMenuFragment == null) {
            // If menu is not visible, show it
            pullDownMenuFragment = new PullDownMenuFragment();
            fragmentTransaction.add(R.id.fragment_container, pullDownMenuFragment, "pullDownMenu");
            // Grey out the background
            findViewById(R.id.main_layout).setAlpha(0.5f);
            // Adjust the translation to include arrow's height
            fragmentContainer.animate().translationY(arrowIcon.getHeight());
            arrowIcon.setRotation(180); // Rotate arrow icon
        } else {
            // If menu is visible, hide it
            fragmentTransaction.remove(pullDownMenuFragment);

            // Reset the translation and rotation
            fragmentContainer.animate().translationY(0);
            arrowIcon.setRotation(0);
            // Remove the greyed-out background
            findViewById(R.id.main_layout).setAlpha(1.0f);
        }

        fragmentTransaction.commit();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My IOT Channel";
            String description = "Channel for My IOT Application";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showNotificationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Permission");
        builder.setMessage("Please enable notifications to use this app.");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestNotificationPermission();
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // or handle the exit action as needed
            }
        });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestNotificationPermission() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivityForResult(intent, NOTIFICATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            // Check notification permission after the user has taken action
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // User still hasn't enabled notifications, handle accordingly
                finish(); // or display another message or take appropriate action
            }
        }
    }
    // Inside your method for making API calls
    private void makeApiCall(String actuatorCommand) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    // Check if serverIpAddress and serverPort are not null
                    if (serverIpAddress == null || serverPort == 0) {
                        // Prompt the user to enter server details again or handle accordingly
                        return;
                    }

                    // Construct the URL for your API endpoint using the entered server details
                    String apiUrl = String.format("http://%s:%d/control_actuator", serverIpAddress, serverPort);

                    // Create a URL object
                    URL url = new URL(apiUrl);

                    // Create an HttpURLConnection object
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        //Toast.makeText(MainActivity.this, "dsvvv ", Toast.LENGTH_SHORT).show();

                        // Set request method to POST
                        urlConnection.setRequestMethod("POST");

                        // Set request headers if needed
                        urlConnection.setRequestProperty("Content-Type", "application/json");

                        // Enable input/output streams
                        urlConnection.setDoOutput(true);

                        // Construct the request payload
                        JSONObject payload = new JSONObject();
                        payload.put("command", actuatorCommand);

                        // Write the payload to the output stream
                        OutputStream outputStream = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        writer.write(payload.toString());
                        writer.flush();
                        writer.close();
                        outputStream.close();

                        // Get the response from the server (if needed)
                        int responseCode = urlConnection.getResponseCode();

                        // Process the response if needed

                        // Show Toast messages based on the response code
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            showSuccessToast("Request successful");
                        } else {
                            showFailureToast("Request unsuccessful");
                        }

                    } finally {
                        // Disconnect the HttpURLConnection
                        urlConnection.disconnect();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Show Toast for exception
                    showFailureToast("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    // Helper method to show a success Toast message
    private void showSuccessToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to show a failure Toast message
    private void showFailureToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIpAddressPortDialog() {
        // Create an AlertDialog.Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Server Details");

        // Set up the layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ip_port, null);
        builder.setView(view);

        // Get references to the EditTexts in the dialog
        EditText ipAddressEditText = view.findViewById(R.id.editTextIpAddress);
        EditText portEditText = view.findViewById(R.id.editTextPort);

        // Set input types for IP address and port
        ipAddressEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        portEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Set up the positive button (OK button)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the entered values from the EditTexts
                serverIpAddress = ipAddressEditText.getText().toString().trim();
                serverPort = Integer.parseInt(portEditText.getText().toString().trim());

                // Save the server details (IP address and port) for later use
                // You might want to store these values in SharedPreferences for persistence
                // Save the server details in SharedPreferences
                saveServerDetails(serverIpAddress, serverPort);

                // Now, you can use serverIpAddress and serverPort in your API calls
            }
        });

        // Set up the negative button (Cancel button)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the cancel action, e.g., close the app or show an error message
                finish();
            }
        });

        // Show the dialog
        builder.show();
    }
    // Method to retrieve server details from SharedPreferences
    private void retrieveServerDetails() {
        SharedPreferences preferences = getSharedPreferences("ServerDetails", Context.MODE_PRIVATE);
        serverIpAddress = preferences.getString("serverIpAddress", null);
        serverPort = preferences.getInt("serverPort", 0);

        // You can now use serverIpAddress and serverPort in your API calls
    }

    // Method to save server details in SharedPreferences
    private void saveServerDetails(String ipAddress, int port) {
        SharedPreferences preferences = getSharedPreferences("ServerDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("serverIpAddress", ipAddress);
        editor.putInt("serverPort", port);
        editor.apply();
    }

}

