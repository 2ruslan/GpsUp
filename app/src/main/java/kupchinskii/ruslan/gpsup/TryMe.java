package kupchinskii.ruslan.gpsup;

import android.util.Log;

public class TryMe implements Thread.UncaughtExceptionHandler {

    Thread.UncaughtExceptionHandler oldHandler;

    public TryMe() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        if(oldHandler != null)
            oldHandler.uncaughtException(thread, throwable);
    }
}