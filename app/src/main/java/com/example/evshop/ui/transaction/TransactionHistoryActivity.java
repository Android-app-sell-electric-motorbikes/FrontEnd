package com.example.evshop.ui.transaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.google.android.material.appbar.MaterialToolbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionHistoryActivity extends AppCompatActivity {

    private TransactionViewModel viewModel;
    private RecyclerView rvTransactions;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rvTransactions = findViewById(R.id.rvTransactions);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        toolbar.setNavigationOnClickListener(v -> finish());

        observeViewModel();
        viewModel.fetchTransactions();
    }

    private void observeViewModel() {
        viewModel.loading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.transactions.observe(this, transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvTransactions.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvTransactions.setVisibility(View.VISIBLE);
                rvTransactions.setAdapter(new TransactionAdapter(transactions));
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
