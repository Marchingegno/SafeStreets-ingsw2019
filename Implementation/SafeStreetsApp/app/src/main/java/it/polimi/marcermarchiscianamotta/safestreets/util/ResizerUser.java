package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.graphics.Bitmap;

public interface ResizerUser {

	void onBitmapResized(Bitmap resizedBitmap, int mMaxDimension);
}
