package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.util.LoadPictureTask;
import it.polimi.marcermarchiscianamotta.safestreets.interfaces.LoadBitmapInterface;

/**
 * Displays a pictures and allows to delete it.
 *
 * @author Marcer
 */
public class PictureActivity extends AppCompatActivity implements LoadBitmapInterface {

	//Log tag
	private static final String TAG = "PictureActivity";

	//Constant
	private static final int PICTURE_DESIRED_SIZE = 680;

	//UI
	@BindView(R.id.delete_button)
	Button deleteButton;
	@BindView(R.id.return_button)
	Button returnButton;
	@BindView(R.id.picture_view)
	ImageView pictureImageView;

	//Other
	int mViewIndex;
	Uri mPicturePath;

	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);

		// Needed for @BindView attributes.
		ButterKnife.bind(this);

		mPicturePath = Uri.parse(getIntent().getStringExtra("Picture to display"));
		mViewIndex = Integer.parseInt(getIntent().getStringExtra("Index of the view associated with the picture"));

		loadAndDisplayPicture(mPicturePath);

		// Add back button to action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Once the picture has been loaded it is displayed.
	 *
	 * @param bitmap the loaded bitmap.
	 */
	@Override
	public void onPictureLoaded(Bitmap bitmap) {
		if (bitmap == null) {
			Log.e(TAG, "Couldn't load picture.");
			Toast.makeText(getApplicationContext(), "Couldn't load picture.", Toast.LENGTH_SHORT).show();
			return;
		}
		pictureImageView.setImageBitmap(bitmap);
	}
	//endregion

	//region Private methods
	//================================================================================
	//Calls a task to load the image from the specified uri.
	private void loadAndDisplayPicture(Uri uri) {
		Log.d(TAG, "Loading picture at: " + uri.toString());
		LoadPictureTask loadTask = new LoadPictureTask(this);
		loadTask.setMaxDimension(PICTURE_DESIRED_SIZE);
		loadTask.execute(uri);
	}
	//endregion

	//region UI methods
	//================================================================================
	@OnClick(R.id.delete_button)
	public void onClickDeleteButton(View v) {
		Log.d(TAG, "User selected picture #" + mViewIndex + " at " + mPicturePath + " to be deleted");

		Intent returnIntent = new Intent();
		returnIntent.putExtra("Want to delete", "true");
		returnIntent.putExtra("View index", String.valueOf(mViewIndex));
		returnIntent.putExtra("Picture path", mPicturePath.toString());
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}

	@OnClick(R.id.return_button)
	public void onClickReturnButton(View v) {
		Log.d(TAG, "User chose to return");

		Intent returnIntent = new Intent();
		returnIntent.putExtra("Want to delete", "false");
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}
	//endregion
}
