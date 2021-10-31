package com.example.milkanalyzer.firebase;

import com.example.milkanalyzer.object.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class FireBaseHelper {

    public DatabaseReference databaseReference;


    public FireBaseHelper(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(User.class.getSimpleName());
    }

    public Task<Void> addUser(String key, User user){
        return databaseReference.child(key).setValue(user);
    }

    public Task<Void> updateUser(String key, Map<String,Object> taskMap){
        return databaseReference.child(key).updateChildren(taskMap);
    }

    public Task<Void> removeUser(String key){
        return databaseReference.child(key).removeValue();
    }


}
