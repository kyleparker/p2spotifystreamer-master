package kyleparker.example.com.p2spotifystreamer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistListFragment;
import kyleparker.example.com.p2spotifystreamer.ui.fragment.ArtistTrackListFragment;
import kyleparker.example.com.p2spotifystreamer.util.Constants;

// TODO: Tablet UI uses a Master-Detail layout implemented using fragments. The left fragment is for
// searching artists and the right fragment is for displaying top tracks of a selected artist. The
// Now Playing controls are displayed in a DialogFragment.
// TODO: When a track is selected, app uses an Intent to launch the Now playing screen and starts playback of the track.
// DONE: Convert to fragments and provide a tablet layout
// TODO: Add activity transition animations
// DONE: Test on a tablet - structure should be in place, just need to test and tweak
// TODO: App displays a “Now Playing” Button in the ActionBar that serves to reopen the player UI should the user navigate back
// to browse content and then want to resume control over playback.
/**
 * Main activity for Project 1: Spotify Streamer app. This activity provides both phone and tablet layouts.
 *
 * Created by kyleparker on 6/15/2015.
 */
public class MainActivity extends BaseActivity implements
        ArtistListFragment.Callbacks,
        ArtistTrackListFragment.Callbacks {

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
            arguments.putBoolean(Constants.EXTRA_IS_TABLET, mIsTablet);
            ArtistListFragment fragment = new ArtistListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_search_container, fragment)
                    .commit();
        }
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(mActivity, R.string.toast_not_implemented, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup the toolbar and add the title for the activity
     */
    private void setupToolbar() {
        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setBackgroundColor(mActivity.getResources().getColor(R.color.theme_primary_dark));
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(mActivity.getString(R.string.app_name));
            }
        });
    }

    /**
     * Callback method from {@link ArtistListFragment.Callbacks} indicating that the item with the given
     * ID was selected.
     */
    @Override
    public void onItemSelected(String id, String artistName, String imageUrl) {
        if (mIsTablet) {
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail
            // fragment using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.EXTRA_ARTIST_ID, id);
            arguments.putString(Constants.EXTRA_IMAGE_URL, imageUrl);
            arguments.putString(Constants.EXTRA_TITLE, artistName);
            arguments.putBoolean(Constants.EXTRA_IS_TABLET, mIsTablet);
            ArtistTrackListFragment fragment = new ArtistTrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity for the selected item ID.
            Intent detailIntent = new Intent(this, ArtistTrackActivity.class);
            detailIntent.putExtra(Constants.EXTRA_ARTIST_ID, id);
            detailIntent.putExtra(Constants.EXTRA_IMAGE_URL, imageUrl);
            detailIntent.putExtra(Constants.EXTRA_TITLE, artistName);
            detailIntent.putExtra(Constants.EXTRA_IS_TABLET, mIsTablet);
            startActivity(detailIntent);
        }
    }
}
