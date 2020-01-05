package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

import it.polimi.marcermarchiscianamotta.safestreets.external.ExifUtil;
import it.polimi.marcermarchiscianamotta.safestreets.interfaces.LoadBitmapInterface;

/**
 * Loads a bitmap from the local storage and returns it.
 * @author Marcer
 */
public class LoadPictureTask extends AsyncTask<Uri, Void, Bitmap> {
	//Log tag
	private static final String TAG = "LoadPictureTask";

	//Max dimensions of the bitmap
	private int maxDimension = 1280;

	private Context context;
	private LoadBitmapInterface caller;

	//Constructor
	//================================================================================
	public LoadPictureTask(Context context) {
		this.context = context;
		this.caller = (LoadBitmapInterface) context;
	}
	//endregion

	//region Task overridden methods
	//================================================================================

	/**
	 * Loads the picture at the specified uri into a bitmap.
	 *
	 * @param params The uri of the picture to load.
	 * @return The bitmap generated.
	 */
	@Override
	protected Bitmap doInBackground(Uri... params) {
		Uri pathOfThePictureToLoad = params[0];
		Log.d(TAG, "Started loading picture at: " + pathOfThePictureToLoad);
		InputStream input = openInputStream(pathOfThePictureToLoad);

		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		closeInputStream(input);

		if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
			return null;
		}

		int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
		double ratio = (originalSize > maxDimension) ? (originalSize / maxDimension) : 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		input = openInputStream(pathOfThePictureToLoad);
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		Bitmap orientedBitmap = ExifUtil.rotateBitmap(pathOfThePictureToLoad.getPath(), bitmap);
		closeInputStream(input);
		return orientedBitmap;
	}

	/**
	 * Once finished to load the picture, the caller is called back.
	 *
	 * @param bitmap the bitmap loaded.
	 */
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		Log.d(TAG, "Finished loading");
		caller.onPictureLoaded(bitmap);
	}
	//endregion

	//region Public methods
	//================================================================================

	/**
	 * Sets the max dimension of the bitmap.
	 *
	 * @param maxDimension max dimension of the bitmap.
	 */
	public void setMaxDimension(int maxDimension) {
		this.maxDimension = maxDimension;
	}
	//endregion

	//region Private methods
	//================================================================================
	private InputStream openInputStream(Uri pathToOpen) {
		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(pathToOpen);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return inputStream;
	}

	private void closeInputStream(InputStream inputStream) {
		try {
			inputStream.close();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	private int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0) return 1;
		else return k;
	}
	//endregion
}

