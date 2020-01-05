package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

import android.net.Uri;

/**
 * This interface must be implemented by the classes that wish to save a picture on the device's storage.
 */
public interface SavePictureInterface {
	/**
	 * This method is called when the picture has been successfully saved. The Uri where it has been saved is passed as parameter.
	 *
	 * @param savedPicturePath uri where the picture has been saved.
	 */
	void onPictureSaved(Uri savedPicturePath);
}
