package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.polimi.marcermarchiscianamotta.safestreets.util.Interfaces.SaveUser;

public class SavePictureTask extends AsyncTask<Void, Void, Uri> {
	private static final String TAG = "SavePictureTask";
	private static final int THUMBNAIL_SIZE = 1280;

	private Uri picturePathToCompress;
	private SaveUser caller;
	private File pathWhereToSave;
	private Context context;

	public SavePictureTask(Uri picturePathToCompress, File directory, SaveUser caller, Context context) {
		pathWhereToSave = new File(directory, picturePathToCompress.getLastPathSegment());
		this.caller = caller;
		this.picturePathToCompress = picturePathToCompress;
		this.context = context;
	}

	@Override
	protected Uri doInBackground(Void... params) {
		Log.d(TAG, "Compressing " + picturePathToCompress + " to " + pathWhereToSave);
		FileOutputStream fos = null;
		Bitmap bitmapImage = getThumbnail(picturePathToCompress);
		try {
			fos = new FileOutputStream(pathWhereToSave);
			bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Uri.parse(pathWhereToSave.toURI().toString());
	}

	public Bitmap getThumbnail(Uri uri) {
		InputStream input = null;
		try {
			input = context.getContentResolver().openInputStream(uri);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		onlyBoundsOptions.inDither = true;//optional
		onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
		BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		try {
			input.close();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

		if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
			return null;
		}

		int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

		double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		bitmapOptions.inDither = true; //optional
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
		try {
			input = context.getContentResolver().openInputStream(uri);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		try {
			input.close();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return bitmap;
	}

	private int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0) return 1;
		else return k;
	}

	@Override
	protected void onPreExecute() {
	}


	@Override
	protected void onPostExecute(Uri uri) {
		caller.onPictureSaved(uri);
	}
}