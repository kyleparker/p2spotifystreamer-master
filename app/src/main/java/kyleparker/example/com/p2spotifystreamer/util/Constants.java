package kyleparker.example.com.p2spotifystreamer.util;

/**
 * App constants
 *
 * Created by kyleparker on 6/17/2015.
 */
public class Constants {
    public final static int STATUS_OK = 200;

    public static final String EXTRA_ARTIST_ID = "artist_id";
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IS_PLAYING = "is_playing";
    public static final String EXTRA_IS_TABLET = "is_tablet";
    public static final String EXTRA_SEEK_POSITION = "seek_position";
    public static final String EXTRA_SELECTED_ARTIST_POSITION = "selected_artist_position";
    public static final String EXTRA_SELECTED_TRACK_POSITION = "selected_track_position";
    public static final String EXTRA_ARTIST_NAME = "artist_name";
    public static final String EXTRA_TRACK = "track";

    public final static String KEY_ARTIST_ARRAY = "key_artist_array";
    public static final String KEY_BIND_SERVICE = "key_bind_service";
    public final static String KEY_TRACK_ARRAY = "key_track_array";

    public final static String US_COUNTRY_ID = "US";

    public final static String SETTINGS_NAME = "SettingsActivity";

    public enum MimeType {
        AUDIO ("audio/3gpp"),
        IMAGE ("image/*"),
        TEXT ("text/plain");

        private final String mimeType;

        MimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getValue() {
            return mimeType;
        }
    }
}
