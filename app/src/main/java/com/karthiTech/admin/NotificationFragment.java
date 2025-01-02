package com.karthiTech.admin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.karthiTech.admin.databinding.FragmentNotificationBinding;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private EditText edtMessage;
    private Button btnSendNotification, btnSendInAppMessage;
    private FirebaseFirestore firestore;

    public NotificationFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        edtMessage = binding.edtMessage;
        btnSendNotification = binding.btnSendNotification;
        btnSendInAppMessage = binding.btnSendInAppMessage;
        firestore = FirebaseFirestore.getInstance();

        btnSendNotification.setOnClickListener(v -> sendNotification());
        btnSendInAppMessage.setOnClickListener(v -> sendInAppMessage());

        return root;
    }

    private void sendNotification() {
        String message = edtMessage.getText().toString().trim();
        if (message.isEmpty()) {
            edtMessage.setError("Please enter a message");
            return;
        }
        firestore.collection("Notifications").add(new NotificationMessage(message));
        edtMessage.setText("");

        sendPushNotification(message);

        createNotification(message);
    }
    private void sendPushNotification(String message) {
        RemoteMessage remoteMessage = new RemoteMessage.Builder("533728352951@fcm.googleapis.com")
                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                .addData("message", message)
                .build();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(token -> {
                    if (!token.isSuccessful()) {
                        Toast.makeText(getActivity(), "Error getting token", Toast.LENGTH_SHORT).show();
                    } else {
                        FirebaseMessaging.getInstance().send(remoteMessage);
                        Toast.makeText(getActivity(), "Notification sent successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(getActivity(), "default")
                .setContentTitle("Cashsify Notification")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        notificationManager.notify(1, notification);
    }

    private void sendInAppMessage() {
        String message = edtMessage.getText().toString().trim();
        if (message.isEmpty()) {
            edtMessage.setError("Please enter a message");
            return;
        }

        firestore.collection("inAppMessages").add(new InAppMessage(message))
                .addOnSuccessListener(documentReference -> Toast.makeText(getActivity(), "Message sent successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error sending message", Toast.LENGTH_SHORT).show());
    }

    public static class NotificationMessage {
        private String message;

        public NotificationMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class InAppMessage {
        private final String message;

        public InAppMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}