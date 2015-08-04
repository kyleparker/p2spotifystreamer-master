package kyleparker.example.com.p2spotifystreamer.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.content.ProviderUtils;
import kyleparker.example.com.p2spotifystreamer.object.MyArtist;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;
import kyleparker.example.com.p2spotifystreamer.service.MediaPlayerService;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistTrackListFragment;
import kyleparker.example.com.p2spotifystreamer.util.Constants;
import kyleparker.example.com.p2spotifystreamer.util.PrefUtils;

// DONE: When a track is selected, app uses an Intent to launch the Now playing screen and starts playback of the track.

/**
 * Activity for phones to display the top 10 tracks for an artist. Include {@link ArtistTrackListFragment} to display
 * the results.
 *
 * Created by kyleparker on 6/16/2015.
 */
public class ArtistTrackActivity extends BaseActivity implements ArtistTrackListFragment.Callbacks {

    private ProgressDialog mProgressDialog;

    private String mArtistId;
    private String mImageUrl;
    private String mArtistName;

    private ArrayList<MyTrack> mTracks;

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
            arguments.putString(Constants.EXTRA_ARTIST_NAME, mArtistName);
            arguments.putBoolean(Constants.EXTRA_IS_TABLET, mIsLargeLayout);
            ArtistTrackListFragment fragment = new ArtistTrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.now_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_now_playing:
                handleNowPlaying();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get extras from the intent bundle
     */
    private void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mArtistId = extras.getString(Constants.EXTRA_ARTIST_ID);
            mArtistName = extras.getString(Constants.EXTRA_ARTIST_NAME);
            mImageUrl = extras.getString(Constants.EXTRA_IMAGE_URL);
        }
    }

    /**
     * In a separate thread to avoid any disruption to the UI, load the "now playing" information for the currently selected artist
     * and track.
     */
    private void handleNowPlaying() {
        mProgressDialog = ProgressDialog.show(mActivity, null, mActivity.getString(R.string.content_loading));
        mProgressDialog.show();

        Runnable load = new Runnable() {
            public void run() {
                try {
                    ProviderUtils provider = ProviderUtils.Factory.get(mActivity);

                    // Retrieve the list of tracks to be used in the spinner - the user can associate the media with a specific path on the trip
                    mTracks = provider.getTracks();

                    if (!TextUtils.isEmpty(PrefUtils.getString(mActivity, R.string.selected_artist_id_key, ""))) {
                        MyArtist artist = provider.getArtist(PrefUtils.getString(mActivity, R.string.selected_artist_id_key, ""));
                        if (artist != null) {
                            mArtistName = artist.name;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    mActivity.runOnUiThread(handleNowPlayingRunnable);
                }
            }
        };

        Thread thread = new Thread(null, load, "handleNowPlaying");
        thread.start();
    }

    /**
     * On the main thread, start the intent for the Now Playing activity
     */
    private final Runnable handleNowPlayingRunnable = new Runnable() {
        public void run() {
            if (mTracks != null && !mTracks.isEmpty()) {
                Intent intent = new Intent(mActivity, PlayerActivity.class);
                intent.putParcelableArrayListExtra(Constants.EXTRA_TRACK, mTracks);
                intent.setAction(MediaPlayerService.ACTION_STOP);
                intent.putExtra(Constants.EXTRA_ARTIST_NAME, mArtistName);
                intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, PrefUtils.getInt(mActivity, R.string.selected_track_position_key, 0));

                startActivity(intent);
            } else {
                Toast.makeText(mActivity, R.string.toast_error_loading, Toast.LENGTH_LONG).show();
            }

            mProgressDialog.dismiss();
        }
    };

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
    public void onTrackSelected(String id, String artistName, ArrayList<MyTrack> tracks, int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setAction(MediaPlayerService.ACTION_STOP);
        intent.putExtra(Constants.EXTRA_TRACK, tracks);
        intent.putExtra(Constants.EXTRA_ARTIST_NAME, artistName);
        intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, position);
        startActivity(intent);
    }
}
