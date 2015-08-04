package kyleparker.example.com.p2spotifystreamer.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;
import kyleparker.example.com.p2spotifystreamer.service.MediaPlayerService;
import kyleparker.example.com.p2spotifystreamer.service.Playback;
import kyleparker.example.com.p2spotifystreamer.ui.BaseActivity;
import kyleparker.example.com.p2spotifystreamer.ui.MainActivity;
import kyleparker.example.com.p2spotifystreamer.ui.PlayerActivity;
import kyleparker.example.com.p2spotifystreamer.util.Constants;
import kyleparker.example.com.p2spotifystreamer.util.LogHelper;
import kyleparker.example.com.p2spotifystreamer.util.Utils;

// DONE: App implements streaming playback of tracks
// DONE: User is able to advance to the previous track
// DONE: User is able to advance to the next track
// DONE: Play button starts/resumes playback of currently selected track
// DONE: Pause button pauses playback of currently selected track
/**
 * Player fragment used to stream the track.
 *
 * Created by kyleparker on 7/2/2015.
 */
public class PlayerFragment extends DialogFragment {
    private Activity mActivity;
    private View mRootView;

    private Handler mHandler = new Handler();

    private MediaPlayer mMediaPlayer;
    private ImageButton mButtonPlay;
    private ImageButton mButtonPrevious;
    private ImageButton mButtonNext;
    private SeekBar mSeekBar;
    private TextView mCurrentDuration;
    private TextView mTotalDuration;
    private TextView mArtistName;
    private TextView mTrackName;
    private TextView mAlbumName;
    private ImageView mAlbumCover;
    private ProgressBar mProgressBar;

    private ArrayList<MyTrack> mTracks;
    private int mPosition;
    private int mSeekBarPosition = 0;
    private String mArtist;
    private boolean mIsPlaying = false;
    private boolean mIsLargeLayout;

    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this.getActivity();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(MediaPlayerService.CMD_NAME);
                mPosition = intent.getIntExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, 0);

                displayArtistInfo(mPosition);

                LogHelper.e("***> playerFragment", action);
                if (action.equalsIgnoreCase(MediaPlayerService.ACTION_PAUSE)) {
                    mButtonPlay.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);

                    mButtonPlay.setImageResource(R.drawable.btn_play);
                } else if (action.equalsIgnoreCase(MediaPlayerService.ACTION_PLAY)) {
                    mIsPlaying = true;
                    mButtonPlay.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);

                    mButtonPlay.setImageResource(R.drawable.btn_pause);
                } else if (action.equalsIgnoreCase(MediaPlayerService.ACTION_PREPARED)) {
                    mIsPlaying = true;
                    mButtonPlay.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);

                    mButtonPlay.setImageResource(R.drawable.btn_pause);

                    updateProgressBar();
                    // set Progress bar values
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                }
            }
        };

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        if (!mIsLargeLayout) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_player, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reloadFromArguments(getArguments());

        if (mIsLargeLayout) {
            setupToolbar();
        }

        if (savedInstanceState != null) {
            mTracks = savedInstanceState.getParcelableArrayList(Constants.KEY_TRACK_ARRAY);
            mPosition = savedInstanceState.getInt(Constants.EXTRA_SELECTED_TRACK_POSITION);
            mArtist = savedInstanceState.getString(Constants.EXTRA_ARTIST_NAME);
            mSeekBarPosition = savedInstanceState.getInt(Constants.EXTRA_SEEK_POSITION);
            mIsPlaying = savedInstanceState.getBoolean(Constants.EXTRA_IS_PLAYING);
        }

        if (mTracks != null && !mTracks.isEmpty()) {
            setupView();
            displayArtistInfo(mPosition);

            // If the device is rotated and the seek position is greater than 0, update the bar
            if (mSeekBarPosition > 0) {
                updateProgressBar();
                // set Progress bar values
                mSeekBar.setProgress(0);
                mSeekBar.setMax(100);
            }

            // If the device is rotated, show the play button
            if (savedInstanceState != null) {
                mButtonPlay.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                if (mIsPlaying) {
                    mButtonPlay.setImageResource(R.drawable.btn_pause);
                }
            }

            if (mActivity instanceof MainActivity) {
                // If the service has been bound and the user selects a new track, start playing
                MediaPlayerService service = ((MainActivity) mActivity).getMediaPlayerService();

                if (service != null) {
                    service.handlePlayerControl(MediaPlayerService.ACTION_PLAY, mTracks, mArtist, mPosition, 0);
                }
            }
        } else {
            // There was an error passing the track, so show a message
            Toast.makeText(mActivity, R.string.toast_error_track, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTracks != null) {
            outState.putParcelableArrayList(Constants.KEY_TRACK_ARRAY, mTracks);
            outState.putInt(Constants.EXTRA_SELECTED_TRACK_POSITION, mPosition);
            outState.putString(Constants.EXTRA_ARTIST_NAME, mArtist);
            outState.putInt(Constants.EXTRA_SEEK_POSITION, mSeekBarPosition);
            outState.putBoolean(Constants.EXTRA_IS_PLAYING, mIsPlaying);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver((mReceiver),
                new IntentFilter(MediaPlayerService.ACTION_CMD)
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mReceiver);
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.track, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Uri uri = Utils.getLocalBitmapUri(mAlbumCover);
            String subject = mActivity.getString(R.string.content_share_subject, mTracks.get(mPosition).name,
                    mTracks.get(mPosition).getArtistName());
            String message = mActivity.getString(R.string.content_share_message, mTracks.get(mPosition).preview_url,
                    mTracks.get(mPosition).getArtistName(), mTracks.get(mPosition).getAlbumName(),
                    mTracks.get(mPosition).name);

            if (uri == null) {
                Utils.newShareIntent(mActivity, subject, message, Constants.MimeType.TEXT, null);
            } else {
                Utils.newShareIntent(mActivity, subject, message, Constants.MimeType.IMAGE, uri);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Display the artist and album information for the current track
     *
     * @param position
     */
    private void displayArtistInfo(int position) {
        mArtistName.setText((mPosition + 1) + ". " + mArtist);
        mAlbumName.setText(mTracks.get(position).getAlbumName());
        mTrackName.setText(mTracks.get(position).name);

        // TODO: Add transition animation when going from track to track?
        if (!TextUtils.isEmpty(mTracks.get(position).getImageUrl())) {
            Picasso.with(mActivity)
                    .load(mTracks.get(position).getImageUrl())
                    .into(mAlbumCover);
        } else {
            mAlbumCover.setImageResource(R.drawable.ic_spotify);
        }
    }

    /**
     * Function to change progress to timer
     *
     * @param progress -
     * @param totalDuration returns current duration in milliseconds
     */
    private int progressToTimer(int progress, int totalDuration) {
        int currentDuration;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    /**
     * Convert the fragment arguments to intents for use during the activity lifecycle
     */
    private void reloadFromArguments(Bundle arguments) {
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            mTracks = extras.getParcelableArrayList(Constants.EXTRA_TRACK);
            mArtist = extras.getString(Constants.EXTRA_ARTIST_NAME);
            mPosition = extras.getInt(Constants.EXTRA_SELECTED_TRACK_POSITION);
        }
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar_actionbar);
        toolbar.setBackgroundColor(mActivity.getResources().getColor(R.color.background_window));
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });
        toolbar.inflateMenu(R.menu.track);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_share:
                        Uri uri = Utils.getLocalBitmapUri(mAlbumCover);
                        String subject = mActivity.getString(R.string.content_share_subject, mTracks.get(mPosition).name,
                                mTracks.get(mPosition).getArtistName());
                        String message = mActivity.getString(R.string.content_share_message, mTracks.get(mPosition).preview_url,
                                mTracks.get(mPosition).getArtistName(), mTracks.get(mPosition).getAlbumName(),
                                mTracks.get(mPosition).name);

                        if (uri == null) {
                            Utils.newShareIntent(mActivity, subject, message, Constants.MimeType.TEXT, null);
                        } else {
                            Utils.newShareIntent(mActivity, subject, message, Constants.MimeType.IMAGE, uri);
                        }
                        return true;
                }

                return false;
            }
        });
    }

    /**
     * Set up the layout for the view
     */
    private void setupView() {
        mArtistName = (TextView) mRootView.findViewById(R.id.artist_name);
        mAlbumName = (TextView) mRootView.findViewById(R.id.album_name);
        mTrackName = (TextView) mRootView.findViewById(R.id.track_name);
        mAlbumCover = (ImageView) mRootView.findViewById(R.id.album_cover);

        mButtonPrevious = (ImageButton) mRootView.findViewById(R.id.button_previous);
        mButtonPrevious.setOnClickListener(mOnClickListener);

        mButtonPlay = (ImageButton) mRootView.findViewById(R.id.button_play_pause);
        mButtonPlay.setOnClickListener(mOnClickListener);
        mButtonPlay.setVisibility(View.GONE);

        mButtonNext = (ImageButton) mRootView.findViewById(R.id.button_next);
        mButtonNext.setOnClickListener(mOnClickListener);

        mSeekBar = (SeekBar) mRootView.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mCurrentDuration = (TextView) mRootView.findViewById(R.id.audio_current_duration);
        mTotalDuration = (TextView) mRootView.findViewById(R.id.audio_total_duration);

        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.loading_progress);
    }

    /**
     * Update timer on seekbar
     */
    private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }

    /**
     * Handle the listener events for the seekbar
     */
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int position, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // remove message Handler from updating progress bar
            mHandler.removeCallbacks(mUpdateTimeTask);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MediaPlayerService service = mActivity instanceof MainActivity ?
                    ((MainActivity) mActivity).getMediaPlayerService() :
                    ((PlayerActivity) mActivity).getMediaPlayerService();
            Playback playback = mActivity instanceof MainActivity ?
                    ((MainActivity) mActivity).getPlayback() :
                    ((PlayerActivity) mActivity).getPlayback();

            if (service == null || playback == null) {
                return;
            }

            mMediaPlayer = playback.getMediaPlayer();
            if (mMediaPlayer != null) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = mMediaPlayer.getDuration();
                mSeekBarPosition = progressToTimer(seekBar.getProgress(), totalDuration);

                // forward or backward to certain seconds
                service.handlePlayerControl(MediaPlayerService.ACTION_SEEK, mTracks, mArtist, mPosition, mSeekBarPosition + 1);

                // update timer progress again
                updateProgressBar();
            }
        }
    };

    /**
     * Handle the click event for the media buttons
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MediaPlayerService service = mActivity instanceof MainActivity ?
                    ((MainActivity) mActivity).getMediaPlayerService() :
                    ((PlayerActivity) mActivity).getMediaPlayerService();
            Playback playback = mActivity instanceof MainActivity ?
                    ((MainActivity) mActivity).getPlayback() :
                    ((PlayerActivity) mActivity).getPlayback();

            if (service == null || playback == null) {
                return;
            }

            int state = playback.getState();
            MediaPlayer mediaPlayer = playback.getMediaPlayer();

            if (view.getId() == mButtonPrevious.getId()) {
                mButtonPlay.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                service.handlePlayerControl(MediaPlayerService.ACTION_PREVIOUS, mTracks, mArtist, mPosition,
                        mediaPlayer.getCurrentPosition());

                mPosition--;
                if (mPosition < 0) {
                    mPosition = mTracks.size() - 1;
                }

                displayArtistInfo(mPosition);
                mIsPlaying = true;
            } else if (view.getId() == mButtonPlay.getId()) {
                if (state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_STOPPED ||
                        state == PlaybackState.STATE_NONE) {
                    service.handlePlayerControl(MediaPlayerService.ACTION_PLAY, mTracks, mArtist, mPosition,
                            mediaPlayer.getCurrentPosition());
                } else if (state == PlaybackState.STATE_PLAYING) {
                    service.handlePlayerControl(MediaPlayerService.ACTION_PAUSE, mTracks, mArtist, mPosition,
                            mediaPlayer.getCurrentPosition());
                    mIsPlaying = false;
                }
            } else if (view.getId() == mButtonNext.getId()) {
                mButtonPlay.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                service.handlePlayerControl(MediaPlayerService.ACTION_NEXT, mTracks, mArtist, mPosition,
                        mediaPlayer.getCurrentPosition());

                mPosition++;
                if (mPosition == mTracks.size()) {
                    mPosition = 0;
                }

                displayArtistInfo(mPosition);
                mIsPlaying = true;
            }
        }
    };

    /**
     * Background Runnable thread
     */
    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            Playback playback = mActivity instanceof MainActivity ?
                    ((MainActivity) mActivity).getPlayback() :
                    ((PlayerActivity) mActivity).getPlayback();

            if (playback == null) {
                return;
            }

            mMediaPlayer = playback.getMediaPlayer();

            if (mMediaPlayer != null) {
                long totalDuration = mMediaPlayer.getDuration();
                mSeekBarPosition = mMediaPlayer.getCurrentPosition();

                // Displaying Total Duration time
                mTotalDuration.setText(Utils.formatMilliseconds(totalDuration));
                // Displaying time completed playing
                mCurrentDuration.setText(Utils.formatMilliseconds(mSeekBarPosition));

                Double percentage;

                long currentSeconds = mSeekBarPosition / 1000;
                long totalSeconds = (int) (totalDuration / 1000);

                // calculating percentage
                percentage = (((double) currentSeconds) / totalSeconds) * 100;

                // Updating progress bar
                int progress = percentage.intValue();
                mSeekBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            }
        }
    };
}
