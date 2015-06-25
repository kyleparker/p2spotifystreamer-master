package kyleparker.example.com.p2spotifystreamer.ui;

import android.os.Bundle;

import kyleparker.example.com.p2spotifystreamer.R;

// TODO: [Phone] UI contains a screen that represents the player. It contains  playback controls for the currently selected track
// TODO: App implements streaming playback of tracks
// TODO: User is able to advance to the previous track
// TODO: User is able to advance to the next track
// TODO: Play button starts/resumes playback of currently selected track
// TODO: Pause button pauses playback of currently selected track
// TODO: If a user taps on another track while one is currently playing, playback is stopped on the currently playing
// track and the newly selected track (in other words, the tracks should not mix)
// TODO: App implements a notification with playback controls ( Play, pause , next & previous track )
// TODO: Notification controls appear on the lockscreen
// TODO: Notification displays track name and album art thumbnail
// TODO: App adds a menu for sharing the currently playing track
// TODO: App uses a shareIntent to expose the external Spotify URL for the current track
// TODO: App has a menu item to select the country code (which is automatically passed into the get Top Tracks query )
// TODO: App has menu item to toggle showing notification controls on the lock screen
/**
 * Spotify player activity.
 *
 * Created by kyleparker on 6/16/2015.
 */
public class PlayerActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle bundle) {
        mShouldBeFloatingWindow = shouldBeFloatingWindow();
        if (mShouldBeFloatingWindow) {
            setupFloatingWindow(R.dimen.floating_window_width, R.dimen.floating_window_height);
        }

        super.onCreate(bundle);
        setContentView(R.layout.activity_player);
    }
}
