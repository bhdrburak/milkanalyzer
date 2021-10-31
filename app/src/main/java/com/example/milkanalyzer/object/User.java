package com.example.milkanalyzer.object;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "book")
public class User {

    @PrimaryKey()
    private String id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "surname")
    private String surName;
    @ColumnInfo(name = "fullname")
    private String fullName;
    @ColumnInfo(name = "company_name")
    private String companyName;
    @ColumnInfo(name = "address")
    private String address;
    @ColumnInfo(name = "phone_number")
    private String phone;
    @ColumnInfo(name = "mail")
    private String mail;
    @ColumnInfo(name = "user_pic")
    private String userPicture;
    @ColumnInfo(name = "village_name")
    private String villageName;
    @ColumnInfo(name = "village_no")
    private String villageNo;
    @ColumnInfo(name = "target_milk")
    private String targetMilk;
    @ColumnInfo(name = "last_taken_milk")
    private String lastTakenMilk;
    @ColumnInfo(name = "last_taken_date")
    private String lastTakenDate;
    @ColumnInfo(name = "taken_milk")
    private String takenMilk;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getVillageNo() {
        return villageNo;
    }

    public void setVillageNo(String villageNo) {
        this.villageNo = villageNo;
    }

    public String getTargetMilk() {
        return targetMilk;
    }

    public void setTargetMilk(String targetMilk) {
        this.targetMilk = targetMilk;
    }

    public String getLastTakenMilk() {
        return lastTakenMilk;
    }

    public void setLastTakenMilk(String lastTakenMilk) {
        this.lastTakenMilk = lastTakenMilk;
    }

    public String getLastTakenDate() {
        return lastTakenDate;
    }

    public void setLastTakenDate(String lastTakenDate) {
        this.lastTakenDate = lastTakenDate;
    }

    public String getTakenMilk() {
        return takenMilk;
    }

    public void setTakenMilk(String takenMilk) {
        this.takenMilk = takenMilk;
    }
}
