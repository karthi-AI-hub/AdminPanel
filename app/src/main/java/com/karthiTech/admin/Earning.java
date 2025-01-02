package com.karthiTech.admin;

public class Earning {
    private String id; // Firestore document ID
    private int cashToday;
    private int cashTotal;
    private int completedTasks;
    private int referToday;
    private int referTotal;

    // Empty constructor for Firestore
    public Earning() {}

    public Earning(String id, int cashToday, int cashTotal, int completedTasks, int referToday, int referTotal) {
        this.id = id;
        this.cashToday = cashToday;
        this.cashTotal = cashTotal;
        this.completedTasks = completedTasks;
        this.referToday = referToday;
        this.referTotal = referTotal;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCashToday() {
        return cashToday;
    }

    public void setCashToday(int cashToday) {
        this.cashToday = cashToday;
    }

    public int getCashTotal() {
        return cashTotal;
    }

    public void setCashTotal(int cashTotal) {
        this.cashTotal = cashTotal;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getReferToday() {
        return referToday;
    }

    public void setReferToday(int referToday) {
        this.referToday = referToday;
    }

    public int getReferTotal() {
        return referTotal;
    }

    public void setReferTotal(int referTotal) {
        this.referTotal = referTotal;
    }
}

