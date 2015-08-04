package kyleparker.example.com.p2spotifystreamer.ui;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.UUID;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.content.ProviderUtils;
import kyleparker.example.com.p2spotifystreamer.object.MyArtist;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;
import kyleparker.example.com.p2spotifystreamer.service.MediaPlayerService;
import kyleparker.example.com.p2spotifystreamer.service.Playback;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistListFragment;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistTrackListFragment;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.PlayerFragment;
import kyleparker.example.com.p2spotifystreamer.util.Constants;
import kyleparker.example.com.p2spotifystreamer.util.PrefUtils;

// DONE: Tablet UI uses a Master-Detail layout implemented using fragments. The left fragment is for
// searching artists and the right fragment is for displaying top tracks of a selected artist. The
// Now Playing controls are displayed in a DialogFragment.
// DONE: When a track is selected, app uses an Intent to launch the Now playing screen and starts playback of the track.
// DONE: Convert to fragments and provide a tablet layout
// DONE: Add activity transition animations
// DONE: Test on a tablet - structure should be in place, just need to test and tweak
// DONE: App displays a “Now Playing” Button in the ActionBar that serves to reopen the player UI should the user navigate back
// to browse content and then want to resume control over playback.
/**
 * Main activity for Project 1: Spotify Streamer app. This activity provides both phone and tablet layouts.
 *
 * Created by kyleparker on 6/15/2015.
 */
public class MainActivity extends BaseActivity implements
        ArtistListFragment.Callbacks,
        ArtistTrackListFragment.Callbacks {

    private MediaPlayerService mMediaPlayerService;
    private Playback mPlayback;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;

    private ArrayList<MyTrack> mTracks;
    private int mPosition;
    private String mArtist;
    private String mImageUrl;
    private boolean mBound = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            arguments.putBoolean(Constants.EXTRA_IS_TABLET, mIsLargeLayout);
            ArtistListFragment fragment = new ArtistListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_search_container, fragment)
                    .commit();
        } else {
            mBound = savedInstanceState.getBoolean(Constants.KEY_BIND_SERVICE);

            if (mBound) {
                Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                intent.putParcelableArrayListExtra(Constants.EXTRA_TRACK, mTracks);
                intent.putExtra(Constants.EXTRA_ARTIST_NAME, mArtist);
                intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, mPosition);
                getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound && mActivity.isFinishing()) {
            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(Constants.KEY_BIND_SERVICE, mBound);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
                return true;
            case R.id.action_now_playing:
                handleNowPlaying();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                            mArtist = artist.name;
                            mImageUrl = artist.getImageUrl();
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
                if (mIsLargeLayout) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    PlayerFragment playerFragment = new PlayerFragment();

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Constants.EXTRA_TRACK, mTracks);
                    bundle.putString(Constants.EXTRA_ARTIST_NAME, mArtist);
                    bundle.putInt(Constants.EXTRA_SELECTED_TRACK_POSITION,
                            PrefUtils.getInt(mActivity, R.string.selected_track_position_key, 0));

                    playerFragment.setArguments(bundle);

                    // The device is using a large layout, so show the fragment as a dialog
                    playerFragment.show(fragmentManager, "dialog");

                    Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                    intent.putParcelableArrayListExtra(Constants.EXTRA_TRACK, mTracks);
                    intent.putExtra(Constants.EXTRA_ARTIST_NAME, mArtist);
                    intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION,
                            PrefUtils.getInt(mActivity, R.string.selected_track_position_key, 0));
                    getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                } else {
                    Intent intent = new Intent(mActivity, PlayerActivity.class);
                    intent.putParcelableArrayListExtra(Constants.EXTRA_TRACK, mTracks);
                    intent.putExtra(Constants.EXTRA_ARTIST_NAME, mArtist);
                    intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION,
                            PrefUtils.getInt(mActivity, R.string.selected_track_position_key, 0));

                    startActivity(intent);
                }
            } else {
                Toast.makeText(mActivity, R.string.toast_error_loading, Toast.LENGTH_LONG).show();
            }

            mProgressDialog.dismiss();
        }
    };

    /**
     * Setup the toolbar and add the title for the activity
     */
    private void setupToolbar() {
        mToolbar = getActionBarToolbar();
        mToolbar.setBackgroundColor(mActivity.getResources().getColor(R.color.theme_primary_dark));
        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                mToolbar.setTitle(mActivity.getString(R.string.app_name));
            }
        });
    }

    /**
     * Callback method from {@link ArtistListFragment.Callbacks} indicating that the item with the given
     * ID was selected.
     */
    @Override
    public void onArtistSelected(String id, String artistName, String imageUrl) {
        if (mIsLargeLayout) {
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail
            // fragment using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.EXTRA_ARTIST_ID, id);
            arguments.putString(Constants.EXTRA_IMAGE_URL, imageUrl);
            arguments.putString(Constants.EXTRA_ARTIST_NAME, artistName);
            arguments.putBoolean(Constants.EXTRA_IS_TABLET, mIsLargeLayout);
            ArtistTrackListFragment fragment = new ArtistTrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity for the selected item ID.
            Intent intent = new Intent(this, ArtistTrackActivity.class);
            intent.putExtra(Constants.EXTRA_ARTIST_ID, id);
            intent.putExtra(Constants.EXTRA_IMAGE_URL, imageUrl);
            intent.putExtra(Constants.EXTRA_ARTIST_NAME, artistName);
            startActivity(intent);
        }
    }

    @Override
    public void onTrackSelected(String id, String artistName, ArrayList<MyTrack> tracks, int position) {
        mTracks = tracks;
        mArtist = artistName;
        mPosition = position;

        FragmentManager fragmentManager = getSupportFragmentManager();
        PlayerFragment playerFragment = new PlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.EXTRA_TRACK, mTracks);
        bundle.putString(Constants.EXTRA_ARTIST_NAME, mArtist);
        bundle.putInt(Constants.EXTRA_SELECTED_TRACK_POSITION, mPosition);

        playerFragment.setArguments(bundle);

        // The device is using a large layout, so show the fragment as a dialog
        playerFragment.show(fragmentManager, "dialog");

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.putParcelableArrayListExtra(Constants.EXTRA_TRACK, mTracks);
        intent.putExtra(Constants.EXTRA_ARTIST_NAME, mArtist);
        intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, mPosition);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mMediaPlayerService = binder.getService();
            mBound = true;

            mPlayback = mMediaPlayerService.getPlayback();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaPlayerService getMediaPlayerService() {
        return mMediaPlayerService;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
