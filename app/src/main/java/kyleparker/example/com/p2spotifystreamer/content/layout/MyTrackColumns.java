package kyleparker.example.com.p2spotifystreamer.content.layout;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Table definition for the MyTrackColumns table
 *
 * Created by kyleparker on 8/4/2015.
 */
public interface MyTrackColumns extends BaseColumns {

    String TABLE_NAME = "track";
    Uri CONTENT_URI = Uri.parse(LayoutUtils.CONTENT_URI + TABLE_NAME);
    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.kyleparker" + TABLE_NAME;
    String CONTENT_ITEMTYPE = "vnd.android.cursor.item/vnd.kyleparker" + TABLE_NAME;

    // Columns
    String ALBUM_NAME = "album_name";
    String ARTIST_ID = "artist_id";
    String DURATION = "duration";
    String IMAGE_URL = "image_url";
    String PREVIEW_URL = "preview_url";
    String TRACK_ID = "track_id";
    String TRACK_NAME = "track_name";
    String TRACK_NUMBER = "track_number";

    String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TRACK_ID + " TEXT," +
                    TRACK_NAME + " TEXT," +
                    IMAGE_URL + " TEXT," +
                    TRACK_NUMBER + " TEXT," +
                    DURATION + " INTEGER," +
                    PREVIEW_URL + " TEXT," +
                    ALBUM_NAME + " TEXT," +
                    ARTIST_ID + " TEXT " +
                    ");";

    String DEFAULT_SORT_ORDER = TABLE_NAME + "." + _ID;
}
