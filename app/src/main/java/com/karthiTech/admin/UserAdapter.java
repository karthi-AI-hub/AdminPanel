package com.karthiTech.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final Set<String> selectedUserIds = new HashSet<>();
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.nameTextView.setText(user.getName());
        holder.emailTextView.setText(user.getPhone());

        holder.selectCheckbox.setOnCheckedChangeListener(null);
        holder.selectCheckbox.setChecked(selectedUserIds.contains(user.getPhone()));


        if(user.getLoginlogin() != null){
            Date lastLoginDate = user.getLoginlogin().toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(lastLoginDate);
            holder.lastLoginTextView.setText("Last Login: " + formattedDate);
        } else {
            holder.lastLoginTextView.setText("Last Login: N/A");

        }
        holder.selectCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUserIds.add(user.getPhone());
            } else {
                selectedUserIds.remove(user.getPhone());
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        CheckBox selectCheckbox;
        TextView lastLoginTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            selectCheckbox = itemView.findViewById(R.id.selectCheckbox);
            lastLoginTextView = itemView.findViewById(R.id.lastLoginTextView);
        }
    }

    public Set<String> getSelectedUserIds() {
        return selectedUserIds;
    }
}
