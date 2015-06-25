package kyleparker.example.com.p2spotifystreamer.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistListFragment;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistTrackListFragment;
import kyleparker.example.com.p2spotifystreamer.util.Constants;

// TODO: When a track is selected, app uses an Intent to launch the Now playing screen and starts playback of the track.

/**
 * Activity for phones to display the top 10 tracks for an artist. Include {@link ArtistTrackListFragment} to display
 * the results.
 *
 * Created by kyleparker on 6/16/2015.
 */
public class ArtistTrackActivity extends BaseActivity implements ArtistTrackListFragment.Callbacks {

    private String mArtistId;
    private String mImageUrl;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_track);

        getExtras();
        setupToolbar();

        // savedInstanceState is non-null when there is fragment state saved from previous configurations of
        // this activity (e.g. when rotating the screen from portrait to landscape). In this case, the fragment
        // will automatically be re-added to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.EXTRA_ARTIST_ID, mArtistId);
            arguments.putString(Constants.EXTRA_IMAGE_URL, mImageUrl);
            arguments.putString(Constants.EXTRA_TITLE, mTitle);
            arguments.putBoolean(Constants.EXTRA_IS_TABLET, mIsTablet);
            ArtistTrackListFragment fragment = new ArtistTrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    /**
     * Get extras from the intent bundle
     */
    private void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mArtistId = extras.getString(Constants.EXTRA_ARTIST_ID);
            mTitle = extras.getString(Constants.EXTRA_TITLE);
            mImageUrl = extras.getString(Constants.EXTRA_IMAGE_URL);
        }
    }


    /**
     * Setup the toolbar for the activity.
     */
    private void setupToolbar() {
        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });
    }

    @Override
    public void onItemSelected(String id, String artistName, String imageUrl) {

    }
}
