package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

import android.graphics.Bitmap;

/**
 * This interface must be implemented by the classes that wish to load a bitmap from the device's storage.
 */
public interface LoadBitmapInterface {

	/**
	 * This method is called when the bitmap has been successfully loaded. The result is passed as parameter.
	 *
	 * @param bitmap the loaded bitmap.
	 */
	void onPictureLoaded(Bitmap bitmap);
}
