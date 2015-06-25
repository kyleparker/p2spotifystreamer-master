package kyleparker.example.com.p2spotifystreamer.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;
import kyleparker.example.com.p2spotifystreamer.ui.BaseActivity;
import kyleparker.example.com.p2spotifystreamer.util.Adapters;
import kyleparker.example.com.p2spotifystreamer.util.Constants;
import kyleparker.example.com.p2spotifystreamer.util.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

// DONE: [Phone] UI contains a screen for displaying the top tracks for a selected artist
// DONE: Individual track layout contains - Album art thumbnail, track name, album name
// DONE: [Phone] UI places components in the same location and orientation as shown in the mockup
// DONE: Switch this to a listview in order to use the parallax scrolling with floating toolbar?
// DONE: App displays a list of top tracks
// DONE: App implements Artist Search + GetTopTracks API Requests (using spotify wrapper)
// DONE: Handle device rotation - when the list is partially scrolled and orientation changes, the header doesn't reset
// DONE: App stores the most recent top tracks query results and their respective metadata (track name, artist name, album name)
// locally in list. The queried results are retained on rotation.
// DONE: Display a message if no tracks found for artist
// DONE: Display a loading spinner during the data retrieval

/**
 * Fragment to display the top 10 tracks for a selected artist
 *
 * The fragment implements parallax scrolling with the artist image and toolbar. A library from
 * https://github.com/ksoichiro/Android-ObservableScrollView was used to implement this functionality.
 *
 * Created by kyleparker on 6/17/2015.
 */
public class ArtistTrackListFragment extends Fragment implements ObservableScrollViewCallbacks {
    private Activity mActivity;
    private View mRootView;

    private ArrayList<MyTrack> mTrackList;
    private Adapters.ArtistTrackAdapter mAdapter;

    private View mHeader;
    private View mHeaderBar;
    private View mListBackgroundView;
    private View mHeaderBackground;
    private ImageView mImage;
    private ProgressDialog mProgressDialog;

    private int mActionBarSize;
    private int mFlexibleSpaceImageHeight;
    private int mPrevScrollY;
    private boolean mGapIsChanging;
    private boolean mGapHidden;
    private boolean mReady;
    private boolean mIsTablet;
    private String mArtistId;
    private String mArtistName;
    private String mImageUrl;

    // The fragment's current callback object, which is notified of list item clicks.
    private Callbacks mCallbacks = sDummyCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_artist_track_list, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reloadFromArguments(getArguments());
        setupView();

        if (savedInstanceState != null) {
            mTrackList = savedInstanceState.getParcelableArrayList(Constants.KEY_TRACK_ARRAY);
            if (mTrackList != null && !mTrackList.isEmpty()) {
                mAdapter.addAll(mTrackList);
            }
        } else {
            getArtistTrackList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!mTrackList.isEmpty()) {
            outState.putParcelableArrayList(Constants.KEY_TRACK_ARRAY, mTrackList);
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateViews(scrollY, true);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    /**
     * As the user scrolls, animate the background header
     *
     * @param shouldShowGap
     * @param animated
     */
    private void changeHeaderBackgroundHeightAnimated(boolean shouldShowGap, boolean animated) {
        if (mGapIsChanging) {
            return;
        }
        final int heightOnGapShown = mHeaderBar.getHeight();
        final int heightOnGapHidden = mHeaderBar.getHeight() + mActionBarSize;
        final float from = mHeaderBackground.getLayoutParams().height;
        final float to;
        if (shouldShowGap) {
            if (!mGapHidden) {
                // Already shown
                return;
            }
            to = heightOnGapShown;
        } else {
            if (mGapHidden) {
                // Already hidden
                return;
            }
            to = heightOnGapHidden;
        }
        if (animated) {
            ViewPropertyAnimator.animate(mHeaderBackground).cancel();
            ValueAnimator a = ValueAnimator.ofFloat(from, to);
            a.setDuration(100);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    changeHeaderBackgroundHeight(height, to, heightOnGapHidden);
                }
            });
            a.start();
        } else {
            changeHeaderBackgroundHeight(to, to, heightOnGapHidden);
        }
    }

    /**
     * As the user scrolls, change the background header height
     *
     * @param height
     * @param to
     * @param heightOnGapHidden
     */
    private void changeHeaderBackgroundHeight(float height, float to, float heightOnGapHidden) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeaderBackground.getLayoutParams();
        lp.height = (int) height;
        lp.topMargin = (int) (mHeaderBar.getHeight() - height);
        mHeaderBackground.requestLayout();
        mGapIsChanging = (height != to);
        if (!mGapIsChanging) {
            mGapHidden = (height == heightOnGapHidden);
        }
    }

    /**
     * Create an {@link ObservableRecyclerView} to enable the parallax effect with the RecyclerView and the background
     * artist image
     *
     * @return
     */
    private ObservableRecyclerView createScrollable() {
        int artistsPerRow = mActivity.getResources().getInteger(R.integer.artist_tracks_per_row);

        ObservableRecyclerView recyclerView = (ObservableRecyclerView) mRootView.findViewById(R.id.artist_track_list);
        recyclerView.setScrollViewCallbacks(this);
        // Define the gridlayout for the RecyclerView - column count will change based on rotation and device type
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, artistsPerRow);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);

        View headerView = new View(mActivity);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mFlexibleSpaceImageHeight));
        headerView.setMinimumHeight(mFlexibleSpaceImageHeight);
        // This is required to disable header's list selector effect
        headerView.setClickable(false);

        mTrackList = new ArrayList<>();
        mAdapter = new Adapters.ArtistTrackAdapter(mActivity, mTrackList, headerView);
        recyclerView.setAdapter(mAdapter);

        return recyclerView;
    }

    /**
     * Get the height of the toolbar - to be used during the scrolling and parallax effect
     *
     * @return
     */
    private int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = mActivity.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    /**
     * Retrieve the Top 10 tracks for the artist, based on the artist selected on the previous screen
     *
     * TODO: Create a setting to allow the user to select the country - initially, default to US
     */
    private void getArtistTrackList() {
        if (Utils.isOnline(mActivity)) {
            mProgressDialog = ProgressDialog.show(mActivity, null, mActivity.getString(R.string.content_loading));
            mProgressDialog.show();

            // Spotify search using callback
            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();

            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, Constants.US_COUNTRY_ID);
            options.put(SpotifyService.OFFSET, 0);
            options.put(SpotifyService.LIMIT, 10);

            service.getArtistTopTrack(mArtistId, options, mSpotifyCallback);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.toast_error_not_online), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert the fragment arguments to intents for use during the activity lifecycle
     */
    private void reloadFromArguments(Bundle arguments) {
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            mArtistId = extras.getString(Constants.EXTRA_ARTIST_ID);
            mArtistName = extras.getString(Constants.EXTRA_TITLE);
            mImageUrl = extras.getString(Constants.EXTRA_IMAGE_URL);
            mIsTablet = extras.getBoolean(Constants.EXTRA_IS_TABLET);
        }
    }

    /**
     * Setup the various views for the fragment - including those needed for the parallax effect
     */
    private void setupView() {
        mActionBarSize = getActionBarSize();
        // Even when the top gap has began to change, header bar still can move within mIntersectionHeight.
        mFlexibleSpaceImageHeight = mIsTablet ? mActionBarSize :
                mActivity.getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);

        mImage = (ImageView) mRootView.findViewById(R.id.image);
        mHeader = mRootView.findViewById(R.id.header);
        mHeaderBar = mRootView.findViewById(R.id.header_bar);
        mHeaderBackground = mRootView.findViewById(R.id.header_background);
        mListBackgroundView = mRootView.findViewById(R.id.list_background);

        final ObservableRecyclerView scrollable = createScrollable();
        ScrollUtils.addOnGlobalLayoutListener(scrollable, new Runnable() {
            @Override
            public void run() {
                mReady = true;
                updateViews(scrollable.getCurrentScrollY(), false);
            }
        });

        if (mIsTablet) {
            // If the user is on a tablet, hide the artist image
            mImage.setVisibility(View.GONE);
            mHeaderBar.setBackgroundColor(mActivity.getResources().getColor(R.color.theme_accent_2));
            mHeaderBackground.setBackgroundColor(mActivity.getResources().getColor(R.color.theme_accent_2));
        } else {
            TextView subtitleView = (TextView) mRootView.findViewById(R.id.subtitle);
            subtitleView.setText(mArtistName);

            // Else if the user is on a phone, style the status bar, toolbar and background parallax image
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            mImage.setImageBitmap(bitmap);
                            Palette.Swatch darkSwatch = palette.getDarkVibrantSwatch() == null ?
                                    palette.getDarkMutedSwatch() : palette.getDarkVibrantSwatch();

                            if (darkSwatch != null) {
                                mHeaderBar.setBackgroundColor(darkSwatch.getRgb());
                                mHeaderBackground.setBackgroundColor(darkSwatch.getRgb());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = mActivity.getWindow();
                                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(darkSwatch.getRgb());
                                }
                            }
                        }
                    });
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            if (!TextUtils.isEmpty(mImageUrl)) {
                Picasso.with(mActivity)
                        .load(mImageUrl)
                        .resize(300, 300)
                        .centerCrop()
                        .into(target);
            } else {
                mImage.setImageResource(R.drawable.ic_placeholder_artist);
            }
        }
    }

    private void updateViews(int scrollY, boolean animated) {
        // If it's ListView, onScrollChanged is called before ListView is laid out (onGlobalLayout).
        // This causes weird animation when onRestoreInstanceState occurred,
        // so we check if it's laid out already.
        if (!mReady) {
            return;
        }

        // Translate image
        ViewHelper.setTranslationY(mImage, -scrollY / 2);

        // Translate header
        float headerTranslationY = ScrollUtils.getFloat(-scrollY + mFlexibleSpaceImageHeight - mHeaderBar.getHeight(), 0,
                Float.MAX_VALUE);
        ViewHelper.setTranslationY(mHeader, headerTranslationY);

        // Show/hide gap
        final int headerHeight = mHeaderBar.getHeight();
        boolean scrollUp = mPrevScrollY < scrollY;
        if (scrollUp) {
            if (mFlexibleSpaceImageHeight - headerHeight - mActionBarSize <= scrollY) {
                changeHeaderBackgroundHeightAnimated(false, animated);
            }
        } else {
            if (scrollY <= mFlexibleSpaceImageHeight - headerHeight - mActionBarSize) {
                changeHeaderBackgroundHeightAnimated(true, animated);
            }
        }

        mPrevScrollY = scrollY;

        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, ViewHelper.getTranslationY(mHeader));
    }

    /**
     * Callback for the Spotify searchArtist method. A successful search will load the adapter and display the results.
     */
    private retrofit.Callback<Tracks> mSpotifyCallback = new retrofit.Callback<Tracks>() {
        @Override
        public void success(Tracks result, Response response) {
            if (response.getStatus() == Constants.STATUS_OK) {
                if (result == null || result.tracks.isEmpty()) {
                    // In order display the toast must be run on the main UI thread
                    // The callback does not have access to the original view from this thread
                    showToast(R.string.toast_no_results);
                    return;
                }

                mTrackList = new ArrayList<>();
                List<Track> trackList = result.tracks;

                for (Track track : trackList) {
                    MyTrack myTrack = new MyTrack();
                    myTrack.album = new AlbumSimple();

                    myTrack.id = track.id;
                    myTrack.name = track.name;
                    if (track.album.images != null && track.album.images.size() > 0) {
                        myTrack.setImageUrl(track.album.images.get(0).url);
                    }
                    myTrack.album.name = track.album.name;

                    mTrackList.add(myTrack);
                }

                // In order to update the adapter and the recyclerview, the addAll method must be run on the main UI thread
                // The callback does not have access to the original view from this thread
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addAll(mTrackList);
                        mProgressDialog.dismiss();
                    }
                });
            } else {
                showToast(R.string.toast_error_results);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            showToast(R.string.toast_error_results);
        }

        public void showToast(final int resId) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                    Toast.makeText(mActivity, mActivity.getString(resId, mArtistName), Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    /**
     * A callback interface that all activities containing this fragment must implement. This mechanism allows activities
     * to be notified of item selections.
     */
    public interface Callbacks {
        // Callback for when an item has been selected.
        void onItemSelected(String id, String artistName, String imageUrl);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not
     * attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id, String artistName, String imageUrl) { }
    };
}
