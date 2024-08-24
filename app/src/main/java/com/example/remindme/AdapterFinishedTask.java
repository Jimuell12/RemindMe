package com.example.remindme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterFinishedTask extends RecyclerView.Adapter<AdapterFinishedTask.ViewHolder> {
    private final List<DataModel> dataList;


    public AdapterFinishedTask(List<DataModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.finished_task, parent, false);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textData);
        }
    }
}
