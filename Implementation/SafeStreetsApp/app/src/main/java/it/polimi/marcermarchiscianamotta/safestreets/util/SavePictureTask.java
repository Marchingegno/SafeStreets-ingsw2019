package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.SaveUser;

/**
 * Task that saves the specified bitmap to the specified uri.
 */
public class SavePictureTask extends AsyncTask<Bitmap, Void, Uri> {
	private static final String TAG = "SavePictureTask";

	//Quality of the compression
	private int quality = 100;
	private Uri pathWhereToSave;

	private SaveUser caller;

	public SavePictureTask(SaveUser caller) {
		this.caller = caller;
	}

	//region Task overridden methods
	//================================================================================

	/**
	 * Saves the bitmap passed as parameter.
	 *
	 * @param params the bitmap to save.
	 * @return the uri where the bitmap has been saved to.
	 */
	@Override
	protected Uri doInBackground(Bitmap... params) {
		Bitmap pictureToSave = params[0];
		File fileWhereToSave = new File(URI.create(pathWhereToSave.toString()));

		Log.d(TAG, "Saving to: " + pathWhereToSave);

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(fileWhereToSave);
			pictureToSave.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pathWhereToSave;
	}

	@Override
	protected void onPreExecute() {
	}

	/**
	 * Once saved the location where the picture has been saved is return.
	 * @param uri the uri where the picture has been saved to.
	 */
	@Override
	protected void onPostExecute(Uri uri) {
		caller.onPictureSaved(uri);
	}
	//endregion

	//region Public methods
	//================================================================================
	public void setQuality(int quality) {
		this.quality = quality;
	}

	public void setPathWhereToSave(Uri pathWhereToSave) {
		this.pathWhereToSave = pathWhereToSave;
	}
	//endregion
}