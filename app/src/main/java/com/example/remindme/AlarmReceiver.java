package com.example.remindme;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlarmReceiver extends Worker {

    private static Ringtone ringtone; // Declare Ringtone as a class variable
    private static final int NOTIFICATION_ID = 1;

    private static Vibrator vibrator;

    // Declare the BroadcastReceiver as a class member
    private final BroadcastReceiver stopRingtoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the ringtone when the button is clicked
            stopRingtone();

            // Cancel the notification
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(NOTIFICATION_ID);
        }
    };

    public AlarmReceiver(Context context, WorkerParameters params) {
        super(context, params);
    }

    @SuppressLint("MissingPermission")
    @Override
    public Result doWork() {
        String title = getInputData().getString("title");
        String content = getInputData().getString("content");
        Context context = getApplicationContext();

        String channelId = "TimerChannel";

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationPattern = {0, 1000};

        vibrator.vibrate(vibrationPattern, 0);


        // Get the default alarm sound URI
        Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // Create a Ringtone object using the obtained URI
        ringtone = RingtoneManager.getRingtone(context, alarmSoundUri);


        IntentFilter filter = new IntentFilter();
        filter.addAction("STOP_RINGTONE");
        context.registerReceiver(stopRingtoneReceiver, filter);

        // Create an Intent for the BroadcastReceiver
        Intent stopIntent = new Intent(context, StopRingtoneReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction("NOTIFICATION_CLICKED");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent clickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent fullpendingIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Create a custom layout for the notification with a stop button
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setFullScreenIntent(fullpendingIntent, true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
                .setDeleteIntent(stopPendingIntent)
                .setTimeoutAfter(0)
                .setContentIntent(clickPendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.black)); // Customize the color

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, notificationBuilder.build());

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM) // Set the intended usage, e.g., USAGE_ALARM
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Set the content type
                .build();

        if (ringtone != null) {
            ringtone.setAudioAttributes(audioAttributes);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.setVolume(1.0f);
                ringtone.setLooping(true);
            }
            // Play the ringtone
            ringtone.play();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2 * 60 * 1000); // Sleep for 1 minute
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Stop the ringtone after 1 minute
                    stopRingtone();
                }
            }).start();

        }
        return Result.success();
    }

    // Add a method to stop the ringtone
    public static void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    // BroadcastReceiver to handle the button click
    public static class StopRingtoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopRingtone();
            Intent stopRingtoneIntent = new Intent(context, AlarmReceiver.class);
            stopRingtoneIntent.setAction("STOP_RINGTONE");
            context.sendBroadcast(stopRingtoneIntent);

            // Cancel the notification
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(NOTIFICATION_ID);
        }
    }
}
