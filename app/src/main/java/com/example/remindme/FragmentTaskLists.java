package com.example.remindme;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentTaskLists#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTaskLists extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentTaskLists() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TasksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTaskLists newInstance(String param1, String param2) {
        FragmentTaskLists fragment = new FragmentTaskLists();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    Button add_button, close_task, add_data;
    ConstraintLayout add_task;
    private List<DataModel> dataList;
    private AdapterTask adapter;
    private EditText editText;
    private DatabaseHelper databaseHelper;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Calendar calendar;
    private Button DatePicker, TimePicker;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

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
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        dataList = new ArrayList<>();
        adapter = new AdapterTask(dataList);
        databaseHelper = new DatabaseHelper(getActivity());

        DatePicker = rootView.findViewById(R.id.btn_setDate);
        TimePicker = rootView.findViewById(R.id.btn_setTime);

        DatePicker.setOnClickListener(v -> {
            datepicker();
        });

        TimePicker.setOnClickListener(v -> {
            timepicker();
        });


        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        editText = rootView.findViewById(R.id.editText);
        add_data = rootView.findViewById(R.id.add_data);
        add_button = rootView.findViewById(R.id.add_button);
        close_task = rootView.findViewById(R.id.close_task);
        add_task = rootView.findViewById(R.id.add_task);

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_task.setVisibility(View.VISIBLE);
            }
        });

        close_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_task.setVisibility(View.GONE);
            }
        });

        // Retrieve data from the SQLite database and populate dataList
        dataList.addAll(databaseHelper.getAllData());
        adapter.notifyDataSetChanged();

        add_data.setOnClickListener(v -> {
            String inputText = editText.getText().toString().trim();

            if(inputText.isEmpty()){
                Toast.makeText(v.getContext(), "Please add title.", Toast.LENGTH_SHORT).show();
            }

            if (DatePicker.getText().toString().equals("Set Date") || TimePicker.getText().toString().equals("Set Time")) {
                Toast.makeText(v.getContext(), "Please select both date and time.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!inputText.isEmpty()) {
                if (inputText.length() <= 23) { // Check if the input text is 20 characters or less
                    databaseHelper.insertData(inputText);
                    setAlarm(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, inputText, "It's time to focus on your task!");
                    DataModel dataModel = new DataModel(inputText);
                    dataList.add(dataModel);
                    adapter.notifyItemInserted(dataList.size() - 1);
                    editText.getText().clear();
                    add_task.setVisibility(View.GONE);



                } else {
                    Toast.makeText(v.getContext(), "Input text should be 23 characters or less.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        return rootView;
    }

    private void timepicker() {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        // Create a TimePickerDialog with the current time as the default
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;

                        // Update the text of the TimePicker button
                        TimePicker.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                },
                currentHour,
                currentMinute,
                false // Set to true if you want 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    private void datepicker() {
        // Get the current date
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set the current date as the default
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        DatePicker.setText(String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                    }
                }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void setAlarm(int year, int month, int day, int hour, int minute, String notificationTitle, String notificationContent) {
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

        Toast.makeText(requireContext(), "Delay: " + delaySeconds + " seconds", Toast.LENGTH_SHORT).show();

        // Build and enqueue the notification work with the calculated delay
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(AlarmReceiver.class)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .addTag(notificationTitle)
                .setInputData(new Data.Builder()
                        .putString("title", notificationTitle)
                        .putString("content", notificationContent)
                        .build())
                .build();

        WorkManager.getInstance(requireContext()).enqueue(notificationWork);
    }

}