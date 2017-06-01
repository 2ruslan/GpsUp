package kupchinskii.ruslan.gpsup;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new TryMe());
        super.onCreate();
    }
}
