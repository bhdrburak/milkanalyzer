package com.example.milkanalyzer.firebase;

import com.example.milkanalyzer.object.TakenMilk;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class FireBaseMilkHelper {
    public DatabaseReference databaseReference;


    public FireBaseMilkHelper(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(TakenMilk.class.getSimpleName());
    }

    public Task<Void> addMilkInformation(String key, TakenMilk takenMilk){
        return databaseReference.child(key).setValue(takenMilk);
    }

    public Task<Void> updateUser(String key, Map<String,Object> taskMap){
        return databaseReference.child(key).updateChildren(taskMap);
    }

    public Task<Void> removeUser(String key){
        return databaseReference.child(key).removeValue();
    }
}
