package com.example.remindme;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentFinishedTasks#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentFinishedTasks extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentFinishedTasks() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FinishedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentFinishedTasks newInstance(String param1, String param2) {
        FragmentFinishedTasks fragment = new FragmentFinishedTasks();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private AdapterFinishedTask adapterFinishedTask;
    private List<DataModel> dataList;
    private AdapterTask adapter;
    private DatabaseFinishedTaskHelper databaseFinishedHelper;

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
        View rootView = inflater.inflate(R.layout.fragment_finished, container, false);

        dataList = new ArrayList<>();
        adapter = new AdapterTask(dataList);

        databaseFinishedHelper = new DatabaseFinishedTaskHelper(getActivity());
        dataList.addAll(databaseFinishedHelper.getAllData());
        adapter.notifyDataSetChanged();

        recyclerView = rootView.findViewById(R.id.recycler_view_finished);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        recyclerView = rootView.findViewById(R.id.recycler_view_finished);
        adapterFinishedTask = new AdapterFinishedTask(dataList); // Replace TaskAdapter with your adapter class

        // Set up the RecyclerView with your adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapterFinishedTask);

        return rootView;
    }
}
