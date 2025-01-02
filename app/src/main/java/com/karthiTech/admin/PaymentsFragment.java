package com.karthiTech.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karthiTech.admin.databinding.FragmentPaymentsBinding;
import java.util.ArrayList;

public class PaymentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PaymentsAdapter paymentsAdapter;
    private ArrayList<Payment> paymentList;
    private FirebaseFirestore firestore;
    private FragmentPaymentsBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPaymentsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerViewPayments;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        paymentList = new ArrayList<>();
        paymentsAdapter = new PaymentsAdapter(paymentList, this::performAdminAction);

        recyclerView.setAdapter(paymentsAdapter);

        firestore = FirebaseFirestore.getInstance();
        loadPayments();

        return root;
    }

    private void loadPayments() {
        firestore.collectionGroup("Withdrawals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paymentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Payment payment = document.toObject(Payment.class);
                        payment.setId(document.getId());
                        payment.setParentUserId(document.getReference().getParent().getParent().getId());
                        paymentList.add(payment);
                    }
                    paymentsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading payments: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void performAdminAction(Payment payment) {
        firestore.collection("Payments")
                .document(payment.getParentUserId())
                .collection("Withdrawals")
                .document(payment.getId())
                .update("status", "Completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Payment approved!", Toast.LENGTH_SHORT).show();
                    loadPayments();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error approving payment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}