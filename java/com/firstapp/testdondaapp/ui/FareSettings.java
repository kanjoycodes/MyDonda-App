package com.firstapp.testdondaapp.ui;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firstapp.testdondaapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FareSettings extends Fragment {

    private FirebaseFirestore db;
    private TableLayout tableLayout;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fare_settings, container, false);

        db = FirebaseFirestore.getInstance();
        tableLayout = view.findViewById(R.id.saved_routes);

        TextInputEditText inputFrom = view.findViewById(R.id.From);
        TextInputEditText inputTo = view.findViewById(R.id.To);
        TextInputEditText inputAmount = view.findViewById(R.id.enter_rate);
        AppCompatButton addRouteButton = view.findViewById(R.id.add_route_btn);

        // Set up the Button click listener
        addRouteButton.setOnClickListener(v -> {
            String fromWhere = Objects.requireNonNull(inputFrom.getText()).toString().trim();
            String toWhere = Objects.requireNonNull(inputTo.getText()).toString().trim();
            String amount = Objects.requireNonNull(inputAmount.getText()).toString().trim();

            if (fromWhere.isEmpty()) {
                inputFrom.setError("Please enter route start");
                return;
            }
            if (toWhere.isEmpty()) {
                inputTo.setError("Please enter route end");
                return;
            }
            if (amount.isEmpty()) {
                inputAmount.setError("Please enter amount to charge");
                return;
            }

            int payRate;
            try {
                payRate = Integer.parseInt(amount);
            } catch (NumberFormatException e) {
                inputAmount.setError("Please enter a valid number for amount");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("from", fromWhere);
            data.put("to", toWhere);
            data.put("amount", payRate);

            db.collection("FareSettings")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Data successfully added with ID: " + documentReference.getId());
                        inputFrom.setText("");
                        inputTo.setText("");
                        inputAmount.setText("");
                        Toast.makeText(getContext(), "Route added successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding document", e);
                        Toast.makeText(getContext(), "Error adding route", Toast.LENGTH_SHORT).show();
                    });
        });

        startRealTimeListener();
        return view;
    }

    private void startRealTimeListener() {
        listenerRegistration = db.collection("FareSettings")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String from = document.getString("from");
                            String to = document.getString("to");
                            int amount = Objects.requireNonNull(document.getLong("amount")).intValue();
                            String documentId = document.getId();
                            addTableRow(from, to, amount, documentId);
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void addTableRow(String from, String to, int amount, String documentId) {
        TableRow tableRow = new TableRow(getContext());
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        View divider = new View(getContext());
        divider.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2));
        divider.setBackgroundColor(Color.LTGRAY);
        tableLayout.addView(divider);

        String displayText = from + " to " + to;
        int textColor = ContextCompat.getColor(requireContext(), R.color.black);

        TextView fromToTextView = new TextView(getContext());
        fromToTextView.setText(displayText);
        fromToTextView.setTextColor(textColor);
        fromToTextView.setPadding(8, 8, 8, 8);
        fromToTextView.setGravity(Gravity.CENTER_VERTICAL);

        TextView amountTextView = new TextView(getContext());
        amountTextView.setText(String.valueOf(amount));
        amountTextView.setTextColor(textColor);
        amountTextView.setPadding(18, 8, 8, 8);

        AppCompatButton button = new AppCompatButton(getContext());
        button.setText("DELETE");
        button.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.deletemaroongradient));
        button.setTag(documentId);
        button.setOnClickListener(v -> deleteDocument(documentId));

        tableRow.addView(fromToTextView);
        tableRow.addView(amountTextView);
        tableRow.addView(button);
        tableLayout.addView(tableRow);
    }

    private void deleteDocument(String documentId) {
        db.collection("FareSettings").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Document successfully deleted!");
                    Toast.makeText(getContext(), "Route deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting document", e);
                    Toast.makeText(getContext(), "Error deleting route", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
