package com.example.remindme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final String CHANNEL_ID = "TimerChannel";

    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;

    private FragmentManager fragmentManager;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissionNotification();
        createNotificationChannel();

        Intent stopRingtoneIntent = new Intent(this, AlarmReceiver.StopRingtoneReceiver.class);
        stopRingtoneIntent.setAction("STOP_RINGTONE");
        sendBroadcast(stopRingtoneIntent);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        fragment1 = new FragmentTaskLists();
        fragment2 = new FragmentTimer();
        fragment3 = new FragmentFinishedTasks();

        fragmentManager = getSupportFragmentManager();

        // Set the initial fragment
        setFragment(fragment1);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.menu_item1) {
                selectedFragment = fragment1;
            } else if (item.getItemId() == R.id.menu_item2) {
                selectedFragment = fragment2;
            } else if (item.getItemId() == R.id.menu_item3) {
                selectedFragment = fragment3;
            }

            setFragment(selectedFragment);
            return true;
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction() != null && intent.getAction().equals("NOTIFICATION_CLICKED")) {
            // Handle the notification click action here, e.g., stop the ringtone
            Intent stopRingtoneIntent = new Intent(this, AlarmReceiver.StopRingtoneReceiver.class);
            stopRingtoneIntent.setAction("STOP_RINGTONE");
            sendBroadcast(stopRingtoneIntent);
        }
    }


    private void requestPermissionNotification(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);

        } else {

        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Channel for timer notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}