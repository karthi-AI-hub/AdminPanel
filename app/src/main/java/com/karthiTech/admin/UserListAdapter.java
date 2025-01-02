package com.karthiTech.admin;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final List<String> userIds;
    private final OnUserClickListener userClickListener;

    public UserListAdapter(List<String> userIds, OnUserClickListener userClickListener) {
        this.userIds = userIds;
        this.userClickListener = userClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userId = userIds.get(position);
        Log.d("UserListAdapter", "Binding userId: " + userId);
        holder.userIdTextView.setText(userId);
        holder.itemView.setOnClickListener(v -> userClickListener.onUserClick(userId));
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userIdTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userIdTextView = itemView.findViewById(R.id.textUserId);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(String userId);
    }
}

