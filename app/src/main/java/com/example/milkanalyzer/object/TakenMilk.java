package com.example.milkanalyzer.object;


public class TakenMilk {

    private String userId;
    private String takenMilk;
    private String takenMilkDate;

    public TakenMilk(String userId, String takenMilk, String takenMilkDate) {
        this.userId = userId;
        this.takenMilk = takenMilk;
        this.takenMilkDate = takenMilkDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTakenMilk() {
        return takenMilk;
    }

    public void setTakenMilk(String takenMilk) {
        this.takenMilk = takenMilk;
    }

    public String getTakenMilkDate() {
        return takenMilkDate;
    }

    public void setTakenMilkDate(String takenMilkDate) {
        this.takenMilkDate = takenMilkDate;
    }
}
