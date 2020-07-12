package com.familytracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    public static final int TAKE_PIC_REQUEST_CODE = 0;
    public static final int CHOOSE_PIC_REQUEST_CODE = 1;
    private String profilePhotoFileName = "Profile_Photo.jpg";
    private File fileStorageDir;

    private Uri imagePathUri;

    private ImageView profilePictureIV;
    private ImageButton editProfilePictureIB;
    private ImageButton rotateProfilePictureIB;
    private EditText displayNmaeET;
    private EditText userNameET;
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
            }
        });

    }

    private void init() {
        profilePictureIV = findViewById(R.id.idProfilePictureIV_AP);
        editProfilePictureIB = findViewById(R.id.idEditProfilePictureIB_AP);
        rotateProfilePictureIB = findViewById(R.id.idRotateProfilePictureIB_AP);
        displayNmaeET = findViewById(R.id.idDisplayNameET_AP);
        userNameET = findViewById(R.id.idUserNameET_AP);
        mobNumberET = findViewById(R.id.idMobileNumberET_AP);
        editBtn = findViewById(R.id.idEditBtn_AP);
        saveBtn = findViewById(R.id.idSaveBtn_AP);

        defaultMode();

        fileStorageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File profilePhotoFile = new File(fileStorageDir + "/" + profilePhotoFileName);
        if (profilePhotoFile.exists()) {
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                    "com.example.android.fileprovider",
                    profilePhotoFile);

            Log.d(TAG, "init: photoUri: \n" + photoURI);
            try {
                profilePictureIV.setImageBitmap(getBitmap(photoURI));

            } catch (IOException e){
                //Control flow will probably never enter catch as file existence is checked in if statement.
                e.printStackTrace();
            }

        }
    }


    private void editMode() {
        editProfilePictureIB.setVisibility(View.VISIBLE);
        rotateProfilePictureIB.setVisibility(View.VISIBLE);
        displayNmaeET.setEnabled(true);
        userNameET.setEnabled(true);
        mobNumberET.setEnabled(true);
    }

    private void defaultMode() {
        editProfilePictureIB.setVisibility(View.INVISIBLE);
        rotateProfilePictureIB.setVisibility(View.INVISIBLE);
        displayNmaeET.setEnabled(false);
        userNameET.setEnabled(false);
        mobNumberET.setEnabled(false);
    }

    private void changeProfilePic(){
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

    private void rotateProfilePic(){
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

    private Bitmap getBitmap(Uri imageUri) throws IOException{
        int width, height;
        float scalingFactor;
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        if(width > height){
            scalingFactor = 900f / width;
        } else {
            scalingFactor = 900f / height;
        }
        width = (int) (width * scalingFactor);
        height = (int) (height * scalingFactor);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return bitmap;
    }

    private void saveProfilePictureToFile() throws IOException{
        // set storage path
        File toFile = new File(fileStorageDir + "/" + profilePhotoFileName);
        // read image from profilePictureIV ImageView
        Bitmap fromFileBitmap = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
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

//                        File toFile = new File(fileStorageDir + "/" + profilePhotoFileName);
//                        Bitmap fromFileBitmap = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
//
//                        if (!toFile.exists()) {
//                            toFile.createNewFile();
//                        }
//                        OutputStream out = new FileOutputStream(toFile);
//                        fromFileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                        out.flush();
//                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } else {
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                mediaScanIntent.setData(imagePathUri);
//                sendBroadcast(mediaScanIntent);
                Log.d(TAG, "onActivityResult: TAKE_PHOTO: \nimagePathUri : " + imagePathUri);
                // TODO: 7/11/2020 Why is the below line not setting the image I just took throught Take Picture option.
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
