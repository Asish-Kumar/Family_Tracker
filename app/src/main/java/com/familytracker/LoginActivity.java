package com.familytracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int lenOTP = 6;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private EditText mobileNumberET;
    private ImageButton editMobileNumberIB;
    private Button getOTPBtn;
    private TextView countDownTv;
    private ProgressBar progressBar;
    CountDownTimer countDownTimer;
    private EditText otpET;
    private Button resendBtn;
    private Button submitBtn;
    FirebaseAuth firebaseAuth;
    private String phoneNumber;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mobileNumberET = findViewById(R.id.idMobileNumberET_AL);
        editMobileNumberIB = findViewById(R.id.idEditMobileNumberIB_AL);
        getOTPBtn = findViewById(R.id.idGetOTPBtn_AL);
        countDownTv = findViewById(R.id.idCountdownTV_AL);
        progressBar = findViewById(R.id.idProgressPB_AL);  //visibility = INVISIBLE by default
        otpET = findViewById(R.id.idOTPET_AL);  //visibility = INVISIBLE by default
        resendBtn = findViewById(R.id.idResendOTPBtn_AL);  //visibility = INVISIBLE by default
        submitBtn = findViewById(R.id.idSubmitBtn_AL);  //visibility = INVISIBLE by default

        progressBar.setProgress(60);
        firebaseAuth = FirebaseAuth.getInstance();

        editMobileNumberIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultState();
            }
        });

        mobileNumberET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.ACTION_DOWN)  {
                    //do here your stuff f
                    getOTPBtn.performClick();
                    return true;
                }
                return false;
            }
        });


        getOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This time should be equal to the onCodeAutoRetrievalTimeOut time
                startTimer(60);
                sendOTRequest();
            }
        });

        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(phoneNumber, mResendToken);
                countDownTv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                startTimer(60);

                resendBtn.setEnabled(false);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);

            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                mobileNumberET.setError("Authentication failed: Invalid mobile number!");
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(LoginActivity.this, "Maximum try quota reached. Please try after some time.", Toast.LENGTH_SHORT).show();
            }

            defaultState();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            //ask user to enter otp, if auto verification completes before, this part will be skipped
            enterOTPState();

        }

    };

    private void startTimer(final int sec){
        countDownTimer = new CountDownTimer(sec*1000, 1000) {
            @Override
            public void onTick(long l) {
                long sec = l /1000;
                countDownTv.setText(""+sec);

            }

            @Override
            public void onFinish() {
                countDownTv.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                resendBtn.setEnabled(true);
            }
        }.start();
    }

    private void signIn(){
        String code = otpET.getText().toString();
        if(code.length() != lenOTP){
            otpET.setError("Invalid OTP!");
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void defaultState(){
        mobileNumberET.setEnabled(true);
        getOTPBtn.setVisibility(View.VISIBLE);
        countDownTv.setVisibility(View.INVISIBLE);
        countDownTimer.cancel();
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setProgress(100);
        otpET.setText("");
        otpET.setVisibility(View.INVISIBLE);
        resendBtn.setVisibility(View.INVISIBLE);
        resendBtn.setEnabled(false);
        submitBtn.setVisibility(View.INVISIBLE);
    }

    private void enterOTPState(){
        mobileNumberET.setEnabled(false);
        getOTPBtn.setVisibility(View.INVISIBLE);
        countDownTv.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        otpET.setVisibility(View.VISIBLE);
        resendBtn.setVisibility(View.VISIBLE);
        resendBtn.setEnabled(false);
        submitBtn.setVisibility(View.VISIBLE);
    }

    private void sendOTRequest(){
        phoneNumber = mobileNumberET.getText().toString();
        if(phoneNumber.isEmpty()){
            mobileNumberET.setError("Phone number is required!");
            mobileNumberET.requestFocus();
            return;
        }
        if(phoneNumber.length() != 10){
            mobileNumberET.setError("10 digit phone number required!");
            mobileNumberET.requestFocus();
            return;
        }
        if( ! phoneNumber.matches("[0-9]+")){
            return;
        }

        phoneNumber = "+91"+phoneNumber;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            // TODO: 6/23/2020 User profile pending
                            FirebaseUser user = task.getResult().getUser(); // this user result can be used to show user profile

                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                otpET.setError("Invalid otp");
                            }
                        }
                    }
                });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }



}
