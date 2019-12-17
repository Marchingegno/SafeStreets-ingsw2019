package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;

public class ImageRecognition {
	private static final String TAG = "ImageRecognition";

	static public void retrievePlateFromPhoto(Context context, Uri photoPath, ImageRecognitionUser caller) {
		FirebaseVisionImage image = null;
		try {
			image = FirebaseVisionImage.fromFilePath(context, photoPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
				.getOnDeviceTextRecognizer();

		detector.processImage(image)
				.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
					@Override
					public void onSuccess(FirebaseVisionText firebaseVisionText) {
						// Task completed successfully
						String resultText = firebaseVisionText.getText();
						String resultPlate = findFirstPlate(resultText);
						Log.d(TAG, "Image recognition result: " + resultPlate);
						caller.onTextRecognized(resultPlate);
					}
				})
				.addOnFailureListener(
						new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								// Task failed with an exception
								// ...
								Log.e(TAG, "Failed to find text", e);
								caller.onTextRecognized(null);
							}
						});
	}

	static private String findFirstPlate(String text) {
		boolean found = false;
		String[] lines;
		int i = 0;
		lines = text.split(System.getProperty("line.separator"));

		while (!found && i < lines.length) {
			lines[i] = lines[i].replace('-', ' ').replace(".", "").replace(" ", "");
			Log.d(TAG, lines[i]);
			found = lines[i].matches("[A-Z][A-Z][0-9][0-9][0-9][A-Z][A-Z](.)*");
			i++;
		}

		return found ? lines[i - 1].substring(0, 7) : null;
	}
}
