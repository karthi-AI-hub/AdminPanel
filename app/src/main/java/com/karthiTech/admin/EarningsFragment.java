package com.karthiTech.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.karthiTech.admin.databinding.FragmentEarningsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EarningsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EarningsAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Earning> earningsList;
    private FragmentEarningsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEarningsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        earningsList = new ArrayList<>();
        adapter = new EarningsAdapter(earningsList, this::showEditDeleteDialog);
        recyclerView.setAdapter(adapter);

        fetchEarnings();
        root.findViewById(R.id.btnAdd).setOnClickListener(v -> showAddDialog());

        return root;
    }

    private void fetchEarnings() {
        db.collection("Earnings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    earningsList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Earning earning = document.toObject(Earning.class);
                        earning.setId(document.getId());
                        earningsList.add(earning);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_earning, null);
        builder.setView(dialogView);

        dialogView.findViewById(R.id.etUserId).setVisibility(View.VISIBLE);
        EditText etUserId = dialogView.findViewById(R.id.etUserId);
        EditText etCashToday = dialogView.findViewById(R.id.etCashToday);
        EditText etCashTotal = dialogView.findViewById(R.id.etCashTotal);
        EditText etCompletedTask = dialogView.findViewById(R.id.etCompletedTask);
        EditText etReferToday = dialogView.findViewById(R.id.etReferToday);
        EditText etReferTotal = dialogView.findViewById(R.id.etReferTotal);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String userId = etUserId.getText().toString().trim();
            String cashToday = etCashToday.getText().toString().trim();
            String cashTotal = etCashTotal.getText().toString().trim();
            String completedTask = etCompletedTask.getText().toString().trim();
            String referToday = etReferToday.getText().toString().trim();
            String referTotal = etReferTotal.getText().toString().trim();

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(cashToday) || TextUtils.isEmpty(cashTotal) || TextUtils.isEmpty(completedTask) || TextUtils.isEmpty(referToday) || TextUtils.isEmpty(referTotal)) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            dialogView.findViewById(R.id.etUserId).setVisibility(View.GONE);

            Map<String, Object> earning = new HashMap<>();
            earning.put("cashToday", Integer.parseInt(cashToday));
            earning.put("cashTotal", Integer.parseInt(cashTotal));
            earning.put("completedTasks", Integer.parseInt(completedTask));
            earning.put("referToday", Integer.parseInt(referToday));
            earning.put("referTotal", Integer.parseInt(referTotal));

            db.collection("Earnings").document(userId)
                    .set(earning)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Earning added", Toast.LENGTH_SHORT).show();
                        fetchEarnings();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showEditDeleteDialog(Earning earning) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit/Delete Earning");
        builder.setMessage("Choose an action for user: " + earning.getId());

        builder.setPositiveButton("Edit", (dialog, which) -> showEditDialog(earning));
        builder.setNegativeButton("Delete", (dialog, which) -> deleteEarning(earning));
        builder.setNeutralButton("Cancel", null);

        builder.create().show();
    }

    private void showEditDialog(Earning earning) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_earning, null);
        builder.setView(dialogView);

        EditText etCashToday = dialogView.findViewById(R.id.etCashToday);
        EditText etCashTotal = dialogView.findViewById(R.id.etCashTotal);
        EditText etCompletedTask = dialogView.findViewById(R.id.etCompletedTask);
        EditText etReferToday = dialogView.findViewById(R.id.etReferToday);
        EditText etReferTotal = dialogView.findViewById(R.id.etReferTotal);

        etCashToday.setText(String.valueOf(earning.getCashToday()));
        etCashTotal.setText(String.valueOf(earning.getCashTotal()));
        etCompletedTask.setText(String.valueOf(earning.getCompletedTasks()));
        etReferToday.setText(String.valueOf(earning.getReferToday()));
        etReferTotal.setText(String.valueOf(earning.getReferTotal()));

        builder.setPositiveButton("Update", (dialog, which) -> {
            String cashToday = etCashToday.getText().toString().trim();
            String cashTotal = etCashTotal.getText().toString().trim();
            String completedTask = etCompletedTask.getText().toString().trim();
            String referToday = etReferToday.getText().toString().trim();
            String referTotal = etReferTotal.getText().toString().trim();

            if (TextUtils.isEmpty(cashToday) || TextUtils.isEmpty(cashTotal) || TextUtils.isEmpty(completedTask) || TextUtils.isEmpty(referToday) || TextUtils.isEmpty(referTotal)) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("cashToday", Integer.parseInt(cashToday));
            updates.put("cashTotal", Integer.parseInt(cashTotal));
            updates.put("completedTasks", Integer.parseInt(completedTask));
            updates.put("referToday", Integer.parseInt(referToday));
            updates.put("referTotal", Integer.parseInt(referTotal));

            db.collection("Earnings").document(earning.getId())
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Earning updated", Toast.LENGTH_SHORT).show();
                        fetchEarnings();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void deleteEarning(Earning earning) {
        db.collection("Earnings").document(earning.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Earning deleted", Toast.LENGTH_SHORT).show();
                    fetchEarnings();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}