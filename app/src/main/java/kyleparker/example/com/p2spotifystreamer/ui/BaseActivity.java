package kyleparker.example.com.p2spotifystreamer.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import kyleparker.example.com.p2spotifystreamer.R;

/**
 * Base activity for all activities in the project. Extends {@link:AppCompatActivity}.
 * While this may not be needed at this point, due to the minimal amount of activities, this will be used
 * during the next stage of the Spotify Streamer project.
 *
 * Created by kyleparker on 6/15/2015.
 */
public class BaseActivity extends AppCompatActivity {

    protected AppCompatActivity mActivity;
    protected boolean mIsLargeLayout;

    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        intent.putExtras(arguments);
        return intent;
    }

    /**
     * Retrieve the base toolbar for the activity.
     *
     * @return toolbar
     */
    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }

        return mActionBarToolbar;
    }
}