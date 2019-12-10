package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.RetrieveViolationsManager;

public class SafeStreetsDataActivity extends AppCompatActivity {

    RetrieveViolationsManager retrieveViolationsManager = new RetrieveViolationsManager(this);

    //region Static methods
    //================================================================================
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, SafeStreetsDataActivity.class);
    }
    //endregion


    //region Overridden methods
    //================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_safestreets_data);
        ButterKnife.bind(this); // Needed for @BindView attributes.
    }
    //endregion
}
