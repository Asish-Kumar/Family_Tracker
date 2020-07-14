package com.familytracker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    public static final int TAKE_PIC_REQUEST_CODE = 0;
    public static final int CHOOSE_PIC_REQUEST_CODE = 1;
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 16;
    public static final String USER_NAMES_COLLECTION = "usernames";

    private String profilePhotoFileName = "Profile_Photo.jpg";
    private File fileStorageDir;
    private Uri imagePathUri;
    private String prevUserName;
    private String userId;
    private List<String> allUserNames;
    private boolean isDisplayNameVaild = false;
    private boolean isUserNameValid = false;

    private ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseStorage firebaseStorage;

    private ImageView profilePictureIV;
    private ImageButton editProfilePictureIB;
    private ImageButton rotateProfilePictureIB;
    private EditText displayNameET;
    private EditText userNameET;
    private ImageButton userNameValidityIB;
    private ImageButton displayNameValidityIB;
    private EditText mobNumberET;
    private Button editBtn;
    private Button saveBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Initialise vars
        init();

        editProfilePictureIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Change Pic Pressed", Toast.LENGTH_SHORT).show();
                changeProfilePic();
            }
        });//End change profile image onClick Listener

        rotateProfilePictureIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateProfilePic();
            }
        });

        displayNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = displayNameET.getText().toString();
                if (checkNameValidity(displayNameET, text)) {
                    displayNameValidityIB.setImageDrawable(getDrawable(R.drawable.ic_correct_circle));
                    isDisplayNameVaild = true;
                } else {
                    displayNameValidityIB.setImageDrawable(getDrawable(R.drawable.ic_wrong_circle));
                    isDisplayNameVaild = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        userNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                userNameValidityIB.setImageDrawable(getDrawable(R.drawable.ic_correct_circle));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = userNameET.getText().toString();
                if (checkNameValidity(userNameET, text)) {
                    if (allUserNames.contains(text) && !text.equals(prevUserName)) {
                        userNameValidityIB.setImageDrawable(getDrawable(R.drawable.ic_wrong_circle));
                        isUserNameValid = false;
                    } else {
                        userNameValidityIB.setImageDrawable(getDrawable(R.drawable.ic_correct_circle));
                        isUserNameValid = true;
                    }
                } else {
                    userNameValidityIB.setImageDrawable(getDrawable(R.drawable.ic_wrong_circle));
                    isUserNameValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Save Profile Picture to a file in local storage
                    saveProfilePictureToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                defaultMode();
                saveProfile();
            }
        });

    }

    private void init() {
        // execution order of following lines of code strictly matters
        profilePictureIV = findViewById(R.id.idProfilePictureIV_AP);
        editProfilePictureIB = findViewById(R.id.idEditProfilePictureIB_AP);
        rotateProfilePictureIB = findViewById(R.id.idRotateProfilePictureIB_AP);
        displayNameET = findViewById(R.id.idDisplayNameET_AP);
        userNameET = findViewById(R.id.idUserNameET_AP);
        userNameValidityIB = findViewById(R.id.idUserNameValidityIB_AP);
        displayNameValidityIB = findViewById(R.id.idDisplayNameValidityIB_AP);
        mobNumberET = findViewById(R.id.idMobileNumberET_AP);
        editBtn = findViewById(R.id.idEditBtn_AP);
        saveBtn = findViewById(R.id.idSaveBtn_AP);

        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getUid();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        fileStorageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        allUserNames = new ArrayList<>();
        downloadAllUserNames();
        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Getting data from server...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ProfileActivity.this.onBackPressed();
            }
        });
        progressDialog.show();

        displayUserProfile();
        defaultMode();

    }


    private void downloadAllUserNames() {

        db.collection(USER_NAMES_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: downloadAlluserNames task is successful.");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                allUserNames.add(document.getId());

                            }
                            progressDialog.dismiss();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(getApplicationContext(), "Error getting data.", Toast.LENGTH_SHORT).show();
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
    }

    private void displayUserProfile() {

        // Display User Profile Photo
        File profilePhotoFile = new File(fileStorageDir + "/" + profilePhotoFileName);
        if (profilePhotoFile.exists()) {
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                    "com.example.android.fileprovider",
                    profilePhotoFile);

            Log.d(TAG, "init: photoUri: \n" + photoURI);
            try {
                profilePictureIV.setImageBitmap(getBitmap(photoURI));

            } catch (IOException e) {
                //Control flow will probably never enter catch as file existence is checked in if statement.
                e.printStackTrace();
            }
        }

        // Display user display name and username
        db.collection(userId + "_collection").document("profile")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: task Result : " + task.getResult());
                            if (task.getResult().exists()) {
                                displayNameET.setText(task.getResult().get("displayname").toString());
                                userNameET.setText(task.getResult().get("username").toString());
                                prevUserName = userNameET.getText().toString();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

        // Display mobNumber by removing +91 from start
        mobNumberET.setText(firebaseAuth.getCurrentUser().getPhoneNumber().substring(3));
    }


    private void editMode() {
        editProfilePictureIB.setVisibility(View.VISIBLE);
        rotateProfilePictureIB.setVisibility(View.VISIBLE);
        displayNameET.setEnabled(true);
        userNameET.setEnabled(true);
    }

    private void defaultMode() {
        editProfilePictureIB.setVisibility(View.INVISIBLE);
        rotateProfilePictureIB.setVisibility(View.INVISIBLE);
        displayNameET.setEnabled(false);
        userNameET.setEnabled(false);
    }

    private void saveProfile() {
        final Bitmap profilePicBitmap = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
        final String displayName = displayNameET.getText().toString();
        final String userName = userNameET.getText().toString();
        final String phoneNumber = "+91" + mobNumberET.getText().toString();

        if ((isUserNameValid || userName.equals(prevUserName)) && isDisplayNameVaild) {

            if (!userName.equals(prevUserName)) {
                Log.d(TAG, "saveProfile: prevUserName : "+prevUserName);
                // update USER_NAMES_COLLECTION with new userName and delete any previous data for this same user
                updateUserNamesCollection(userName);
            }

            uploadProfilePhoto(profilePicBitmap);

            Map<String, Object> userProfileData = new HashMap<>();
            userProfileData.put("displayname", displayName);
            userProfileData.put("username", userName);
            userProfileData.put("phonenumber", phoneNumber);

            db.collection(userId + "_collection").document("profile")
                    .set(userProfileData, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "onFailure: saveProfile Error: ", e);
                        }
                    });
        }

    }

    private void updateUserNamesCollection(String newUserName) {
        // find and delete previousUserName document
        db.collection(USER_NAMES_COLLECTION).document(prevUserName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "onComplete: updateUserNamesCollection: Document exist.");
                                //delete this document
                                db.collection(USER_NAMES_COLLECTION).document(prevUserName)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error deleting document", e);
                                            }
                                        });


                            } else {
                                Log.d(TAG, "onComplete: updateUserNamesCollection: Document does not exist.");
                            }
                        } else {
                            Log.d(TAG, "Failed with: ", task.getException());
                        }
                    }
                });

        // add new userName document to usernames collection
        Map<String, Object> data = new HashMap<>();
        data.put("userid", userId);

        db.collection(USER_NAMES_COLLECTION).document(newUserName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: updateUserNamesCollection Error : " + e);
                    }
                });

    }


    private void uploadProfilePhoto(Bitmap profilePicBitmap) {
        // Upload the profile photo
        // get reference to file location on firebaseStorage (doesn't require the file to be present at the location at prior)
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference profilePictureRef = storageRef.child("user_profile_photos/" + userId + "_profile_photo.jpg");

        // convert the image Bitmap to Bytes Array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profilePicBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // start an upload task
        UploadTask uploadTask = profilePictureRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful upload
                exception.printStackTrace();
                Toast.makeText(getApplicationContext(), "Uploading profile picture failed! Retry.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.d(TAG, "onSuccess: TaskSnapshot: " + taskSnapshot);
                Toast.makeText(getApplicationContext(), "Uploaded profile picture.", Toast.LENGTH_SHORT).show();
            }
        }); // End upload profile photo

    }

    private boolean checkNameValidity(EditText editText, String name) {
        if (name.length() < NAME_MIN_LENGTH) {
            editText.setError("At least "+NAME_MIN_LENGTH+" characters required!");
            return false;
        }
        if (name.length() > NAME_MAX_LENGTH) {
            editText.setError("At most "+NAME_MAX_LENGTH+" characters allowed!");
            return false;
        }
        return true;
    }


    private void changeProfilePic() {
        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Upload or Take a photo");
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //upload image
                Intent choosePictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                choosePictureIntent.setType("image/*");
                startActivityForResult(choosePictureIntent, CHOOSE_PIC_REQUEST_CODE);

                saveBtn.setEnabled(true);

            }
        });
        builder.setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //take photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Log.d(TAG, "onClick: photoFile: \n" + photoFile);
                        ;
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                "com.example.android.fileprovider",
                                photoFile);
                        Log.d(TAG, "onClick: photoUri: \n" + photoURI);
                        imagePathUri = photoURI;
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TAKE_PIC_REQUEST_CODE);
                    }
                }
            }

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void rotateProfilePic() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        profilePictureIV.setImageBitmap(bitmap);
    }

    private File createImageFile() throws IOException {
        // Create an image file name;
        String imageFileName = "Profile_Photo.jpg";
        File image = new File(fileStorageDir + "/" + imageFileName);
        if (!image.exists()) {
            image.createNewFile();
        }
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private Bitmap getBitmap(Uri imageUri) throws IOException {
        int width, height;
        float scalingFactor;
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        if (width > height) {
            scalingFactor = 900f / width;
        } else {
            scalingFactor = 900f / height;
        }
        width = (int) (width * scalingFactor);
        height = (int) (height * scalingFactor);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return bitmap;
    }

    private void saveProfilePictureToFile() throws IOException {
        // set storage path
        File toFile = new File(fileStorageDir + "/" + profilePhotoFileName);
        Bitmap fromFileBitmap;
        try {
            // read image from profilePictureIV ImageView
             fromFileBitmap = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
        } catch (ClassCastException e){
            Log.d(TAG, "saveProfilePictureToFile: ClassCastException occurred but has been handled. Error: " + e);
            return;
        }
        // create the storage file
        if (!toFile.exists()) {
            toFile.createNewFile();
        }
        OutputStream out = new FileOutputStream(toFile);
        // write image data to file
        fromFileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        // memory cleanup
        out.flush();
        out.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PIC_REQUEST_CODE) {
                if (data == null) {
                    Toast.makeText(getApplicationContext(), "Image cannot be null!", Toast.LENGTH_LONG).show();
                } else {
                    imagePathUri = data.getData();
                    //set previews
                    Log.d(TAG, "onActivityResult: CHOOSE_PIC \nimagePathUri: " + imagePathUri);

                    try {
                        profilePictureIV.setImageBitmap(getBitmap(imagePathUri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } else {
                Log.d(TAG, "onActivityResult: TAKE_PHOTO: \nimagePathUri : " + imagePathUri);
                profilePictureIV.setImageURI(null);
                try {
                    profilePictureIV.setImageBitmap(getBitmap(imagePathUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_LONG).show();
        }
    }

}
