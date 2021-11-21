package com.example.milkanalyzer.object;


import com.google.gson.annotations.SerializedName;

public class TakenMilk {

    @SerializedName("userId")
    private String userId;
    @SerializedName("takenMilk")
    private String takenMilk;
    @SerializedName("takenMilkDate")
    private String takenMilkDate;

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
