package kyleparker.example.com.p2spotifystreamer.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.service.MediaPlayerService;
import kyleparker.example.com.p2spotifystreamer.service.Playback;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.PlayerFragment;
import kyleparker.example.com.p2spotifystreamer.util.Constants;

// DONE: [Phone] UI contains a screen that represents the player. It contains  playback controls for the currently selected track
// DONE: If a user taps on another track while one is currently playing, playback is stopped on the currently playing
// track and the newly selected track (in other words, the tracks should not mix)
// DONE: App implements a notification with playback controls ( Play, pause , next & previous track )
// DONE: Notification controls appear on the lockscreen
// DONE: Notification displays track name and album art thumbnail
// DONE: App adds a menu for sharing the currently playing track
// DONE: App uses a shareIntent to expose the external Spotify URL for the current track
// DONE: App has a menu item to select the country code (which is automatically passed into the get Top Tracks query )
// DONE: App has menu item to toggle showing notification controls on the lock screen
// DONE: Create layout for phone landscape orientation
// TODO: Create deep link activity to handle opening a shared preview URL (assumes the person has the app installed)
/**
 * Spotify player activity.
 *
 * Created by kyleparker on 6/16/2015.
 */
public class PlayerActivity extends BaseActivity {

    private MediaPlayerService mMediaPlayerService;
    private Playback mPlayback;
    private boolean mBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        setupToolbar();

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setArguments(getIntent().getExtras());

            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(R.id.player_container, playerFragment).commit();

            // When a track is selected, the "stop" action will indicate that the service needs to
            // stop and start playing the new track.
            Intent intent = new Intent(mActivity, MediaPlayerService.class);
            if (getIntent().getAction() != null &&
                    getIntent().getAction().equalsIgnoreCase(MediaPlayerService.ACTION_STOP)) {
                try {
                    mBound = false;
                    getApplicationContext().stopService(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            intent.putExtras(getIntent().getExtras());
            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        } else {
            mBound = savedInstanceState.getBoolean(Constants.KEY_BIND_SERVICE);

            // Handle rotation - if the service is bound, rebind to enable playback controls
            if (mBound) {
                Intent intent = new Intent(mActivity, MediaPlayerService.class);
                intent.putExtras(getIntent().getExtras());
                getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(Constants.KEY_BIND_SERVICE, mBound);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaPlayerService getMediaPlayerService() {
        return mMediaPlayerService;
    }
}
