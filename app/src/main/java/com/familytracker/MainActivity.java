package com.familytracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;


public class MainActivity extends AppCompatActivity implements Serializable {


    private static final String TAG = "MainActivity";
    private boolean backPressed = false;
    private FirebaseAuth firebaseAuth;

    private LoginTracker loginTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginTracker = new LoginTracker() {
            @Override
            public void loginComplete(LoginActivity loginActivity) {
                Intent intent = new Intent(loginActivity, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        };

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null){
            Log.d(TAG, "onCreate: User not logged in");
            // TODO: 7/9/2020 go to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("LoginTracker", loginTracker);
            startActivity(intent);
        } else{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
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