package kyleparker.example.com.p2spotifystreamer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.WindowManager;

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
    protected boolean mIsTablet;
    protected boolean mShouldBeFloatingWindow;

    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        mIsTablet = isDeviceTablet(mActivity);
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
     * Determine whether the device is a phone or a tablet based on the resource string
     *
     * @param context
     * @return
     */
    private boolean isDeviceTablet(Context context) {
        String current = context.getString(R.string.device_type);
        return current.equals(context.getString(R.string.device_type_tablet_key));
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

    /**
     * Floating window is enabled per the styles.xml in the sw600dp folder (floating window for tablets)
     */
    protected void setupFloatingWindow(int width, int height) {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(width);
        params.height = getResources().getDimensionPixelSize(height);
        params.alpha = 1;
        params.dimAmount = 0.80f;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    /**
     * Determine based on the style, whether the window should be floating or full screen
     * @return
     */
    protected boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();
        return !((theme == null) || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) &&
                (floatingWindowFlag.data != 0);
    }
}