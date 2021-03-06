package com.recipease.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ProfileActivity extends DrawerActivity {

    // TODO: 3/20/2018 Get user's bio, profile picture, and display name from database - DONE
    // TODO: 3/21/2018 Get recipes created and recipes favorited from database

    ArrayList<Long> recipesOwned;

    String displayName, bio;
    String userID;
    Uri imageURI;
    String imageURL;
    EditText etBio, etDisplayName;
    ImageView ivProfilePic;
    User current_user;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    boolean pictureModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.activity_profile, null, false);
        mDrawerLayout.addView(contentView, 0);

        etDisplayName = findViewById(R.id.etDisplayName);
        etBio = findViewById(R.id.etBio);
        ivProfilePic = findViewById(R.id.ivProfilePic);


        // Get current user's information
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userID = user.getUid();

        current_user = null;
        imageURI = null;

        pictureModified = false;

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        if (user.isAnonymous()) {

        }
        else {
            databaseReference.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot the_user) {
                    current_user = the_user.getValue(User.class);
                    if (current_user != null) {
                        displayName = current_user.getDisplayName();
                        bio = current_user.getBio();
                        imageURL = current_user.getImageURL();
                        if (current_user.getRecipesOwned() == null) {
                            recipesOwned = new ArrayList<Long>();
                        } else {
                            recipesOwned = new ArrayList(current_user.getRecipesOwned());
                        }
                        // Set GUI fields to current user's information
                        etDisplayName.setText(displayName);
                        etBio.setText(bio);
                        if (imageURL == "" || imageURL == null) {
                        }
                        else {
                            Glide.with(ProfileActivity.this).load(imageURL).centerCrop().into(ivProfilePic);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i(TAG, "onCancelled", databaseError.toException());
                }
            });
        }
    }

    private void showAlert(String messageToSay, String buttonText) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(messageToSay);
        alertDialogBuilder.setPositiveButton(buttonText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void saveChanges(View v) {
        if (user.isAnonymous()) {

        }
        else {
            if (current_user != null) {
                current_user.setDisplayName(etDisplayName.getText().toString());
                current_user.setBio(etBio.getText().toString());
                if (pictureModified == true) {
                    changeProfilePicture();
                } else {
                    databaseReference.child("users").child(userID).setValue(current_user);
                }
                Toast.makeText(this, "Profile changes saved", Toast.LENGTH_LONG).show();
                Intent goHome = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(goHome);
            }
        }

    }

    private void changeProfilePicture() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_pictures" + "/" + userID + "/" + imageURI.getLastPathSegment());
        UploadTask uploadTask = storageRef.putFile(imageURI);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                imageURL = downloadUrl.toString();
                current_user.setImageURL(imageURL);
                databaseReference.child("users").child(userID).setValue(current_user);
            }
        });

    }


    //Called when imageView is clicked
    private static final int READ_REQUEST_CODE = 42;
    public void selectProfilePicture(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Glide.with(this).load(uri).centerCrop().into(ivProfilePic);
                imageURI = uri;
                pictureModified = true;
            }
        }
    }

}