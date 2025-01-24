package com.firstapp.testdondaapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.firstapp.testdondaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Profile extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText editName, editEmail;
    private Button btnUploadPhoto, btnSave;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth, Storage, and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profile_pictures");
        databaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        btnUploadPhoto = view.findViewById(R.id.btn_upload_photo);
        btnSave = view.findViewById(R.id.btn_save);


        // Set up the user data if available
        if (currentUser != null) {
            editEmail.setText(currentUser.getEmail());

            // Load existing profile image if available
            databaseReference.child(currentUser.getUid()).child("profileImageUrl")
                    .get().addOnSuccessListener(dataSnapshot -> {
                        String imageUrl = dataSnapshot.getValue(String.class);
                        if (imageUrl != null) {
                            Picasso.get().load(imageUrl).into(profileImage);
                        }
                    });
        }

        // Set click listeners
        btnUploadPhoto.setOnClickListener(v -> openFileChooser());
        btnSave.setOnClickListener(v -> saveProfileChanges());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfileChanges() {
        String name = editName.getText().toString();

        if (name.isEmpty()) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("email", currentUser.getEmail());

            // Check if a new image is selected for upload
            if (imageUri != null) {
                StorageReference fileReference = storageReference.child(userId + ".jpg");
                fileReference.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save the image URL in Realtime Database under the user's profile
                            updates.put("profileImageUrl", uri.toString());
                            databaseReference.child(userId).updateChildren(updates)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to Update Profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }))
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Image Upload Failed", Toast.LENGTH_SHORT).show());
            } else {
                // If no new image is selected, just update the name and email
                databaseReference.child(userId).updateChildren(updates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Failed to Update Profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    //adding the profile information to Profile info linear layout


}
