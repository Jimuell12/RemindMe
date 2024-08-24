package com.example.remindme;

import androidx.lifecycle.ViewModel;

public class TimerViewModel extends ViewModel {
    private String taskText;

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }
}

