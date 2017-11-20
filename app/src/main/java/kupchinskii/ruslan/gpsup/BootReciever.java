package kupchinskii.ruslan.gpsup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceHelper.init(context);
        if (PreferenceHelper.GetAutoStartRebooting()) {

            Intent s = new Intent(context, MainActivity.class);
            s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
            context.startActivity(s);

            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(setIntent);
        }
    }
}
