package it.polimi.marcermarchiscianamotta.safestreets.util.cloud;

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

import it.polimi.marcermarchiscianamotta.safestreets.controller.AuthenticationManager;

/**
 * Handles the connection with the cloud storage.
 *
 * @author Desno365
 */
public class StorageConnection {

	/**
	 * Uploads the pictures to the cloud and returns their storage identifier.
	 *
	 * @param pictures          path of the pictures that need to be uploaded.
	 * @param listenerActivity  the activity that will listen for success or failure events.
	 * @param onSuccessListener the code to execute on success.
	 * @param onFailureListener the code to execute on failure.
	 * @return the storage identifier of the pictures uploaded.
	 */
	public static List<String> uploadPicturesToCloudStorage(List<Uri> pictures, Activity listenerActivity, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
		if (pictures == null || pictures.size() == 0)
			throw new RuntimeException("Trying to upload an array of pictures with 0 pictures.");

		ArrayList<String> picturesInUpload = new ArrayList<>();
		for (Uri uri : pictures) {
			// Name and path of the file.
			String pictureUuid = UUID.randomUUID().toString();
			StorageReference mImageRef = FirebaseStorage.getInstance().getReference("pictures/" + AuthenticationManager.getUserUid() + "/" + pictureUuid);

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
