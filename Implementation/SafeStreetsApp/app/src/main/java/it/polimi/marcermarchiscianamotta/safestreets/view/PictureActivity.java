package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;

public class PictureActivity extends AppCompatActivity {

	private static final String TAG = "PictureActivity";

	@BindView(R.id.delete_button)
	Button deleteButton;

	@BindView(R.id.return_button)
	Button returnButton;

	@BindView(R.id.picture_view)
	ImageView pictureImageView;

	int mViewIndex;
	Uri mPicturePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);

		// Needed for @BindView attributes.
		ButterKnife.bind(this);

		mPicturePath = Uri.parse(getIntent().getStringExtra("Picture to display"));
		mViewIndex = Integer.parseInt(getIntent().getStringExtra("Index of the view associated with the picture"));

		pictureImageView.setImageURI(mPicturePath);
	}

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
