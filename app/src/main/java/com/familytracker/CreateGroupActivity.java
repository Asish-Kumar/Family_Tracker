package com.familytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class CreateGroupActivity extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";
    public static final String USER_NAMES_COLLECTION = "usernames";
    final long ONE_MEGABYTE = 1024 * 1024;
    FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    private SearchView searchView;
    private LinearLayout linearLayout;
    private LayoutInflater friendCardInflater;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        init();

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String userName) {
                //search for the userName online
                // if user exists show a friends card with this user details
                // else show no such user found

                // check username availability
                progressDialog = new ProgressDialog(CreateGroupActivity.this);
                progressDialog.setMessage("Searching user...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                db.collection(USER_NAMES_COLLECTION)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: downloadAlluserNames task is successful.");
                                    boolean userExists = false;
                                    String searchedUserId = "";
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        if (userName.equals(document.getId())) {
                                            userExists = true;
                                            searchedUserId = document.get("userid").toString();
                                            break;
                                        }
                                    }
                                    progressDialog.dismiss();

                                    final String searchedUserIdTemp = searchedUserId; // for downloading profile pic
                                    if (userExists) {
                                        //get display name
                                        db.collection(searchedUserId + "_collection")
                                                .document("profile")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            String displayName = task.getResult().get("displayname").toString();

                                                            View v = friendCardInflater.inflate(R.layout.friend_card, null);
                                                            final ImageView profilePictureIV = v.findViewById(R.id.idProfilePictureIV_FC);
                                                            TextView displayNameTV = v.findViewById(R.id.idDisplayNameTV_FC);
                                                            TextView userNameTV = v.findViewById(R.id.idUserNameTV_FC);
                                                            ImageView addFriendIV = v.findViewById(R.id.idAddFriendIV_FC);

                                                            displayNameTV.setText(displayName);
                                                            userNameTV.setText(userName);

                                                            addFriendIV.setVisibility(View.VISIBLE);
                                                            addFriendIV.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    // TODO: 8/25/2020 ADD this user to friend
                                                                    //  and send notification for adding to this group

                                                                }
                                                            });

                                                            linearLayout.addView(v);

                                                            //download and show this user's profile pic
                                                            StorageReference storageRef = firebaseStorage.getReference();
                                                            StorageReference profilePictureRef = storageRef.child("user_profile_photos/" + searchedUserIdTemp + "_profile_photo.jpg");

                                                            profilePictureRef.getBytes(ONE_MEGABYTE)
                                                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                        @Override
                                                                        public void onSuccess(byte[] bytes) {
                                                                            // Data for "images/island.jpg" is returns, use this as needed
                                                                            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                                                            profilePictureIV.setImageDrawable(image);
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception exception) {
                                                                            Log.e(TAG, "onFailure: Error while downloading user profile pic.");
                                                                            exception.printStackTrace();
                                                                        }
                                                                    });

                                                        } else {
                                                            Log.e(TAG, "onComplete: task failed while getting displayName.");
                                                            Toast.makeText(CreateGroupActivity.this, "Internal error occurred. Try again.", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });


                                    } else {
                                        Toast.makeText(CreateGroupActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                    Toast.makeText(getApplicationContext(), "Error getting data. Check internet connection.", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Failed to get data from server.", Toast.LENGTH_SHORT).show();

                            }
                        });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        searchView = findViewById(R.id.idSearchUserNameSV_ACD);
        linearLayout = findViewById(R.id.idSearchedUserNameLL_ACG);
        friendCardInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}