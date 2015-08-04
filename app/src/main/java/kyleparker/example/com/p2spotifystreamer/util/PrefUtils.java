package kyleparker.example.com.p2spotifystreamer.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kyleparker on 8/4/2015.
 */
public class PrefUtils {
    /**
     * Gets a preference key
     *
     * @param context the context
     * @param keyId the key id
     */
    public static String getKey(Context context, int keyId) {
        return context.getString(keyId);
    }

    /**
     * Gets a boolean preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param defaultValue the default value
     */
    public static boolean getBoolean(Context context, int keyId, boolean defaultValue) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(getKey(context, keyId), defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Sets a boolean preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param value the value
     */
    public static void setBoolean(Context context, int keyId, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getKey(context, keyId), value);
        editor.apply();
    }

    /**
     * Gets an double preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param defaultValue the default value
     */
    public static float getFloat(Context context, int keyId, float defaultValue) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getFloat(getKey(context, keyId), defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Sets an integer preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param value the value
     */
    public static void setFloat(Context context, int keyId, float value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(getKey(context, keyId), value);
        editor.apply();
    }

    /**
     * Gets an integer preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param defaultValue the default value
     */
    public static int getInt(Context context, int keyId, int defaultValue) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getInt(getKey(context, keyId), defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Sets an integer preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param value the value
     */
    public static void setInt(Context context, int keyId, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getKey(context, keyId), value);
        editor.apply();
    }

    /**
     * Gets a long preference value.
     *
     * @param context the context
     * @param keyId the key id
     */
    public static long getLong(Context context, int keyId) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getLong(getKey(context, keyId), -1L);
        } catch (Exception ex) {
            ex.printStackTrace();
            return keyId;
        }
    }

    /**
     * Sets a long preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param value the value
     */
    public static void setLong(Context context, int keyId, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(getKey(context, keyId), value);
        editor.apply();
    }

    /**
     * Gets a string preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param defaultValue default value
     */
    public static String getString(Context context, int keyId, String defaultValue) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(getKey(context, keyId), defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Sets a string preference value.
     *
     * @param context the context
     * @param keyId the key id
     * @param value the value
     */
    public static void setString(Context context, int keyId, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getKey(context, keyId), value);
        editor.apply();
    }
}
