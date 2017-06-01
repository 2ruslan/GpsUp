package kupchinskii.ruslan.gpsup;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    public static final String APP_PREFERENCES = "pref";
    public static final String APP_PREFERENCES_AUTO_START = "AUTOSTART";

    private static SharedPreferences mSettings;

    public static void init(Context context) {
        mSettings = context.getSharedPreferences(PreferencesHelper.APP_PREFERENCES, Context.MODE_PRIVATE);
    }



    //region AUTOSTART
    public static void SetWidgetPref(int widgetId, widgetPref p)    {
        SharedPreferences.Editor editor = mSettings.edit();

        editor.putString("W_PCK_" + String.valueOf(widgetId), p.Package);
        editor.putInt("W_MOD_" + String.valueOf(widgetId), p.Mode.getValue());

        editor.apply();
    }

    public static widgetPref GetWidgetPref(int widgetId)    {
        widgetPref res = new widgetPref();
        try {
            res.Package = mSettings.getString("W_PCK_" + String.valueOf(widgetId), "");
            res.Mode = widgetPref.en_w_mode.fromInteger(mSettings.getInt("W_MOD_" + String.valueOf(widgetId), -1));
        } catch (Exception ex) {
        }

        return res;
    }


}
