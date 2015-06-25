package kyleparker.example.com.p2spotifystreamer.util;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import kyleparker.example.com.p2spotifystreamer.R;
import kyleparker.example.com.p2spotifystreamer.object.MyArtist;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;

/**
 * Adapter class for the RecyclerView
 * <p/>
 * Based on the code sample provided by Google - https://developer.android.com/samples/RecyclerView/index.html
 * Headers were added based on this blog post - http://blog.sqisland.com/2014/12/recyclerview-grid-with-header.html
 * Example on how to add click listener - https://github.com/VenomVendor/RecyclerView
 * <p/>
 * Created by kyleparker on 6/15/2015.
 */
public class Adapters {
    public static class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
        private Context context;
        private List<MyArtist> items;
        private OnItemClickListener itemClickListener;
        // Start with first item selected
        private int mSelectedItem = -1;

        public ArtistAdapter(Context context, List<MyArtist> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist, viewGroup, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (mSelectedItem > -1) {
                // Set selected state; use a state list drawable to style the view
                viewHolder.getCardView().setSelected(mSelectedItem == position);
            }

            // Subtract 1 for the header
            MyArtist item = items.get(position);

            if (item != null) {
                viewHolder.getArtistName().setText(item.name);
                if (!TextUtils.isEmpty(item.getImageUrl())) {
                    Picasso.with(context)
                            .load(item.getImageUrl())
                            .resize(300, 300)
                            .centerCrop()
                            .into(viewHolder.getArtistThumb(), getLoaderCallback(viewHolder.getProgress()));
                } else {
                    viewHolder.getArtistThumb().setImageResource(R.drawable.ic_placeholder_artist);
                    viewHolder.getProgress().setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public MyArtist getItem(int position) {
            return items.get(position);
        }

        public void setSelectedItem(int position) {
            mSelectedItem = position;
        }

        public void addAll(List<MyArtist> artists) {
            items.clear();
            items.addAll(artists);
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView artistName;
            private ImageView artistThumb;
            private ProgressBar progress;
            private CardView cardView;

            public ViewHolder(View base) {
                super(base);

                cardView = (CardView) base.findViewById(R.id.cardview);
                artistName = (TextView) base.findViewById(R.id.artist_name);
                artistThumb = (ImageView) base.findViewById(R.id.artist_thumb);
                progress = (ProgressBar) base.findViewById(R.id.progress);

                base.setOnClickListener(this);
            }

            public CardView getCardView() {
                return cardView;
            }

            public TextView getArtistName() {
                return artistName;
            }

            public ImageView getArtistThumb() {
                return artistThumb;
            }

            public ProgressBar getProgress() {
                return progress;
            }

            @Override
            public void onClick(View v) {
                // Redraw the old selection and the new
                notifyItemChanged(mSelectedItem);
//                mSelectedItem = getLayoutPosition();
//                notifyItemChanged(mSelectedItem);

                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, getPosition());
                }
            }
        }

        private Callback getLoaderCallback(final ProgressBar progressBar) {
            return new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    progressBar.setVisibility(View.GONE);
                }
            };
        }
    }

    public static class ArtistTrackAdapter extends RecyclerView.Adapter<ArtistTrackAdapter.ViewHolder> {
        private static final int ITEM_VIEW_TYPE_HEADER = 0;
        private static final int ITEM_VIEW_TYPE_ITEM = 1;

        private Context mContext;
        private List<MyTrack> mItems;
        private View mHeader;
//        private static OnItemClickListener itemClickListener;

        public ArtistTrackAdapter(Context context, List<MyTrack> items, View header) {
            mContext = context;
            mItems = items;
            mHeader = header;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == ITEM_VIEW_TYPE_HEADER) {
                return new ViewHolder(mHeader);
            }

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist_track, viewGroup, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (isHeader(position)) {
                return;
            }

            // Subtract 1 for the header
            MyTrack item = mItems.get(position - 1);

            if (item != null) {
                viewHolder.getAlbumName().setText(mContext.getString(R.string.content_track_position, position, item.album.name));
                viewHolder.getTrackName().setText(item.name);
                if (!TextUtils.isEmpty(item.getImageUrl())) {
                    Picasso.with(mContext)
                            .load(item.getImageUrl())
                            .resize(300, 300)
                            .centerCrop()
                            .into(viewHolder.getAlbumThumb(), getLoaderCallback(viewHolder.getProgress()));
                } else {
                    viewHolder.getAlbumThumb().setImageResource(R.drawable.ic_placeholder_artist);
                    viewHolder.getProgress().setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return (position == 0) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount () {
            // Add 1 to retrieve the total number of items, including the header
            return mItems.size() + 1;
        }

        public MyTrack getItem(int position) {
            return mItems.get(position + 1);
        }

        public boolean isHeader(int position) {
            return position == 0;
        }

        public void addAll(List<MyTrack> tracks) {
            mItems.clear();
            mItems.addAll(tracks);
            notifyDataSetChanged();
        }

//        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
//            ArtistTrackAdapter.itemClickListener = itemClickListener;
//        }

//        public interface OnItemClickListener {
//            void onItemClick(View view, int position);
//        }

        public class ViewHolder extends RecyclerView.ViewHolder { //implements View.OnClickListener {
            private TextView albumName;
            private TextView trackName;
            private ImageView albumThumb;
            private ProgressBar progress;

            public ViewHolder(View base) {
                super(base);

                albumName = (TextView) base.findViewById(R.id.album_name);
                trackName = (TextView) base.findViewById(R.id.track_name);
                albumThumb = (ImageView) base.findViewById(R.id.album_thumb);
                progress = (ProgressBar) base.findViewById(R.id.progress);

//                base.setOnClickListener(this);
            }

            public TextView getAlbumName() {
                return albumName;
            }

            public TextView getTrackName() {
                return trackName;
            }

            public ImageView getAlbumThumb() {
                return albumThumb;
            }

            public ProgressBar getProgress() {
                return progress;
            }

//            @Override
//            public void onClick(View v) {
//                if (itemClickListener != null) {
//                    itemClickListener.onItemClick(v, getPosition());
//                }
//            }
        }

        private Callback getLoaderCallback(final ProgressBar progressBar) {
            return new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    progressBar.setVisibility(View.GONE);
                }
            };
        }
    }
}
