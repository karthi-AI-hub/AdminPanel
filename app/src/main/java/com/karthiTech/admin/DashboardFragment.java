package com.karthiTech.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.karthiTech.admin.databinding.FragmentDashboardBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseFirestore firestore;
    long verifiedUsers = 0;
    long nonVerifiedUsers = 0;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firestore = FirebaseFirestore.getInstance();
        fetchAnalytics();

        return root;
    }

    private void fetchAnalytics() {
        fetchUserInsights();
        fetchFinancialInsights();
        fetchPerformanceMetrics();
        fetchOperationalInsights();
        setupCharts();
    }

    private void fetchUserInsights() {
        firestore.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    binding.textTotalUsers.setText("Total Users : " + totalUsers);

                    verifiedUsers = queryDocumentSnapshots.getDocuments().stream()
                            .filter(doc -> Boolean.TRUE.equals(doc.getBoolean("isVerified")))
                            .count();
                    nonVerifiedUsers = totalUsers - verifiedUsers;

                    binding.textVerifiedUsers.setText("Verified Users : " + verifiedUsers);
                    binding.textNonVerifiedUsers.setText("Non-Verified Users : " + nonVerifiedUsers);
                });

        firestore.collection("Users")
                .whereEqualTo("LastLogin", getCurrentDate())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int activeUsersToday = queryDocumentSnapshots.size();
                    binding.textActiveUsersToday.setText("Active Users : " + activeUsersToday);
                });
    }

    private void fetchFinancialInsights() {
        firestore.collection("Earnings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalEarnings = queryDocumentSnapshots.getDocuments().stream()
                            .mapToDouble(doc -> doc.getDouble("cashTotal") != null ? doc.getDouble("cashTotal") : 0)
                            .sum();

                    double todayEarnings = queryDocumentSnapshots.getDocuments().stream()
                            .filter(doc -> getCurrentDate().equals(doc.getString("CurrentDate")))
                            .mapToDouble(doc -> doc.getDouble("cashToday") != null ? doc.getDouble("cashToday") : 0)
                            .sum();

                    binding.textTotalEarnings.setText("TotalEarnings : \u20B9 " + totalEarnings);
                    binding.textTodayEarnings.setText("TodayEarnings : \u20B9 " + todayEarnings);
                });

        firestore.collection("Payments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalWithdrawals = queryDocumentSnapshots.size();
                    double totalWithdrawalAmount = queryDocumentSnapshots.getDocuments().stream()
                            .mapToDouble(doc -> doc.getDouble("amount") != null ? doc.getDouble("amount") : 0)
                            .sum();

                    long pendingWithdrawals = queryDocumentSnapshots.getDocuments().stream()
                            .filter(doc -> "Pending".equals(doc.getString("status")))
                            .count();

                    double pendingWithdrawalAmount = queryDocumentSnapshots.getDocuments().stream()
                            .filter(doc -> "Pending".equals(doc.getString("status")))
                            .mapToDouble(doc -> doc.getDouble("amount") != null ? doc.getDouble("amount") : 0)
                            .sum();

                    long todayWithdrawals = queryDocumentSnapshots.getDocuments().stream()
                            .filter(doc -> getCurrentDate().equals(doc.getString("time")))
                            .count();

                    binding.textTotalWithdrawals.setText("Total Withdrawals : " + totalWithdrawals);
                    binding.textPendingWithdrawals.setText("Pending Withdrawals : " + pendingWithdrawals);
                    binding.textTodayWithdrawals.setText("Today Withdrawals : " + todayWithdrawals);
                    binding.textPendingWithdrawalAmount.setText("Pending Withdrawal Amount : \u20B9 " + pendingWithdrawalAmount);
                    binding.textTotalWithdrawalAmount.setText("Total Withdrawal Amount : \u20B9 " + totalWithdrawalAmount);

                });
    }

    private void fetchPerformanceMetrics() {
        firestore.collection("Earnings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalAdEngagement = queryDocumentSnapshots.getDocuments().stream()
                            .mapToDouble(doc -> doc.getDouble("completedTasks") != null ? doc.getDouble("completedTasks") : 0)
                            .sum();

                    double todayAdEngagement = queryDocumentSnapshots.getDocuments().stream()
                            .filter(doc -> getCurrentDate().equals(doc.getString("CurrentDate")))
                            .mapToDouble(doc -> doc.getDouble("completedTasks") != null ? doc.getDouble("completedTasks") : 0)
                            .sum();

                    double averageEarningsPerUser = totalAdEngagement / queryDocumentSnapshots.size();

                    binding.textTotalAdEngagement.setText("Total AdEngagement : " + totalAdEngagement);
                    binding.textTodayAdEngagement.setText("Today AdEngagement : " + todayAdEngagement);
                    binding.textAverageEarnings.setText("Average Earnings : \u20B9 " + averageEarningsPerUser);
                });
    }

    private void fetchOperationalInsights() {
        firestore.collection("Earnings")
                .orderBy("cashTotal", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> topEarningUsers = queryDocumentSnapshots.getDocuments().stream()
                            .map(DocumentSnapshot::getId)
                            .collect(Collectors.toList());
                    binding.textTopEarningUsers.setText("Top Earning Users : " + String.join(", ", topEarningUsers));
                });

        firestore.collection("Payments")
                .orderBy("amount", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> frequentWithdrawers = queryDocumentSnapshots.getDocuments().stream()
                            .map(DocumentSnapshot::getId)
                            .collect(Collectors.toList());
                    binding.textFrequentWithdrawers.setText("Frequent Withdrawers : " +String.join(", ", frequentWithdrawers));
                });
    }

    private void setupCharts() {
        setupPieChart();
        setupLineChart();
    }

    private void setupPieChart() {
        PieChart pieChart = binding.pieChart;
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(verifiedUsers, "Verified Users"));
        entries.add(new PieEntry(nonVerifiedUsers, "Non-Verified Users"));

        PieDataSet dataSet = new PieDataSet(entries, "User Verification");
        dataSet.setColors(new int[]{Color.GREEN, Color.RED});
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.invalidate();
    }

    private void setupLineChart() {
        LineChart lineChart = binding.lineChart;
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 50));
        entries.add(new Entry(2, 100));
        entries.add(new Entry(3, 150));

        LineDataSet dataSet = new LineDataSet(entries, "Earnings Trend");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }


    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


/*
User Insights
Total Users: Display the total number of users registered in the app.
Active Users Today: Show the number of users who logged in or used the app today.
Verified vs. Non-Verified Users: Separate counts for verified and non-verified users.
Financial Insights
Total Earnings: Aggregate the total amount earned by all users.
Today's Earnings: Sum of the amounts earned by all users today.
Total Withdrawals: Total number and amount of withdrawals requested by users.
Today's Withdrawals: Count and total amount of withdrawals processed today.
Pending Withdrawals: Count and total amount of withdrawals pending approval.
Performance Metrics
Ad Engagement: Number of ads watched by all users today and cumulatively.
Average Earnings per User: Average amount earned by a user in a day or over time.
Operational Insights
Top Earning Users: List of users with the highest earnings.
Frequent Withdrawers: Users who make frequent withdrawal requests.
Most Active Users: Users with the highest activity levels (logins, ad views, etc.).
Additional Features
Date Range Filter: Allow admin to view data for a specific date range.
Real-Time Updates: Automatically refresh key stats in real-time.
Charts and Graphs: Use tools like MPAndroidChart to display trends (e.g., earnings, active users) visually.
Notifications Summary: Insights into notifications sent, read, and acted upon.
User Feedback: Highlight unresolved user complaints or feedback.
 */