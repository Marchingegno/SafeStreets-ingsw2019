package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.app.Activity;
import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageConnection {

    public static List<String> uploadPicturesToCloudStorage(List<Uri> pictures, Activity listenerActivity, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        if(pictures.size() == 0)
            throw new RuntimeException("Trying to upload an array of pictures with 0 pictures.");

        ArrayList<String> picturesInUpload = new ArrayList<>();
        for (Uri uri : pictures) {
            // Name and path of the file.
            String uuid = UUID.randomUUID().toString();
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);

            // Asynchronously uploads the file.
            mImageRef.putFile(uri)
                    .addOnSuccessListener(listenerActivity, onSuccessListener)
                    .addOnFailureListener(listenerActivity, onFailureListener);

            // Save reference of the file currently uploading.
            picturesInUpload.add(mImageRef.getName());
        }
        return picturesInUpload;
    }
}
