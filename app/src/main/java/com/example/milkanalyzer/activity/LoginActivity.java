package com.example.milkanalyzer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.milkanalyzer.R;
import com.example.milkanalyzer.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {


    private ActivityLoginBinding mBinding;
    private static final int PERMISSION_REQUEST_CODE = 10023;
    private String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    PERMISSION_REQUEST_CODE);
        }

        mBinding.login.setOnClickListener(view -> {
            String username = mBinding.username.getEditText().getText().toString();
            String password = mBinding.password.getEditText().getText().toString();
            if (username.equals("admin") && password.equals("1234")){
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                myIntent.putExtra("username", username); //Optional parameters
                LoginActivity.this.startActivity(myIntent);
            } else {
                Toast.makeText(LoginActivity.this, "Kullanıcı Adı veya Parola yanlış tekrar deneyin.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (String permission : PERMISSIONS) {
                    if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        checkPermissions();
                        return;
                    }
                }
                break;
        }
    }

}