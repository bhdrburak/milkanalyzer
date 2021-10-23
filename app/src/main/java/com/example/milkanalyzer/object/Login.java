package com.example.milkanalyzer.object;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "book")
public class Login {

    @ColumnInfo(name = "username")
    private String username;
    @ColumnInfo(name = "password")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
