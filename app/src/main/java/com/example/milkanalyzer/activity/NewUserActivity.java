package com.example.milkanalyzer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.example.milkanalyzer.AppManager;
import com.example.milkanalyzer.firebase.FireBaseHelper;
import com.example.milkanalyzer.R;
import com.example.milkanalyzer.databinding.ActivityNewUserBinding;
import com.example.milkanalyzer.object.User;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewUserActivity extends AppCompatActivity {


    private ActivityNewUserBinding mBinding;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_user);

        mBinding.save.setOnClickListener(view -> {
            saveData();
        });

        AppManager.getConnectedThread().setHandler(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                break;
                            case -1:
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String readMessage = msg.obj.toString(); // Read message from Arduino
                        getData(readMessage.split(":"));
                        break;
                }
            }
        });
    }

    private void saveData() {
        if (mBinding.name.getText().toString().trim().length() == 0) {
            mBinding.name.requestFocus();
            return;
        }
        if (mBinding.surname.getText().toString().trim().length() == 0) {
            mBinding.surname.requestFocus();
            return;
        }
        if (mBinding.phone.getText().toString().trim().length() == 0) {
            mBinding.phone.requestFocus();
            return;
        }

        if (mBinding.cardNo.getText().toString().trim().length() == 0) {
            mBinding.cardNo.requestFocus();
            return;
        }
        if (mBinding.villageName.getText().toString().trim().length() == 0) {
            mBinding.villageName.requestFocus();
            return;
        }
        if (mBinding.villageNo.getText().toString().trim().length() == 0) {
            mBinding.villageNo.requestFocus();
            return;
        }
        if (mBinding.target.getText().toString().trim().length() == 0) {
            mBinding.target.requestFocus();
            return;
        }
        if (mBinding.taken.getText().toString().trim().length() == 0) {
            mBinding.taken.requestFocus();
            return;
        }
        FireBaseHelper fireBaseHelper = new FireBaseHelper();
        User user = new User();
        user.setName(mBinding.name.getText().toString());
        user.setSurName(mBinding.surname.getText().toString());
        user.setVillageName(mBinding.villageName.getText().toString());
        user.setVillageNo(mBinding.villageNo.getText().toString());
        user.setPhone(mBinding.phone.getText().toString());
        user.setId(mBinding.cardNo.getText().toString());
        user.setFullName(mBinding.name.getText().toString() + " " + mBinding.surname.getText().toString());
        user.setAddress(mBinding.address.getText().toString());
        user.setTargetMilk(mBinding.target.getText().toString());
        user.setTakenMilk(mBinding.taken.getText().toString());
        user.setLastTakenMilk("0");
        String pattern = "dd.MM.yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        user.setLastTakenDate(simpleDateFormat.format(new Date()));
        fireBaseHelper.addUser(user.getId(), user).addOnSuccessListener(suc -> {
            Toast.makeText(NewUserActivity.this, "Kullanıcı Oluşturuldu.", Toast.LENGTH_LONG).show();
            onBackPressed();
        }).addOnFailureListener(fail -> {
            Toast.makeText(NewUserActivity.this, "Kullanıcı Oluşturulamadı.", Toast.LENGTH_LONG).show();
        });
    }

    public void getData(String[] strings) {
        if ("rfid".equals(strings[0])) {
            mBinding.cardNo.setText(strings[1]);
        }
    }

}

