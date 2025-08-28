package com.example.goals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    private String secret;

    public static DatePickerFragment newInstance(String secret) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putString("secret", secret);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, container, false);

        if (getArguments() != null) {
            secret = getArguments().getString("secret");
        }

        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Button btnAdd = view.findViewById(R.id.btnAddDate);

        Calendar calendar = Calendar.getInstance();

        btnAdd.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1; // months are 0-based
            int year = datePicker.getYear();
            String selectedDate;
            if(month<10) {
                selectedDate = day + "-0" + month + "-" + year;
            }else{
                selectedDate = day + "-" + month + "-" + year;
            }

            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("secrets")
                    .child(secret)
                    .child("date");

            dbRef.setValue(selectedDate).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    dismiss(); // close fragment
                }
            });
        });

        return view;
    }
}
