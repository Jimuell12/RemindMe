package com.example.remindme;

import android.app.TimePickerDialog;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentTimer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTimer extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentTimer() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTimer newInstance(String param1, String param2) {
        FragmentTimer fragment = new FragmentTimer();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    Button button_play, button_pause, button_stop, button_finish;
    TextView tv_timer, tv_task, tv_update;
    ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private long TIMER_DURATION = 500 * 3 * 1000; // 25 minutes
    private final long REST_DURATION = 10000; // 5 minutes
    private long timeRemaining; // Track the remaining time when pausing
    private boolean isTimerRunning = false;
    private TimerViewModel timerViewModel;
    private DatabaseFinishedTaskHelper databaseFinishedTaskHelper;
    private DatabaseHelper databaseHelper;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        button_play = view.findViewById(R.id.button_play);
        button_pause = view.findViewById(R.id.button_pause);
        button_stop = view.findViewById(R.id.button_stop);
        button_finish = view.findViewById(R.id.button_finish);


        // Inside your onCreateView or wherever you initialize your views
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.hum);

        tv_timer = view.findViewById(R.id.tv_timer);
        tv_task = view.findViewById(R.id.tv_task);
        tv_update = view.findViewById(R.id.tv_update);



        updateTimerText();
        progressBar = view.findViewById(R.id.progressBar);

        if(timeRemaining > 0){
            tv_timer.setEnabled(false);
        }else {
            progressBar.setProgress((int) TIMER_DURATION);
        }

        databaseFinishedTaskHelper = new DatabaseFinishedTaskHelper(getActivity());
        databaseHelper = new DatabaseHelper(getActivity());

        button_stop.setEnabled(false);

        // Retrieve the text from the bundle
        getBundle();

        if (tv_task.getText().toString().isEmpty()) {
            button_play.setEnabled(false);
        } else {
            button_play.setEnabled(true);
        }

        tv_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a TimePickerDialog
                showTimePickerDialog();
            }
        });

        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeRemaining > 0) {
                    resumeTimer(timeRemaining);
                } else {
                    startTimer();
                }
            }
        });

        button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    pauseTimer();
                }
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        button_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTask();
            }
        });

        return view;
    }

    private void showTimePickerDialog() {
        // Get the current hours and minutes from TIMER_DURATION
        int initialHours = (int) (TIMER_DURATION / (60 * 60 * 1000));
        int initialMinutes = (int) ((TIMER_DURATION % (60 * 60 * 1000)) / (60 * 1000));

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Calculate the total time in milliseconds
                        long totalTimeInMillis = (hourOfDay * 60 + minute) * 60 * 1000;

                        // Update TIMER_DURATION and set the new time text
                        TIMER_DURATION = totalTimeInMillis;
                        updateTimerText();
                    }

                },
                initialHours,
                initialMinutes,
                true // Use 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    private void updateTimerText() {
        if (timeRemaining > 0) {
            // If there's remaining time, update the timer text using timeRemaining
            int minutes = (int) (timeRemaining / (60 * 1000));
            int seconds = (int) ((timeRemaining % (60 * 1000)) / 1000);
            String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            tv_timer.setText(timeText);
        } else {
            // If timeRemaining is 0, update the timer text using TIMER_DURATION
            int minutes = (int) (TIMER_DURATION / (60 * 1000));
            int seconds = (int) ((TIMER_DURATION % (60 * 1000)) / 1000);
            String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            tv_timer.setText(timeText);
        }
    }


    private void startTimer() {
        setBottomNavigationClickable(false);
        tv_timer.setEnabled(false);
        isRestTimer = false;
        button_stop.setEnabled(true);
        timeRemaining = TIMER_DURATION;

        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Work time
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                // Update the timer text
                String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tv_timer.setText(timeText);
                tv_update.setText("Task Ongoing");
                timeRemaining = millisUntilFinished;

                int progress = (int) (millisUntilFinished * 100 / TIMER_DURATION);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                // Work time finished, start rest time
                restTime();
                String title = "FocusPal";
                String message = "Task Finished Get Some Rest First!";
                setAlarm(title, message);
                playWorkFinishedSound();
            }
        };

        countDownTimer.start();
        isTimerRunning = true;
        button_pause.setVisibility(View.VISIBLE);
        button_play.setVisibility(View.INVISIBLE);
    }
    private boolean isRestTimer;
    private void resumeTimer(long remainingTime) {
        tv_timer.setEnabled(false);
        setBottomNavigationClickable(false);
        button_stop.setEnabled(true);

        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Work time
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                // Update the timer text
                String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tv_timer.setText(timeText);
                if(!isRestTimer){
                    tv_update.setText("Task Ongoing");
                }else {
                    tv_update.setText("Rest Time");
                }
                timeRemaining = millisUntilFinished;

                int progress = (int) (millisUntilFinished * 100 / (isRestTimer ? REST_DURATION : TIMER_DURATION));
                progressBar.setProgress(progress);

                button_pause.setVisibility(View.VISIBLE);
                button_play.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinish() {
                if (isRestTimer) {
                    playRestFinishedSound();
                    startTimer();
                }else {
                    playWorkFinishedSound();
                    restTime();
                }
            }
        };

        countDownTimer.start();
        isTimerRunning = true;
        button_play.setVisibility(View.VISIBLE);
        button_pause.setVisibility(View.INVISIBLE);
    }


    private void pauseTimer() {
        tv_timer.setEnabled(false);
        setBottomNavigationClickable(true);
        countDownTimer.cancel();
        isTimerRunning = false;
        button_pause.setVisibility(View.INVISIBLE);
        button_play.setVisibility(View.VISIBLE);
        button_stop.setEnabled(true);
        if(!isRestTimer){
            tv_update.setText("Task is Paused");
        }else {
            tv_update.setText("Rest is Paused");
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            tv_timer.setEnabled(true);
            countDownTimer.cancel();
            isTimerRunning = false;
            button_pause.setEnabled(false);
            button_play.setEnabled(false);
            button_pause.setVisibility(View.INVISIBLE);
            button_play.setVisibility(View.VISIBLE);
            button_stop.setEnabled(false);
            progressBar.setProgress((int) TIMER_DURATION);
            tv_update.setText("");
            tv_timer.setText("");
            tv_task.setText("");
            timeRemaining = 0;
            updateTimerText();


            setBottomNavigationClickable(true);
        }
    }

    public void restTime() {
        isRestTimer = true;
        countDownTimer = new CountDownTimer(REST_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate minutes and seconds from milliseconds
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                // Update the timer text
                String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tv_timer.setText(timeText);
                tv_update.setText("Rest Time");
                timeRemaining = millisUntilFinished;

                // Calculate the progress during the rest time

                int progress = (int) (millisUntilFinished * 100 / REST_DURATION);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                playRestFinishedSound();
                String title = "FocusPal";
                String message = "Rest Finished Focus now in your Task!";
                setAlarm(title, message);
                startTimer();
            }
        };
        countDownTimer.start();  // Start the rest time countdown
    }
    public void getBundle(){
        Bundle bundle = getArguments();
        if (bundle != null) {
            String text = bundle.getString("text");

            // Find the TextView in your TimerFragment layout and set the text
            tv_task.setText(text);
            timerViewModel.setTaskText(text);

        } else {
            // No bundle, retrieve the task text from the TimerViewModel
            String taskText = timerViewModel.getTaskText();
                tv_task.setText(taskText);
        }
    }

    public void saveData(String taskText){

        databaseFinishedTaskHelper = new DatabaseFinishedTaskHelper(getActivity());
        databaseFinishedTaskHelper.insertData(taskText);

        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.deleteTask(taskText);


    }
    private void setBottomNavigationClickable(boolean clickable) {
        // Enable or disable clickability of the bottom navigation
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(clickable);
        }
    }

    private void finishTask() {
        tv_timer.setEnabled(true);
        String taskText = tv_task.getText().toString();
        if (!taskText.isEmpty()) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // Check if getArguments() is not null before using it
            Bundle args = getArguments();
            if (args != null) {
                args.remove("text");
            }

            timerViewModel.setTaskText("");
            timeRemaining = 0;
            tv_task.setText("");
            updateTimerText();
            tv_update.setText("");
            progressBar.setProgress((int) TIMER_DURATION);
            saveData(taskText);

            // Allow the user to start a new task
            setBottomNavigationClickable(true);
            button_play.setEnabled(false);
        } else {
            Toast.makeText(requireContext(), "No Current Task!", Toast.LENGTH_SHORT).show();
        }
    }



    // Add this as a class variable
    private MediaPlayer mediaPlayer;

    private void playWorkFinishedSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm);
        mediaPlayer.start();
    }

    private void playRestFinishedSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.bell);
        mediaPlayer.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the MediaPlayer instance when the view is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setAlarm(String notificationTitle, String notificationContent){
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(AlarmReceiver.class)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .addTag("wala")
                .setInputData(new Data.Builder()
                        .putString("title", notificationTitle)
                        .putString("content", notificationContent)
                        .build())
                .build();

        WorkManager.getInstance(requireContext()).enqueue(notificationWork);
    }

}

