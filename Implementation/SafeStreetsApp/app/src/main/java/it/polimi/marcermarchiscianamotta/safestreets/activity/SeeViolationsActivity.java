package it.polimi.marcermarchiscianamotta.safestreets.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;

public class SeeViolationsActivity extends AppCompatActivity {

    //region Static methods
    //================================================================================
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, SeeViolationsActivity.class);
    }
    //endregion


    //region Overridden methods
    //================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_see_violations);
        ButterKnife.bind(this); // Needed for @BindView attributes.
    }
    //endregion
}
