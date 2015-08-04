package kyleparker.example.com.p2spotifystreamer.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import kyleparker.example.com.p2spotifystreamer.content.layout.MyArtistColumns;
import kyleparker.example.com.p2spotifystreamer.content.layout.MyTrackColumns;
import kyleparker.example.com.p2spotifystreamer.util.LogHelper;

/**
 * A {@link android.content.ContentProvider} that handles access to the database tables.
 *
 * Created by kyleparker on 8/4/2015.
 */
public class AppContentProvider extends ContentProvider {
    private static final String TAG = AppContentProvider.class.getSimpleName();
    public static final String AUTHORITY = "kyleparker.example.com.p2spotifystreamer";

    private static final String DATABASE_NAME = "music.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_INNER_JOIN = " INNER JOIN ";
    private static final String SQL_ON = " ON ";

    /**
     * Database helper for creating and upgrading the database.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(MyArtistColumns.CREATE_TABLE);
            db.execSQL(MyTrackColumns.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    /**
     * Types of url.
     */
    private enum UrlType {
        ARTIST, ARTIST_ID,
        TRACK, TRACK_ID, TRACK_ARTIST_ID
    }

    private final UriMatcher uriMatcher;
    private static SQLiteDatabase db;

    public AppContentProvider() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, MyArtistColumns.TABLE_NAME, UrlType.ARTIST.ordinal());
        uriMatcher.addURI(AUTHORITY, MyArtistColumns.TABLE_NAME + "/*", UrlType.ARTIST_ID.ordinal());

        uriMatcher.addURI(AUTHORITY, MyTrackColumns.TABLE_NAME, UrlType.TRACK.ordinal());
        uriMatcher.addURI(AUTHORITY, MyTrackColumns.TABLE_NAME + "/*", UrlType.TRACK_ID.ordinal());
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        try {
            db = databaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Unable to open database for writing", e);
        }
        return db != null;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String sortOrder = null;
        switch (getUrlType(url)) {
            case ARTIST:
                queryBuilder.setTables(MyArtistColumns.TABLE_NAME);
                sortOrder = sort != null ? sort : MyArtistColumns.DEFAULT_SORT_ORDER;
                break;
            case ARTIST_ID:
                queryBuilder.setTables(MyArtistColumns.TABLE_NAME);
                queryBuilder.appendWhere(MyArtistColumns.ARTIST_ID + " = '" + url.getPathSegments().get(1) + "'");
                break;
            case TRACK:
                queryBuilder.setTables(MyTrackColumns.TABLE_NAME);
                sortOrder = sort != null ? sort : MyTrackColumns.DEFAULT_SORT_ORDER;
                break;
            case TRACK_ARTIST_ID:
                queryBuilder.setTables(MyTrackColumns.TABLE_NAME +
                        SQL_INNER_JOIN +
                        MyArtistColumns.TABLE_NAME + SQL_ON +
                        appendTableName(MyArtistColumns.TABLE_NAME, MyArtistColumns.ARTIST_ID) + " = " +
                        appendTableName(MyTrackColumns.TABLE_NAME, MyTrackColumns.ARTIST_ID));
                queryBuilder.appendWhere(appendTableName(MyTrackColumns.TABLE_NAME, MyTrackColumns.ARTIST_ID) +
                        " = '" + url.getPathSegments().get(1) + "'");
                sortOrder = sort != null ? sort : MyTrackColumns.DEFAULT_SORT_ORDER;
                break;
            case TRACK_ID:
                queryBuilder.setTables(MyTrackColumns.TABLE_NAME +
                        SQL_INNER_JOIN +
                        MyArtistColumns.TABLE_NAME + SQL_ON +
                        appendTableName(MyArtistColumns.TABLE_NAME, MyArtistColumns.ARTIST_ID) + " = " +
                        appendTableName(MyTrackColumns.TABLE_NAME, MyTrackColumns.ARTIST_ID));
                queryBuilder.appendWhere(appendTableName(MyTrackColumns.TABLE_NAME, MyTrackColumns.TRACK_ID) +
                        " = '" + url.getPathSegments().get(1) + "'");
                sortOrder = sort != null ? sort : MyTrackColumns.DEFAULT_SORT_ORDER;
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), url);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (getUrlType(uri)) {
            case ARTIST:
                return MyArtistColumns.CONTENT_TYPE;
            case ARTIST_ID:
                return MyArtistColumns.CONTENT_ITEMTYPE;
            case TRACK:
                return MyTrackColumns.CONTENT_TYPE;
            case TRACK_ARTIST_ID:
            case TRACK_ID:
                return MyTrackColumns.CONTENT_ITEMTYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (initialValues == null) {
            initialValues = new ContentValues();
        }
        Uri result = null;
        try {
            db.beginTransaction();
            result = insertContentValues(url, getUrlType(url), initialValues);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    @Override
    public int delete(Uri url, String where, String[] selectionArgs) {
        String table;

        switch (getUrlType(url)) {
            case ARTIST:
                table = MyArtistColumns.TABLE_NAME;
                break;
            case TRACK:
                table = MyTrackColumns.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        LogHelper.w(TAG, "Deleting table " + table);
        int count = 0;
        try {
            db.beginTransaction();
            count = db.delete(table, where, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }

        getContext().getContentResolver().notifyChange(url, null, true);

        return count;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] selectionArgs) {
        String table;
        String whereClause;
        switch (getUrlType(url)) {
            case ARTIST_ID:
                table = MyArtistColumns.TABLE_NAME;
                whereClause = MyArtistColumns.ARTIST_ID + "='" + url.getPathSegments().get(1) + "'";
                if (!TextUtils.isEmpty(where)) {
                    whereClause += " AND (" + where + ")";
                }
                break;
            case TRACK_ID:
                table = MyTrackColumns.TABLE_NAME;
                whereClause = MyTrackColumns.TRACK_ID + "='" + url.getPathSegments().get(1) + "'";
                if (!TextUtils.isEmpty(where)) {
                    whereClause += " AND (" + where + ")";
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        int count = 0;
        try {
            db.beginTransaction();
            count = db.update(table, values, whereClause, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }

        getContext().getContentResolver().notifyChange(url, null, true);

        return count;
    }

    private static String appendTableName(String table, String column) {
        return table + "." + column;
    }

    /**
     * Gets the {@link kyleparker.example.com.p2spotifystreamer.content.AppContentProvider.UrlType} for a url.
     *
     * @param url the url
     */
    private UrlType getUrlType(Uri url) {
        return UrlType.values()[uriMatcher.match(url)];
    }

    /**
     * Inserts a content based on the url type.
     *
     * @param url           the content url
     * @param urlType       the url type
     * @param contentValues the content values
     */
    private Uri insertContentValues(Uri url, UrlType urlType, ContentValues contentValues) {
        switch (urlType) {
            case ARTIST:
                return insertArtist(url, contentValues);
            case TRACK:
                return insertTrack(url, contentValues);
            default:
                throw new IllegalArgumentException("Unknown url " + url);
        }
    }

    /**
     * Inserts the artist information
     *
     * @param url the content url
     * @param contentValues the content values
     */
    private Uri insertArtist(Uri url, ContentValues contentValues) {
        boolean hasId = contentValues.containsKey(MyArtistColumns.ARTIST_ID);

        if (!hasId) {
            throw new IllegalArgumentException("ArtistId is required.");
        }

        long rowId = db.insert(MyArtistColumns.TABLE_NAME, MyArtistColumns._ID, contentValues);

        if (rowId >= 0) {
            Uri uri = ContentUris.appendId(MyArtistColumns.CONTENT_URI.buildUpon(), rowId).build();
            getContext().getContentResolver().notifyChange(url, null, true);
            return uri;
        }

        throw new SQLiteException("Failed to insert row into " + url);
    }

    /**
     * Inserts the track information
     *
     * @param url the content url
     * @param contentValues the content values
     */
    private Uri insertTrack(Uri url, ContentValues contentValues) {
        boolean hasId = contentValues.containsKey(MyTrackColumns.TRACK_ID);

        if (!hasId) {
            throw new IllegalArgumentException("TrackId is required.");
        }

        long rowId = db.insert(MyTrackColumns.TABLE_NAME, MyTrackColumns._ID, contentValues);

        if (rowId >= 0) {
            Uri uri = ContentUris.appendId(MyTrackColumns.CONTENT_URI.buildUpon(), rowId).build();
            getContext().getContentResolver().notifyChange(url, null, true);
            return uri;
        }

        throw new SQLiteException("Failed to insert row into " + url);
    }
}
