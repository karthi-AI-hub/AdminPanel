package com.karthiTech.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EarningsAdapter extends RecyclerView.Adapter<EarningsAdapter.EarningsViewHolder> {

    private ArrayList<Earning> earningsList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Earning earning);
    }

    public EarningsAdapter(ArrayList<Earning> earningsList, OnItemClickListener onItemClickListener) {
        this.earningsList = earningsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public EarningsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_earning, parent, false);
        return new EarningsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EarningsViewHolder holder, int position) {
        Earning earning = earningsList.get(position);

        holder.tvUserId.setText("User ID: " + earning.getId());
        holder.tvCashToday.setText("Cash Today: " + earning.getCashToday());
        holder.tvCashTotal.setText("Cash Total: " + earning.getCashTotal());
        holder.tvCompletedTasks.setText("Tasks Completed: " + earning.getCompletedTasks());
        holder.tvReferToday.setText("Refer Today: " + earning.getReferToday());
        holder.tvReferTotal.setText("Refer Total: " + earning.getReferTotal());

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(earning));
    }

    @Override
    public int getItemCount() {
        return earningsList.size();
    }

    public static class EarningsViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserId, tvCashToday, tvCashTotal, tvCompletedTasks, tvReferToday, tvReferTotal;

        public EarningsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvCashToday = itemView.findViewById(R.id.tvCashToday);
            tvCashTotal = itemView.findViewById(R.id.tvCashTotal);
            tvCompletedTasks = itemView.findViewById(R.id.tvCompletedTasks);
            tvReferToday = itemView.findViewById(R.id.tvReferToday);
            tvReferTotal = itemView.findViewById(R.id.tvReferTotal);
        }
    }
}

