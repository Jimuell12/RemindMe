package com.example.remindme;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdapterTask extends RecyclerView.Adapter<AdapterTask.ViewHolder> {
    private List<DataModel> dataList;


    public AdapterTask(List<DataModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataModel dataModel = dataList.get(position);
        holder.textView.setText(dataModel.getData());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void addTask(String taskText) {
        // Create a new DataModel object with the provided task text
        DataModel dataModel = new DataModel(taskText);

        // Add the new DataModel to the data list
        dataList.add(dataModel);

        // Notify the adapter that the data set has changed
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textData);
            btnEdit = itemView.findViewById(R.id.btn_edit);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AppCompatActivity appCompatActivity = (AppCompatActivity) v.getContext();
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String text = dataList.get(position).getData();
                        FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                        FragmentTimer fragmentTimer = new FragmentTimer();

                        Bundle bundle = new Bundle();
                        bundle.putString("text", text); // Pass the text to the TimerFragment
                        fragmentTimer.setArguments(bundle);

                        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragmentTimer).addToBackStack(null).commit();

                        BottomNavigationView bottomNavigationView = (BottomNavigationView) appCompatActivity.findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setSelectedItemId(R.id.menu_item2);

                    }
                }
            });

            itemView.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataModel dataModel = dataList.get(getAdapterPosition());
                    String taskText = dataModel.getData();

                    DatabaseHelper databaseHelper = new DatabaseHelper(v.getContext());
                    databaseHelper.deleteTask(taskText);
                    int position = getAdapterPosition();
                    dataList.remove(position);
                    WorkManager.getInstance(v.getContext()).cancelAllWorkByTag(taskText);
                    notifyDataSetChanged();
                }

            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the "Edit" button click here
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String currentTaskText = dataList.get(position).getData();

                        Dialog editDialog = new Dialog(itemView.getContext());
                        editDialog.setContentView(R.layout.edit_task_dialog);

                        // Find the child views using the Dialog's view
                        Button buttonDate = editDialog.findViewById(R.id.buttonDate);
                        Button buttonTime = editDialog.findViewById(R.id.buttonTime);
                        Button btnConfirm = editDialog.findViewById(R.id.btnConfirm);


                        // Show the Dialog
                        editDialog.show();

                        // Date Picker Dialog
                        buttonDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDatePickerDialog(itemView, buttonDate);
                            }
                        });

                        // Time Picker Dialog
                        buttonTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showTimePickerDialog(itemView, buttonTime);
                            }
                        });

                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                WorkManager.getInstance(v.getContext()).cancelAllWorkByTag(currentTaskText);
                                setAlarm(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, currentTaskText, "It's time to focus on your task!", itemView);
                                editDialog.hide();
                            }
                        });

                    }
                }
            });
        }
    }

    private void showTimePickerDialog(View itemView, Button buttonTime) {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        // Create a TimePickerDialog with the current time as the default
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                itemView.getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Handle time selection
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        buttonTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                },
                currentHour,
                currentMinute,
                false // Set to true if you want 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    private void showDatePickerDialog(View itemView, Button buttonDate) {
        // Get the current date
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set the current date as the default
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                itemView.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Handle date selection
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        buttonDate.setText(String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                    }
                },
                year,
                month,
                day
        );

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void setAlarm(int year, int month, int day, int hour, int minute, String notificationTitle, String notificationContent, View itemView) {
        // Get the current date and time
        Calendar currentTime = Calendar.getInstance();
        long currentMillis = currentTime.getTimeInMillis();

        // Set the selected date and time
        Calendar selectedTime = Calendar.getInstance();
        selectedTime.set(Calendar.YEAR, year);
        selectedTime.set(Calendar.MONTH, month);
        selectedTime.set(Calendar.DAY_OF_MONTH, day);
        selectedTime.set(Calendar.HOUR_OF_DAY, hour);
        selectedTime.set(Calendar.MINUTE, minute);
        selectedTime.set(Calendar.SECOND, 0);
        selectedTime.set(Calendar.MILLISECOND, 0);
        long selectedMillis = selectedTime.getTimeInMillis();

        // Calculate the delay in seconds
        long delaySeconds = (selectedMillis - currentMillis) / 1000;


        // Build and enqueue the notification work with the calculated delay
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(AlarmReceiver.class)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .addTag(notificationTitle)
                .setInputData(new Data.Builder()
                        .putString("title", "Task: "+notificationTitle)
                        .putString("content", notificationContent)
                        .build())
                .build();

        WorkManager.getInstance(itemView.getContext()).enqueue(notificationWork);
    }

}
