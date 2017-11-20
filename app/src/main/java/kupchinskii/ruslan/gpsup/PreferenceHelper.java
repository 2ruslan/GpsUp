package kupchinskii.ruslan.gpsup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class PreferenceHelper {


    public static final String APP_PREFERENCES = "preference";

    public static final String AUTO_START_REBOOTING = "auto_start_rebooting";


    private static SharedPreferences mSettings;

    public static void init(Context context) {
        mSettings = context.getSharedPreferences(PreferenceHelper.APP_PREFERENCES, Context.MODE_PRIVATE);
    }



    //region AUTO_START_REBOOTING
    public static void SetAutoStartRebooting(boolean isAuto) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(AUTO_START_REBOOTING, isAuto);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        }
        else{
            editor.commit();
        }

    }

    public static boolean GetAutoStartRebooting()    {
        return mSettings.getBoolean(AUTO_START_REBOOTING, false);
    }
    // endregion AUTO_START_REBOOTING


}
