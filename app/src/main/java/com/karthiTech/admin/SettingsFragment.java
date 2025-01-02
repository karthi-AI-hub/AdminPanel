package com.karthiTech.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.karthiTech.admin.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final String COLLECTION_PATH = "AppSettings";
    private static final String FIELD_NAME = "Status";
    private FragmentSettingsBinding binding;

    private FirebaseFirestore db;
    private SwitchCompat SplashSwitch, WithdrawSwitch;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        SplashSwitch = binding.SplashSwitch;
        WithdrawSwitch = binding.WithdrawSwitch;


        fetchCurrentStatus();
        SplashSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatus("SplashSettings", isChecked);
        });
        WithdrawSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatus("WithdrawSettings", isChecked);
        });

        return root;
    }

    private void updateStatus(String documentName, boolean isChecked) {
        DocumentReference documentRef = db.collection(COLLECTION_PATH).document(documentName);

        documentRef.update(FIELD_NAME, isChecked)
                .addOnSuccessListener(aVoid -> {
                    String message = documentName.equals("SplashSettings")
                            ? "Splash screen status updated as " + isChecked
                            : "Withdraw settings status updated as " + isChecked;
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating " + documentName, e);
                    Toast.makeText(getContext(), "Error updating " + documentName, Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCurrentStatus() {
        db.collection(COLLECTION_PATH).document("SplashSettings")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean currentStatus = documentSnapshot.getBoolean(FIELD_NAME);
                        if (currentStatus != null) {
                            SplashSwitch.setChecked(currentStatus);
                        }}
        }).addOnFailureListener(e -> { Log.e(TAG, "Error fetching Splash screen status", e);});
        db.collection(COLLECTION_PATH).document("WithdrawSettings")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean currentStatus = documentSnapshot.getBoolean(FIELD_NAME);
                        if (currentStatus != null) {
                            WithdrawSwitch.setChecked(currentStatus);
                        }
                    }
                }).addOnFailureListener( e -> { Log.e(TAG, "Error fetching Withdraw screen status", e);});
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}