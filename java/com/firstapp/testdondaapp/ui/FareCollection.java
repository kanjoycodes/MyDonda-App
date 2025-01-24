package com.firstapp.testdondaapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.firstapp.testdondaapp.R;
import com.firstapp.testdondaapp.models.CollectFareRequest;
import com.firstapp.testdondaapp.network.ApiClient;
import com.firstapp.testdondaapp.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FareCollection extends Fragment {

    private TextView fareAmount;
    private TextInputEditText phoneNumber;
    private Spinner fareSpinner;
    private FirebaseFirestore firestore;
    private ArrayList<String> spinnerDataList;
    private HashMap<String, String> fareMap;
    private ArrayAdapter<String> adapter;

    public FareCollection() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fare_collection, container, false);

        // Initialize views
        fareSpinner = view.findViewById(R.id.mySpinner);
        fareAmount = view.findViewById(R.id.amount);
        phoneNumber = view.findViewById(R.id.phone_no);
        View collectBtn = view.findViewById(R.id.collectfare_btn);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        spinnerDataList = new ArrayList<>();
        fareMap = new HashMap<>();

        // Set up spinner adapter
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerDataList);
        fareSpinner.setAdapter(adapter);

        // Fetch fare settings from Firestore
        fetchFareSettings();

        // Handle spinner item selection
        fareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = spinnerDataList.get(position);
                String correspondingAmount = fareMap.get(selectedItem);
                if (correspondingAmount != null) {
                    fareAmount.setText(correspondingAmount);
                } else {
                    fareAmount.setText("Amount: N/A");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fareAmount.setText("");
            }
        });

        // Handle collect fare button click
        collectBtn.setOnClickListener(v -> collectFare());

        return view;
    }

    private void fetchFareSettings() {
        CollectionReference fareSettingsRef = firestore.collection("FareSettings");

        fareSettingsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                spinnerDataList.clear();
                fareMap.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String from = document.getString("from");
                    String to = document.getString("to");
                    Integer amount = document.getLong("amount").intValue();

                    if (from != null && to != null && amount != null) {
                        String mergedValue = from + " - " + to;
                        spinnerDataList.add(mergedValue);
                        fareMap.put(mergedValue, String.valueOf(amount));
                    }
                }

                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void collectFare() {
        // Get input from text fields
        String phoneNumberStr = phoneNumber.getText().toString().trim();
        String amountStr = fareAmount.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(phoneNumberStr)) {
            Toast.makeText(requireContext(), "Phone number is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phoneNumberStr.matches("^\\+?[0-9]{10,13}$")) {
            Toast.makeText(requireContext(), "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(requireContext(), "Amount is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Enter a valid numeric amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed with STK push
        initiateStkPush(phoneNumberStr, String.valueOf(amount));
    }

    private void initiateStkPush(String phone, String amount) {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        CollectFareRequest request = new CollectFareRequest(phone, amount);

        apiService.collectFare(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "STK Push initiated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to initiate STK Push", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
