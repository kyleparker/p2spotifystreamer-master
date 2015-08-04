package kyleparker.example.com.p2spotifystreamer.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import kyleparker.example.com.p2spotifystreamer.content.layout.MyArtistColumns;
import kyleparker.example.com.p2spotifystreamer.content.layout.MyTrackColumns;
import kyleparker.example.com.p2spotifystreamer.object.MyArtist;
import kyleparker.example.com.p2spotifystreamer.object.MyTrack;

/**
 * Data access for the content provider
 *
 * Created by kyleparker on 8/4/2015.
 */
public class ProviderUtils {
    private final ContentResolver contentResolver;

    public ProviderUtils(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void deleteArtists() {
        contentResolver.delete(MyArtistColumns.CONTENT_URI, null, null);
    }

    public void deleteTracks() {
        contentResolver.delete(MyTrackColumns.CONTENT_URI, null, null);
    }

    public MyArtist getArtist(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }

        Uri uri = Uri.parse(MyArtistColumns.CONTENT_URI + "/" + Uri.encode(id));

        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createArtist(cursor);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public ArrayList<MyArtist> getArtists() {
        ArrayList<MyArtist> artists = new ArrayList<>();

        Cursor cursor = contentResolver.query(MyArtistColumns.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            artists.ensureCapacity(cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    artists.add(createArtist(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return artists;
    }

    public MyTrack getTrack(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }

        Uri uri = Uri.parse(MyTrackColumns.CONTENT_URI + "/" + Uri.encode(id));

        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createTrack(cursor);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public ArrayList<MyTrack> getTracks() {
        ArrayList<MyTrack> tracks = new ArrayList<>();

        Cursor cursor = contentResolver.query(MyTrackColumns.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            tracks.ensureCapacity(cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    tracks.add(createTrack(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return tracks;
    }

    public Uri insertArtist(MyArtist obj) {
        return contentResolver.insert(MyArtistColumns.CONTENT_URI, createContentValues(obj));
    }

    public Uri insertTrack(MyTrack obj) {
        return contentResolver.insert(MyTrackColumns.CONTENT_URI, createContentValues(obj));
    }

    public void updateArtist(MyArtist obj) {
        contentResolver.update(MyArtistColumns.CONTENT_URI, createContentValues(obj),
                MyArtistColumns.ARTIST_ID + "='" + obj.id + "'", null);
    }

    public void updateTrack(MyTrack obj) {
        contentResolver.update(MyTrackColumns.CONTENT_URI, createContentValues(obj),
                MyTrackColumns.TRACK_ID + "='" + obj.id + "'", null);
    }

    /**
     * Create ContentValues for the MyArtist object
     *
     * @param obj
     * @return
     */
    private ContentValues createContentValues(MyArtist obj) {
        ContentValues values = new ContentValues();

        values.put(MyArtistColumns.ARTIST_ID, obj.id);
        values.put(MyArtistColumns.ARTIST_NAME, obj.name);
        values.put(MyArtistColumns.IMAGE_URL, obj.getImageUrl());

        return values;
    }

    /**
     * Create ContentValues for the MyTrack object
     *
     * @param obj
     * @return
     */
    private ContentValues createContentValues(MyTrack obj) {
        ContentValues values = new ContentValues();

        values.put(MyTrackColumns.ALBUM_NAME, obj.getAlbumName());
        values.put(MyTrackColumns.ARTIST_ID, obj.getArtistId());
        values.put(MyTrackColumns.DURATION, obj.duration_ms);
        values.put(MyTrackColumns.IMAGE_URL, obj.getImageUrl());
        values.put(MyTrackColumns.PREVIEW_URL, obj.preview_url);
        values.put(MyTrackColumns.TRACK_ID, obj.id);
        values.put(MyTrackColumns.TRACK_NAME, obj.name);
        values.put(MyTrackColumns.TRACK_NUMBER, obj.track_number);

        return values;
    }

    /**
     * Create MyArtist object from database values
     *
     * @param cursor
     * @return
     */
    private MyArtist createArtist(Cursor cursor) {
        int idxArtistId = cursor.getColumnIndex(MyArtistColumns.ARTIST_ID);
        int idxArtistName = cursor.getColumnIndex(MyArtistColumns.ARTIST_NAME);
        int idxImageUrl = cursor.getColumnIndex(MyArtistColumns.IMAGE_URL);

        MyArtist artist = new MyArtist();

        if (idxArtistId > -1) {
            artist.id = cursor.getString(idxArtistId);
        }
        if (idxArtistName > -1) {
            artist.name = cursor.getString(idxArtistName);
        }
        if (idxImageUrl > -1) {
            artist.setImageUrl(cursor.getString(idxImageUrl));
        }

        return artist;
    }

    /**
     *
     * @param cursor
     * @return
     */
    private MyTrack createTrack(Cursor cursor) {
        int idxAlbumName = cursor.getColumnIndex(MyTrackColumns.ALBUM_NAME);
        int idxArtistId = cursor.getColumnIndex(MyTrackColumns.ARTIST_ID);
        int idxDuration = cursor.getColumnIndex(MyTrackColumns.DURATION);
        int idxImageUrl = cursor.getColumnIndex(MyTrackColumns.IMAGE_URL);
        int idxPreviewUrl = cursor.getColumnIndex(MyTrackColumns.PREVIEW_URL);
        int idxTrackId = cursor.getColumnIndex(MyTrackColumns.TRACK_ID);
        int idxTrackName = cursor.getColumnIndex(MyTrackColumns.TRACK_NAME);
        int idxTrackNumber = cursor.getColumnIndex(MyTrackColumns.TRACK_NUMBER);

        MyTrack track = new MyTrack();

        if (idxAlbumName > -1) {
            track.setAlbumName(cursor.getString(idxAlbumName));
        }
        if (idxArtistId > -1) {
            track.setArtistId(cursor.getString(idxArtistId));
        }
        if (idxDuration > -1) {
            track.duration_ms = cursor.getInt(idxDuration);
        }
        if (idxImageUrl > -1) {
            track.setImageUrl(cursor.getString(idxImageUrl));
        }
        if (idxPreviewUrl > -1) {
            track.preview_url = cursor.getString(idxPreviewUrl);
        }
        if (idxTrackId > -1) {
            track.id = cursor.getString(idxTrackId);
        }
        if (idxTrackName > -1) {
            track.name = cursor.getString(idxTrackName);
        }
        if (idxTrackNumber > -1) {
            track.track_number = cursor.getInt(idxTrackNumber);
        }

        track.artist = createArtist(cursor);

        return track;
    }

    /**
     * A factory which can produce instances of {@link ProviderUtils}
     */
    public static class Factory {
        private static Factory instance = new Factory();

        /**
         * Creates and returns an instance of {@link ProviderUtils} which uses the given context to access its data.
         */
        public static ProviderUtils get(Context context) {
            return instance.newForContext(context);
        }

        /**
         * Creates an instance of {@link ProviderUtils}.
         */
        protected ProviderUtils newForContext(Context context) {
            return new ProviderUtils(context.getContentResolver());
        }
    }
}
