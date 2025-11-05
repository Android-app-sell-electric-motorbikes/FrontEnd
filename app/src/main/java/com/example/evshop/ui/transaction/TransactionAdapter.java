package com.example.evshop.ui.transaction;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.Transaction;
import com.example.evshop.util.Formatters;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderRef, tvAmount, tvStatus, tvCreatedAt;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderRef = itemView.findViewById(R.id.tvOrderRef);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }

        void bind(Transaction transaction) {
            tvOrderRef.setText("Ref: " + transaction.getOrderRef());
            tvAmount.setText(Formatters.currency(transaction.getAmount()));
            tvCreatedAt.setText(Formatters.date(transaction.getCreatedAt()));

            if (transaction.getStatus() == 1) {
                tvStatus.setText("THÀNH CÔNG");
                tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else {
                tvStatus.setText("THẤT BẠI");
                tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
            }
        }
    }
}
