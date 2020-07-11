package com.familytracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int REQUEST_CODE_CHOOSE_PIC = 0;
    private static final int REQUEST_CODE_TAKE_PIC = 1;

    private Uri imagePathUri;

    private ImageView profilePictureIV;
    private ImageButton editProfilePictureIB;
    private EditText displayNmaeET;
    private EditText userNameET;
    private EditText mobNumberET;
    private Button editBtn;
    private Button saveBtn;



    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Initialise vars
        init();

        editProfilePictureIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtn.setEnabled(false);
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Upload or Take a photo");
                builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //upload image
                        Intent choosePictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        choosePictureIntent.setType("image/*");
                        startActivityForResult(choosePictureIntent, REQUEST_CODE_CHOOSE_PIC);

                        saveBtn.setEnabled(true);

                    }
                });
                builder.setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //take photo
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        try {
                            imagePathUri = getImageFileUri();
                        }catch (IOException e){
                            Log.e(TAG, "onClick: Exception: ", e);
                        }
                        if (imagePathUri == null) {
                            //display error
                            Toast.makeText(ProfileActivity.this, "Sorry there was an error!" +
                                    " Try again.", Toast.LENGTH_LONG).show();

                            saveBtn.setEnabled(false);

                        } else {
                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imagePathUri);
                            startActivityForResult(takePicture, REQUEST_CODE_TAKE_PIC);

                            saveBtn.setEnabled(true);

                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TAKE_PIC && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePictureIV.setImageBitmap(imageBitmap);
        }
    }

    private void init(){
        profilePictureIV = findViewById(R.id.idProfilePictureIV_AP);
        editProfilePictureIB = findViewById(R.id.idEditProfilePictureIB_AP);
        displayNmaeET = findViewById(R.id.idDisplayNameET_AP);
        userNameET = findViewById(R.id.idUserNameET_AP);
        mobNumberET =  findViewById(R.id.idMobileNumberET_AP);
        editBtn = findViewById(R.id.idEditBtn_AP);
        saveBtn =  findViewById(R.id.idSaveBtn_AP);
    }


    private Uri getImageFileUri() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                image);

        return photoURI;
    }
}
