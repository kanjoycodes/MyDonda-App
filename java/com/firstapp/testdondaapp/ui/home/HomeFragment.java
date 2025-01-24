package com.firstapp.testdondaapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.firstapp.testdondaapp.R;
import com.firstapp.testdondaapp.TransactionHistory;
import com.firstapp.testdondaapp.databinding.FragmentHomeBinding;
import com.firstapp.testdondaapp.ui.FareCollection;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Observe text from ViewModel and set it to textHome
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Set up the button to navigate to FareCollection when clicked
        binding.btnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use NavController to navigate to the FareCollection fragment
                NavController navController = Navigation.findNavController(view);

                // Navigate to the FareCollection fragment in the nav_graph
                navController.navigate(R.id.nav_farecollection);
            }
        });


        // Set up the LinearLayout to navigate to the Transaction History fragment
        LinearLayout transactionHistoryLayout = binding.getRoot().findViewById(R.id.transaction_history);
        transactionHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use NavController to navigate to the TransactionHistory fragment
                NavController navController = Navigation.findNavController(view);

                // Navigate to the Transaction History fragment in the nav_graph
                navController.navigate(R.id.nav_transactionhistory);
            }
        });

        // Return the root view at the end
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
