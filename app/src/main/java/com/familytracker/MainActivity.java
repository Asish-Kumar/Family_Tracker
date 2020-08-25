package com.familytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean backPressed = false;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if(currentUser == null){
            Log.d(TAG, "onCreate: User not logged in");
            // TODO: 7/9/2020 go to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            finish();
        } else {
            // if user has already set his profile then go directly to DashboardActivity
            // else go to ProfileActivity

            String userId = firebaseAuth.getUid();
            db.collection(userId+"_collection").document("profile")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    Log.d(TAG, "onComplete: updateUserNamesCollection: Document exist.");

                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Log.d(TAG, "onComplete: updateUserNamesCollection: Document does not exist.");

                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Log.d(TAG, "Failed with: ", task.getException());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Exception in getting user details.");
                            e.printStackTrace();
                        }
                    });

        }

        Log.d(TAG, "onCreate: hello");
    }


    @Override
    public void onBackPressed() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }

        this.backPressed = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                backPressed=false;
            }
        }, 2000);
    }



}