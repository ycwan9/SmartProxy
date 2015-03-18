package me.smartproxy.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;

public class Utils {

    public static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";

    public static final String AUTO_START_CONFIG_KEY = "AUTO_START_CONFIG_KEY";

    public static void setAutoStartConfig(Context context, boolean isChecked) {
        SharedPreferences preferences = context.getSharedPreferences("SmartProxy",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AUTO_START_CONFIG_KEY, isChecked);
        editor.commit();
    }

    public static String readConfigUrl(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SmartProxy",
                Context.MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEY, "");
    }

    public static void setConfigUrl(Context context, String configUrl) {
        SharedPreferences preferences = context.getSharedPreferences("SmartProxy",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CONFIG_URL_KEY, configUrl);
        editor.commit();
    }

    public static boolean readAutoStartConfig(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SmartProxy",
                Context.MODE_PRIVATE);
        return preferences.getBoolean(AUTO_START_CONFIG_KEY, false);
    }

    static boolean isValidUrl(String url) {
        try {
            if (url == null || url.isEmpty()) {
                return false;
            }

            if (url.startsWith("/")) {//file path
                File file = new File(url);
                if (!file.exists()) {
//                    onLogReceived(String.format("File(%s) not exists.", url));
                    return false;
                }
                if (!file.canRead()) {
//                    onLogReceived(String.format("File(%s) can't read.", url));
                    return false;
                }
            } else if (url.startsWith("ss")) {//shadowsocks
                return true;
            } else { //url
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                    return false;
                }
                if (uri.getHost() == null) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
