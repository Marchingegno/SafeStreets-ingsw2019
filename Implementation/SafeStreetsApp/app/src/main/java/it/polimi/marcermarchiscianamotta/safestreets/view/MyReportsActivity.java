package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;

public class MyReportsActivity extends AppCompatActivity {

	//region Static methods
	//================================================================================
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, MyReportsActivity.class);
	}
	//endregion


	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_my_reports);
		ButterKnife.bind(this); // Needed for @BindView attributes.
	}
	//endregion
}
