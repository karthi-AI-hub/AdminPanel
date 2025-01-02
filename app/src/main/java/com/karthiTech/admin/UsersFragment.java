package com.karthiTech.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.karthiTech.admin.databinding.FragmentUsersBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UsersFragment extends Fragment {

    private final String TAG = "UsersFragment";
    private FragmentUsersBinding binding;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersCollectionRef = db.collection("Users");
    private List<User> userList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();
    private final MutableLiveData<List<User>> liveUserList = new MutableLiveData<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recyclerView);
        SearchView searchView = root.findViewById(R.id.searchView);

        adapter = new UserAdapter(filteredUserList, this::onUserSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        binding.addUserButton.setOnClickListener(v -> showAddUserDialog());
        binding.deleteButton.setOnClickListener(v -> deleteSelectedUsers());

        fetchUsers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        return root;
    }

    private void fetchUsers() {
        usersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening to users collection", error);
                return;
            }

            if (value != null) {
                List<User> users = new ArrayList<>();
                for (DocumentSnapshot document : value) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                userList = users;
                liveUserList.setValue(users);

                filteredUserList.clear();
                filteredUserList.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            String lowerCaseQuery = query.trim().toLowerCase();
            for (User user : userList) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerCaseQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery)) ||
                        (user.getPhone() != null && user.getPhone().contains(lowerCaseQuery))) {
                    filteredUserList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New User");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(getContext());
        nameInput.setHint("Name");
        layout.addView(nameInput);

        final EditText emailInput = new EditText(getContext());
        emailInput.setHint("Email");
        layout.addView(emailInput);

        final EditText phoneInput = new EditText(getContext());
        phoneInput.setHint("Phone");
        layout.addView(phoneInput);

        final EditText passwordInput = new EditText(getContext());
        passwordInput.setHint("Password");
        layout.addView(passwordInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String phone = phoneInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (validateInput(name, email, phone, password)) {
                User newUser = new User(name, email, phone, password, false, new Timestamp(new Date()));
                addUser(newUser);
            } else {
                Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean validateInput(String name, String email, String phone, String password) {
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }

        if (!Patterns.PHONE.matcher(phone).matches()) {
            return false;
        }

        return password.length() >= 6;
    }


    private void addUser(User user) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.fetchSignInMethodsForEmail(user.getEmail())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (!signInMethods.isEmpty()) {
                            Toast.makeText(getContext(), "Email is already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                                    .addOnSuccessListener(authResult -> {
                                        usersCollectionRef.document(user.getPhone()).set(user, SetOptions.merge())
                                                .addOnSuccessListener(documentReference -> Log.d(TAG, "User added with ID: " + user.getPhone()))
                                                .addOnFailureListener(e -> Log.e(TAG, "Error adding user to Firestore", e));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error creating user in Firebase Authentication", e);
                                        Toast.makeText(getContext(), "Error creating user", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }


    private void deleteSelectedUsers() {
        Set<String> selectedUserIds = adapter.getSelectedUserIds();
        if (!selectedUserIds.isEmpty()) {
            for (String userId : selectedUserIds) {
                deleteNestedCollections(userId);

                usersCollectionRef.document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("Email");
                                String password = documentSnapshot.getString("Password");
                                if (email != null && password != null) {
                                    deleteUserFromFirebaseAuth(email, password, userId);
                                }
                            } else {
                                Log.e(TAG, "User document does not exist: " + userId);
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error fetching user for FirebaseAuth deletion: ", e));
            }

            performBatchDeletion(selectedUserIds);
        } else {
            Toast.makeText(getContext(), "No users selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNestedCollections(String userId) {
        db.collection("Payments").document(userId).collection("Withdrawals").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted Withdrawals subcollection document for userId: " + userId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting Withdrawals subcollection: ", e));
                        }
                    } else {
                        Log.e(TAG, "Error fetching Withdrawals subcollection: ", task.getException());
                    }
                });
    }

    private void performBatchDeletion(Set<String> userIds) {
        WriteBatch batch = db.batch();

        for (String userId : userIds) {
            DocumentReference userDocRef = usersCollectionRef.document(userId);
            DocumentReference paymentsDocRef = db.collection("Payments").document(userId);
            DocumentReference earningsDocRef = db.collection("Earnings").document(userId);

            batch.delete(userDocRef);
            batch.delete(paymentsDocRef);
            batch.delete(earningsDocRef);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch delete successful in Firestore");
                    userList.removeIf(user -> userIds.contains(user.getPhone()));
                    filteredUserList.removeIf(user -> userIds.contains(user.getPhone()));
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Selected users deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Batch delete failed in Firestore: ", e));
    }

    private void deleteUserFromFirebaseAuth(String email, String password, String userId) {
        FirebaseAuth tempAuth = FirebaseAuth.getInstance();
        tempAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = tempAuth.getCurrentUser();
                    if (user != null) {
                        user.delete()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FirebaseAuth user deleted successfully: " + userId))
                                .addOnFailureListener(e -> Log.e(TAG, "Error deleting FirebaseAuth user: " + userId, e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error signing in to delete FirebaseAuth user: " + userId, e));
    }


    private void onUserSelected(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("User Details");

        Date lastLoginDate = user.getLoginlogin().toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(lastLoginDate);

        String userInfo = "Name: " + user.getName() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Password: " + user.getPassword() + "\n" +
                "Phone: " + user.getPhone() + "\n" +
                "isVerified: " + user.isVerified() + "\n" +
                "LastLogin: " + formattedDate ;
        builder.setMessage(userInfo);

        builder.setPositiveButton("MANAGE",(dialog, which) -> showManageUserDialog(user));
        builder.setNegativeButton("EDIT", (dialog, which) -> showEditUserDialog(user));
        builder.setNeutralButton("DELETE", (dialog, which) -> deleteUser(user.getPhone()));
        builder.show();
    }

    private void showManageUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Manage User");
        builder.setMessage("Manage user: " + user.getPhone());

        builder.setNeutralButton("RESET PASSWORD", (dialog, which) -> manageUser(user, "ResetPassword"));
        builder.setPositiveButton("UPDATE EMAIL", (dialog, which) -> manageUser(user, "UpdateEmail"));
        builder.setNegativeButton("UPDATE PASSWORD", (dialog, which) -> manageUser(user, "UpdatePassword"));
        builder.show();
    }

    private void manageUser(User useR, String action) {
        FirebaseAuth tempAuth = FirebaseAuth.getInstance();
        tempAuth.signInWithEmailAndPassword(useR.getEmail(), useR.getPassword())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = tempAuth.getCurrentUser();
                    String MetaDate = user.getMetadata().toString();
                    Toast.makeText(requireContext(), MetaDate, Toast.LENGTH_SHORT).show();
                    if (action == null) {
                        Log.e(TAG, "Action is null");
                        return;
                    } else if (action.equals("ResetPassword")) {
                        tempAuth.sendPasswordResetEmail(user.getEmail())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Password reset email sent to : " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error sending password reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else if (action.equals("UpdateEmail")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        final EditText editText = new EditText(getContext());

                        builder.setTitle("Update Email")
                                .setView(editText)
                                .setPositiveButton("Update", (dialog1, which) -> {
                                    String newEmail = editText.getText().toString();

                                    if (Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                                        user.updateEmail(newEmail)
                                                .addOnSuccessListener(aVoid -> {
                                                    useR.setEmail(newEmail);
                                                    Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Error updating email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null);

                        AlertDialog dialog = builder.create();
                        dialog.show();
                } else if (action.equals("UpdatePassword")) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                        final EditText editText2 = new EditText(getContext());

                        builder2.setTitle("Update Password")
                                .setView(editText2)
                                .setPositiveButton("Update", (dialog1, which) -> {
                                    String newPassword = editText2.getText().toString();

                                    if (newPassword != null && newPassword.length() >= 6) {
                                        user.updatePassword(newPassword)
                                                .addOnSuccessListener(aVoid -> {
                                                    useR.setPassword(newPassword);
                                                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Error updating Password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null);

                        AlertDialog dialog2 = builder2.create();
                        dialog2.show();
                    }else{
                        Toast.makeText(getContext(), "Unknown action: " + action, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit User");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(getContext());
        nameInput.setText(user.getName());
        layout.addView(nameInput);

        final EditText emailInput = new EditText(getContext());
        emailInput.setText(user.getEmail());
        layout.addView(emailInput);

        final EditText phoneInput = new EditText(getContext());
        phoneInput.setText(user.getPhone());
        layout.addView(phoneInput);

        final EditText passwordInput = new EditText(getContext());
        passwordInput.setText(user.getPassword());
        layout.addView(passwordInput);

        final CheckBox verifiedCheckbox = new CheckBox(getContext());
        verifiedCheckbox.setText("Is Verified");
        verifiedCheckbox.setChecked(user.isVerified());
        layout.addView(verifiedCheckbox);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            user.setName(nameInput.getText().toString());
            user.setEmail(emailInput.getText().toString());
            user.setPhone(phoneInput.getText().toString());
            user.setPassword(passwordInput.getText().toString());
            user.setVerified(verifiedCheckbox.isChecked());
            updateUser(user);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateUser(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", user.getName());
        updates.put("Email", user.getEmail());
        updates.put("Phone", user.getPhone());
        updates.put("Password", user.getPassword());
        updates.put("isVerified", user.isVerified());

        usersCollectionRef.document(user.getPhone()).update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating user", e));
    }

    private void deleteUser(String userId) {
        usersCollectionRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("Email");
                        String password = documentSnapshot.getString("Password");

                        if (email != null && password != null) {
                            deleteUserFromFirebaseAuth(email, password, userId);
                            userList.removeIf(user -> user.getPhone().equals(userId));
                            filteredUserList.removeIf(user -> user.getPhone().equals(userId));
                            adapter.notifyDataSetChanged();

                            deleteUserDataFromFirestore(userId);
                        } else {
                            Log.e(TAG, "Email or Password is null for userId: " + userId);
                            Toast.makeText(requireContext(), "User's email or password is missing.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "User document does not exist for userId: " + userId);
                        Toast.makeText(requireContext(), "User document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user document: ", e);
                    Toast.makeText(requireContext(), "Error fetching user document.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUserDataFromFirestore(String userId) {
        // Delete Withdrawals subcollection
        db.collection("Payments").document(userId).collection("Withdrawals").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    }

                    // Delete Payments document
                    db.collection("Payments").document(userId).delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Payments document deleted for userId: " + userId))
                            .addOnFailureListener(e -> Log.e(TAG, "Error deleting Payments document: " + userId, e));
                });

        // Delete Earnings document
        db.collection("Earnings").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Earnings document deleted for userId: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting Earnings document: " + userId, e));

        // Delete User document
        usersCollectionRef.document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document deleted for userId: " + userId);
                    Toast.makeText(requireContext(), "User deleted successfully: " + userId, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting user document: " + userId, e));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
