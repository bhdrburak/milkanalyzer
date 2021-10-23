package com.example.milkanalyzer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.milkanalyzer.FireBaseHelper;
import com.example.milkanalyzer.R;
import com.example.milkanalyzer.databinding.ActivityLoginBinding;
import com.example.milkanalyzer.databinding.ActivityNewUserBinding;
import com.example.milkanalyzer.object.Milk;
import com.example.milkanalyzer.object.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class NewUserActivity extends AppCompatActivity {


    private ActivityNewUserBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_user);

        mBinding.save.setOnClickListener(view -> {
            saveData();
        });
    }

    private void saveData(){
        if (mBinding.name.getText().toString().trim().length() == 0){
            mBinding.name.requestFocus();
            return;
        }
        if (mBinding.surname.getText().toString().trim().length() == 0){
            mBinding.surname.requestFocus();
            return;
        }
        if (mBinding.mail.getText().toString().trim().length() == 0){
            mBinding.mail.requestFocus();
            return;
        }
        if (mBinding.phone.getText().toString().trim().length() == 0){
            mBinding.phone.requestFocus();
            return;
        }
        if (mBinding.address.getText().toString().trim().length() == 0){
            mBinding.address.requestFocus();
            return;
        }
        if (mBinding.companyName.getText().toString().trim().length() == 0){
            mBinding.companyName.requestFocus();
            return;
        }
        if (mBinding.cardNo.getText().toString().trim().length() == 0){
            mBinding.cardNo.requestFocus();
            return;
        }
        FireBaseHelper fireBaseHelper = new FireBaseHelper();
        User user = new User();
        user.setName(mBinding.name.getText().toString());
        user.setSurName(mBinding.surname.getText().toString());
        user.setMail(mBinding.mail.getText().toString());
        user.setCompanyName(mBinding.companyName.getText().toString());
        user.setPhone(mBinding.phone.getText().toString());
        user.setId(mBinding.cardNo.getText().toString());
        user.setFullName(mBinding.name.getText().toString() + " " + mBinding.surname.getText().toString());
        fireBaseHelper.addUser(user.getId(), user).addOnSuccessListener(suc ->{
            FirebaseMilkHelper firebaseMilkHelper = new FirebaseMilkHelper();
            firebaseMilkHelper.addMilk(user.getId(), new Milk("0", mBinding.taken.getText().toString(), mBinding.target.getText().toString())).addOnSuccessListener(call ->{
                Toast.makeText(NewUserActivity.this, "Kayıt Başarılı.", Toast.LENGTH_LONG).show();
                onBackPressed();
            }).addOnFailureListener(fail ->{
                Toast.makeText(NewUserActivity.this, "Kaydedilemedi.", Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(fail ->{
            Toast.makeText(NewUserActivity.this, "Kaydedilemedi.", Toast.LENGTH_LONG).show();
        });
    }

}

class FirebaseMilkHelper {

    private DatabaseReference databaseReference;


    public FirebaseMilkHelper(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Milk.class.getSimpleName());
    }

    public Task<Void> addMilk(String key, Milk milk){
        return databaseReference.child(key).setValue(milk);
    }

    public Task<Void> updateUser(String key, Map<String,Object> taskMap){
        return databaseReference.child(key).updateChildren(taskMap);
    }

    public Task<Void> removeUser(String key){
        return databaseReference.child(key).removeValue();
    }


}