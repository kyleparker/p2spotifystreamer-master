package kyleparker.example.com.p2spotifystreamer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;
import kyleparker.example.com.p2spotifystreamer.util.AlbumArtCache;
import kyleparker.example.com.p2spotifystreamer.util.Constants;
import kyleparker.example.com.p2spotifystreamer.util.LogHelper;
import kyleparker.example.com.p2spotifystreamer.util.MediaNotificationManager;
import kyleparker.example.com.p2spotifystreamer.util.PrefUtils;

// DONE: Lock the notification during playback

/**
 * Service to handle background playback of audio files
 *
 * Modified source code from: https://github.com/googlesamples/android-MediaBrowserService
 */
public class MediaPlayerService extends Service implements Playback.Callback {
    private static final String TAG = LogHelper.makeLogTag(MediaPlayerService.class);

    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "kyleparker.example.com.p2spotifystreamer.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_SEEK = "action_seek";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PREPARED = "action_prepared";

    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    private ArrayList<MyTrack> mTracks;
    private String mArtistName;
    private int mPosition;
    // Indicates whether the service was started.
    private boolean mServiceStarted;

    private MyTrack mCurrentTrack = new MyTrack();

    private DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    private Playback mPlayback;
    private MediaNotificationManager mMediaNotificationManager = null;
    private MediaSession mMediaSession;
    private MediaController mMediaController;
    private LocalBroadcastManager mBroadcastManager;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.e("***> onBind", "here");
        mTracks = intent.getParcelableArrayListExtra(Constants.EXTRA_TRACK);
        mArtistName = intent.getStringExtra(Constants.EXTRA_ARTIST_NAME);
        mPosition = intent.getIntExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, 0);

        if (mTracks != null && !mTracks.isEmpty()) {
            mCurrentTrack = mTracks.get(mPosition);
        }

        mMediaSession = new MediaSession(getApplicationContext(), "MediaPlayerService");
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(mCallback);
        mMediaSession.setPlaybackState(new PlaybackState.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        mMediaController = new MediaController(getApplicationContext(), mMediaSession.getSessionToken());

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mPlayback = new Playback(this);
        mPlayback.setState(PlaybackState.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();

        mMediaNotificationManager = new MediaNotificationManager(this, this.getApplicationContext(), mTracks,
                mArtistName, mPosition);
        handlePlayRequest();
        updatePlaybackState(null);

        return mBinder;
    }

    /**
     * (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        handleStopRequest(null);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        // Always release the MediaSession to clean up resources
        // and notify associated MediaController(s).
        mMediaSession.release();
    }

    private MediaSession.Callback mCallback = new Callback() {
        @Override
        public void onPlay() {
//            LogHelper.e(TAG, "play");
            if (mTracks != null && !mTracks.isEmpty()) {
                handlePlayRequest();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
//            LogHelper.e(TAG, "pause. current state=" + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onSkipToNext() {
            mPosition++;
            if (mTracks != null && mPosition >= mTracks.size()) {
                mPosition = 0;
            }

            handlePlayRequest();
        }

        @Override
        public void onSkipToPrevious() {
            mPosition--;
            if (mTracks != null && mPosition < 0) {
                mPosition = 0;
            }

            handlePlayRequest();
        }

        @Override
        public void onFastForward() { }

        @Override
        public void onRewind() { }

        @Override
        public void onStop() {
//            LogHelper.e(TAG, "stop. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSeekTo(long position) {
//            LogHelper.e(TAG, "onSeekTo:", position);
            mPlayback.seekTo((int) position);
        }
    };

    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead and start the next.
        if (mTracks != null && !mTracks.isEmpty()) {
            // In this sample, we restart the playing queue when it gets to the end:
            mPosition++;
            if (mPosition >= mTracks.size()) {
                mPosition = 0;
            }

            handlePlayRequest();
        } else {
            // If there is nothing to play, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onPrepared() {
        sendResult(ACTION_PREPARED);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSession.Token getSessionToken() {
        return mMediaSession.getSessionToken();
    }

    /**
     * Handle the player control when the user taps on a track from the list
     *
     * @param action
     * @param tracks
     * @param artistName
     * @param position
     * @param seekPosition
     */
    public void handlePlayerControl(String action, ArrayList<MyTrack> tracks, String artistName, int position, int seekPosition) {
        mTracks = tracks;
        mArtistName = artistName;
        mPosition = position;
        mCurrentTrack = mTracks.get(position);

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mMediaController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mMediaController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mMediaController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mMediaController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mMediaController.getTransportControls().stop();
        } else if (action.equalsIgnoreCase(ACTION_SEEK)) {
            mMediaController.getTransportControls().seekTo(seekPosition);
        }
    }

    private MediaMetadata getCurrentPlayingMusic() {
        if (mCurrentTrack != null) {
//            LogHelper.e(TAG, "getCurrentPlayingMusic for musicId=", mCurrentTrack.id);
            return new MediaMetadata.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mArtistName)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mCurrentTrack.id)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mCurrentTrack.getAlbumName())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentTrack.name)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mCurrentTrack.duration_ms)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, mCurrentTrack.getImageUrl())
                    .build();
        }

        return null;
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;
        if (mTracks == null || mTracks.isEmpty()) {
            return actions;
        }
        if (mPlayback.isPlaying()) {
            actions |= PlaybackState.ACTION_PAUSE;
        }
        if (mPosition > 0) {
            actions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        }
        if (mPosition < mTracks.size() - 1) {
            actions |= PlaybackState.ACTION_SKIP_TO_NEXT;
        }
        return actions;
    }

    /**
     * Handle a request to play music
     */
    private void handlePlayRequest() {
        LogHelper.i("***> handlePlayRequest", "here");
        LogHelper.e(TAG, "handlePlayRequest: mState=" + mPlayback.getState());

        if (mPlayback.getState() != PlaybackState.STATE_NONE) {
            sendResult(ACTION_PLAY);
        }
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!mServiceStarted) {
            LogHelper.v(TAG, "Starting service");
            // The MusicService needs to keep running even after the calling MediaBrowser
            // is disconnected. Call startService(Intent) and then stopSelf(..) when we no longer
            // need to play media.
            startService(new Intent(getApplicationContext(), MediaPlayerService.class));
            mServiceStarted = true;
        }

        if (!mMediaSession.isActive()) {
            mMediaSession.setActive(true);
        }

        mCurrentTrack = mTracks.get(mPosition);
        LogHelper.e(TAG, "Set selected track:", mCurrentTrack.id + "|" + mCurrentTrack.name);
        PrefUtils.setString(getApplicationContext(), R.string.selected_track_id_key, mCurrentTrack.id);
        PrefUtils.setInt(getApplicationContext(), R.string.selected_track_position_key, mPosition);

        updateMetadata();
        mPlayback.play(mCurrentTrack);
    }

    /**
     * Handle a request to pause music
     */
    private void handlePauseRequest() {
//        LogHelper.e(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        sendResult(ACTION_PAUSE);
        mPlayback.pause();
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
    }

    /**
     * Handle a request to stop music
     */
    private void handleStopRequest(String withError) {
//        LogHelper.e(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        sendResult(ACTION_STOP);
        mPlayback.stop(true);
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        updatePlaybackState(withError);

        // service is no longer necessary. Will be started again if needed.
        stopSelf();
        mServiceStarted = false;
    }

    private void sendResult(String message) {
        Intent intent = new Intent(ACTION_CMD);
        if (message != null) {
            intent.putExtra(CMD_NAME, message);
            intent.putExtra(Constants.EXTRA_SELECTED_TRACK_POSITION, mPosition);
        }
        mBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    private void updatePlaybackState(String error) {
//        LogHelper.e(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackState.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());

        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackState.STATE_ERROR;
        }
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        if (mCurrentTrack != null && mCurrentTrack.id != null) {
            stateBuilder.setActiveQueueItemId(mCurrentTrack.id.hashCode());
        }

        mMediaSession.setPlaybackState(stateBuilder.build());
        boolean displayNotification = PrefUtils.getBoolean(this, R.string.settings_notification_display_key, true);

        if ((state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED ||
                state == PlaybackState.STATE_BUFFERING) && displayNotification) {
            mMediaNotificationManager.startNotification();
        }
    }

    private void updateMetadata() {
        mMediaSession.setMetadata(getCurrentPlayingMusic());

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (!TextUtils.isEmpty(mCurrentTrack.getImageUrl())) {
            AlbumArtCache.getInstance().fetch(mCurrentTrack.getImageUrl(), new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    MediaMetadata track = getCurrentPlayingMusic();
                    track = new MediaMetadata.Builder(track)
                            // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                            // example, on the lockscreen background when the media session is active.
                            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
                            // set small version of the album art in the DISPLAY_ICON. This is used on
                            // the MediaDescription and thus it should be small to be serialized if
                            // necessary..
                            .putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON, icon)
                            .build();

                    // If we are still playing the same music
                    String currentPlayingId = track.getDescription().getMediaId();
                    if (mCurrentTrack.id.equals(currentPlayingId)) {
                        mMediaSession.setMetadata(track);
                    }
                }
            });
        }
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MediaPlayerService> mWeakReference;

        private DelayedStopHandler(MediaPlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaPlayerService service = mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
//                    LogHelper.e(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
//                LogHelper.e(TAG, "Stopping service with delay handler.");
                service.stopSelf();
                service.mServiceStarted = false;
            }
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }
}