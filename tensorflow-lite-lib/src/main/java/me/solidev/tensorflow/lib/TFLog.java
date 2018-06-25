package me.solidev.tensorflow.lib;

import android.util.Log;

/**
 * @author _SOLID
 * @since 2018/6/6.
 */
public class TFLog {
    private static boolean isDebug = BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
    }
}
