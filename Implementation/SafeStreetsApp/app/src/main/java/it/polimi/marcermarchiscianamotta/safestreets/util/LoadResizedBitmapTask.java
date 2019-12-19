package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import it.polimi.marcermarchiscianamotta.safestreets.util.Interfaces.ResizeUser;

public class LoadResizedBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
	private static final String TAG = "LoadResizedBitmapTask";
	private int mMaxDimension;
	private Context mApplicationContext;
	private ResizeUser userToCallBack;

	public LoadResizedBitmapTask(int maxDimension, Context context, ResizeUser userToCallBack) {
		mMaxDimension = maxDimension;
		this.mApplicationContext = context;
		this.userToCallBack = userToCallBack;
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(Uri... params) {
		Uri uri = params[0];
		if (uri != null) {
			// Implement thumbnail + fullsize later.
			Bitmap bitmap = null;
			try {
				Log.d(TAG, "Start decoding");
				bitmap = decodeSampledBitmapFromUri(uri, mMaxDimension, mMaxDimension);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Can't find file to resize: " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "Error occurred during resize: " + e.getMessage());
			}
			Log.d(TAG, "Finished decoding");
			return bitmap;
		}
		return null;
	}

	public Bitmap decodeSampledBitmapFromUri(Uri fileUri, int reqWidth, int reqHeight)
			throws IOException {

		// First decode with inJustDecodeBounds=true to check dimensions
		InputStream stream = streamFromUri(fileUri);
		stream.mark(stream.available());
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, options);
		stream.reset();

		// Decode bitmap with inSampleSize set
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		BitmapFactory.decodeStream(stream, null, options);
		stream.close();

		InputStream freshStream = streamFromUri(fileUri);
		return BitmapFactory.decodeStream(freshStream, null, options);
	}

	private InputStream streamFromUri(Uri fileUri) throws FileNotFoundException {
		return new BufferedInputStream(
				mApplicationContext.getContentResolver().openInputStream(fileUri));
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		userToCallBack.onBitmapResized(bitmap, mMaxDimension);
	}
}

