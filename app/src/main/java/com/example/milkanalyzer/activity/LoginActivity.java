package com.example.milkanalyzer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.milkanalyzer.R;
import com.example.milkanalyzer.databinding.ActivityLoginBinding;
import com.example.milkanalyzer.object.Login;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

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
            mBinding.progressBar.setVisibility(View.VISIBLE);
            login();
        });
        login();
    }

    private void login(){
        FirebaseLoginHelper firebaseMilkHelper = new FirebaseLoginHelper();
        firebaseMilkHelper.databaseReference.child(mBinding.username.getEditText().getText().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Login user = dataSnapshot.getValue(Login.class);
                if (mBinding.username.getEditText().getText().toString().equals(user.getUsername()) && mBinding.password.getEditText().getText().toString().equals(user.getPassword())){
                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    myIntent.putExtra("username", user.getUsername()); //Optional parameters
                    LoginActivity.this.startActivity(myIntent);
                } else
                    Toast.makeText(LoginActivity.this, "Kullanıcı Adı veya Parola yanlış tekrar deneyin.", Toast.LENGTH_LONG).show();
                mBinding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("TAG", "Failed to read value.", error.toException());
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

    class FirebaseLoginHelper {

        private DatabaseReference databaseReference;


        public FirebaseLoginHelper(){
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            databaseReference = db.getReference(Login.class.getSimpleName());
        }

        public Task<Void> addLogin(String key, Login login){
            return databaseReference.child(key).setValue(login);
        }

        public Task<Void> updateUser(String key, Map<String,Object> taskMap){
            return databaseReference.child(key).updateChildren(taskMap);
        }

        public Task<Void> removeUser(String key){
            return databaseReference.child(key).removeValue();
        }

    }

}