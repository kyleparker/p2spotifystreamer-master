package kyleparker.example.com.p2spotifystreamer.content.layout;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Table definition for the MyArtistColumns table
 *
 * Created by kyleparker on 8/4/2015.
 */
public interface MyArtistColumns extends BaseColumns {

    String TABLE_NAME = "artist";
    Uri CONTENT_URI = Uri.parse(LayoutUtils.CONTENT_URI + TABLE_NAME);
    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.kyleparker" + TABLE_NAME;
    String CONTENT_ITEMTYPE = "vnd.android.cursor.item/vnd.kyleparker" + TABLE_NAME;

    // Columns
    String ARTIST_ID = "artist_id";
    String ARTIST_NAME = "artist_name";
    String IMAGE_URL = "image_url";

    String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ARTIST_ID + " TEXT," +
                    ARTIST_NAME + " TEXT," +
                    IMAGE_URL + " TEXT " +
                    ");";

    String DEFAULT_SORT_ORDER = TABLE_NAME + "." + _ID;
}
