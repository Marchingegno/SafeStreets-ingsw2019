package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.util.Interfaces.ImageRecognitionUser;

/**
 * Handles the image recognition.
 */
public class ImageRecognition {
	private static final String TAG = "ImageRecognition";

	/**
	 * Analyzes the specified image and retrieves the text in it. Once the procedure has terminate
	 * the caller is notified and the list of string is communicated.
	 *
	 * @param context   the context of the application.
	 * @param photoPath the photo's path of the photo to be analyzed.
	 * @param caller    the class that called the procedure and needs to be notified.
	 */
	static public void retrieveText(Context context, Uri photoPath, ImageRecognitionUser caller) {
		FirebaseVisionImage image = null;
		try {
			image = FirebaseVisionImage.fromFilePath(context, photoPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

		assert image != null;
		detector.processImage(image)
				.addOnSuccessListener(
						//On success
						firebaseVisionText -> {
							caller.onTextRecognized(firebaseVisionText.getText()
									.split(System.getProperty("line.separator")));
						})
				.addOnFailureListener(
						//On exception
						e -> {
							Log.e(TAG, "Failed to recognize text", e);
							caller.onTextRecognized(null);
						});
	}

	/**
	 * Analyzes the specified image and retrieves the license plates in it. Once the procedure
	 * has terminate the caller is notified and the list of licence plates is communicated.
	 *
	 * @param context   the context of the application.
	 * @param photoPath the photo's path of the photo to be analyzed.
	 * @param caller    the class that called the procedure and needs to be notified.
	 */
	static public void retrievePlateFromPhoto(Context context, Uri photoPath, ImageRecognitionUser caller) {
		FirebaseVisionImage image = null;
		try {
			image = FirebaseVisionImage.fromFilePath(context, photoPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

		assert image != null;
		detector.processImage(image)
				.addOnSuccessListener(
						//On success
						firebaseVisionText -> {
							String textFound = firebaseVisionText.getText();
							caller.onTextRecognized(findFirstPlate(textFound));
						})
				.addOnFailureListener(
						//On exception
						e -> {
							Log.e(TAG, "Failed to find text", e);
							caller.onTextRecognized(null);
						});
	}

	/**
	 * Searches in the text and returns all the strings that match a license plate.
	 *
	 * @param text the text where to search the plates.
	 * @return all the strings that match a license plate.
	 */
	static private String[] findFirstPlate(String text) {
		String[] lines = text.split(System.getProperty("line.separator"));
		List<String> licencePlatesFound = new ArrayList<>();

		Log.d(TAG, "Text found:\n" + text);

		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replace('-', ' ').replace(".", "").replace(" ", "");
			//If the current string matches a license plate it is added to the license plates found
			if (lines[i].matches("[A-Z][A-Z][0-9][0-9][0-9][A-Z][A-Z](.)*"))
				licencePlatesFound.add(lines[i].substring(0, 7));
		}

		Log.d(TAG, "Plates found: " + licencePlatesFound);

		return licencePlatesFound.toArray(new String[0]);
	}
}
