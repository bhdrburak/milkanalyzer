package com.example.milkanalyzer.object;

import com.google.gson.annotations.SerializedName;

public class ValueModel {
    @SerializedName("fateRate")
    private String fateRate;
    @SerializedName("waterRate")
    private String waterRate;
    @SerializedName("snfRate")
    private String snfRate;
    @SerializedName("laktozRate")
    private String laktozRate;
    @SerializedName("proteinRate")
    private String proteinRate;
    @SerializedName("saltRate")
    private String saltRate;
    @SerializedName("weight")
    private String weight;


    public String getFateRate() {
        return fateRate;
    }

    public void setFateRate(String fateRate) {
        this.fateRate = fateRate;
    }

    public String getWaterRate() {
        return waterRate;
    }

    public void setWaterRate(String waterRate) {
        this.waterRate = waterRate;
    }

    public String getSnfRate() {
        return snfRate;
    }

    public void setSnfRate(String snfRate) {
        this.snfRate = snfRate;
    }

    public String getLaktozRate() {
        return laktozRate;
    }

    public void setLaktozRate(String laktozRate) {
        this.laktozRate = laktozRate;
    }

    public String getProteinRate() {
        return proteinRate;
    }

    public void setProteinRate(String proteinRate) {
        this.proteinRate = proteinRate;
    }

    public String getSaltRate() {
        return saltRate;
    }

    public void setSaltRate(String saltRate) {
        this.saltRate = saltRate;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
