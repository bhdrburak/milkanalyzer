package com.example.milkanalyzer.object;

public class Milk {

    private String totalMilk;
    private String takenMilk;
    private String targetMilk;

    public Milk(String totalMilk, String takenMilk, String targetMilk){
        this.totalMilk = totalMilk;
        this.takenMilk = takenMilk;
        this.targetMilk = targetMilk;
    }

    public String getTotalMilk() {
        return totalMilk;
    }

    public void setTotalMilk(String totalMilk) {
        this.totalMilk = totalMilk;
    }

    public String getTakenMilk() {
        return takenMilk;
    }

    public void setTakenMilk(String takenMilk) {
        this.takenMilk = takenMilk;
    }

    public String getTargetMilk() {
        return targetMilk;
    }

    public void setTargetMilk(String targetMilk) {
        this.targetMilk = targetMilk;
    }
}
