package com.firstapp.testdondaapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firstapp.testdondaapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TransactionHistory extends Fragment {

    private FirebaseFirestore db;
    private TableLayout tableLayout;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        tableLayout = view.findViewById(R.id.transactions); // Reference to the TableLayout in your XML

        // Start the real-time listener for transactions
        startRealTimeListener();

        return view;
    }

    private void startRealTimeListener() {
        listenerRegistration = db.collection("Transactions") // Collection name
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        // Clear previous rows (except the header)
                        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1));

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Get the M-Pesa receipt number from Firestore
                            String mpesaReceiptNumber = document.getString("mpesaReceiptNumber");
                            // Get the timestamp from Firestore
                            Date timestamp = document.getDate("timestamp");  // Assuming 'timestamp' field exists

                            // Add the M-Pesa receipt number and timestamp to the table if it's not null
                            if (mpesaReceiptNumber != null && timestamp != null) {
                                addTableRow(mpesaReceiptNumber, timestamp);
                            }
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void addTableRow(String mpesaReceiptNumber, Date timestamp) {
        TableRow tableRow = new TableRow(getContext());
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create a TextView for the M-Pesa receipt number
        TextView receiptTextView = new TextView(getContext());
        receiptTextView.setText(mpesaReceiptNumber);
        receiptTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        receiptTextView.setPadding(8, 8, 8, 8);
        receiptTextView.setGravity(Gravity.CENTER);

        // Format the timestamp to display the date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(timestamp);

        // Create a TextView for the timestamp (date and time)
        TextView dateTextView = new TextView(getContext());
        dateTextView.setText(formattedDate);
        dateTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        dateTextView.setPadding(8, 8, 8, 8);
        dateTextView.setGravity(Gravity.CENTER);

        // Add the TextViews to the TableRow
        tableRow.addView(receiptTextView);
        tableRow.addView(dateTextView);

        // Add the TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
