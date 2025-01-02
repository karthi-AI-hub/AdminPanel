package com.karthiTech.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentViewHolder> {

    private final List<Payment> paymentList;
    private final OnPaymentClickListener paymentClickListener;

    public PaymentsAdapter(List<Payment> paymentList, OnPaymentClickListener paymentClickListener) {
        this.paymentList = paymentList;
        this.paymentClickListener = paymentClickListener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        String formattedTime = formatTimestampToDate(payment.getTime());

        holder.userId.setText("User Id : " + payment.getParentUserId());
        holder.transactionId.setText("Transaction : " + payment.getId());
        holder.amountTextView.setText("Amount: ₹" + payment.getAmount());
        holder.statusTextView.setText("Status: " + payment.getStatus());
        holder.timeTextView.setText("Time: " + formattedTime);

        if("Completed".equals(payment.getStatus())){
            holder.approveButton.setVisibility(View.GONE);
        }else {
            holder.approveButton.setVisibility(View.VISIBLE);
            holder.approveButton.setOnClickListener(v -> paymentClickListener.onPaymentClick(payment));
        }
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView userId, transactionId, amountTextView, statusTextView, timeTextView;
        Button approveButton;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            userId = itemView.findViewById(R.id.tvuserID);
            transactionId = itemView.findViewById(R.id.tvTransId);
            amountTextView = itemView.findViewById(R.id.textAmount);
            statusTextView = itemView.findViewById(R.id.textStatus);
            timeTextView = itemView.findViewById(R.id.textTime);
            approveButton = itemView.findViewById(R.id.buttonApprove);
        }
    }

    public interface OnPaymentClickListener {
        void onPaymentClick(Payment payment);
    }

    private String formatTimestampToDate(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
            return sdf.format(date);
        }
        return "N/A";
    }
}
