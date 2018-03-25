package kupchinskii.ruslan.gpsup;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Common {

    public static final int CONST_GPS_INTERVAL = 200;
    public static final int CONST_TIMER_INTERVAL = 500;

    public final static String BROADCAST_ACTION = "kupchinskii.ruslan.gpsup";

    public static final String BROADCAST_TYPE = "BROADCAST_TYPE";
    public static final String BROADCAST_VALUE = "BROADCAST_VALUE";
    public static final String BROADCAST_VALUE_STATE = "BROADCAST_VALUE_STATE";



    public static final int BROADCAST_INFO = 0;
    public static final int BROADCAST_STOP = 1;





    public static final int STATUS_DISABLE = 0;
    public static final int STATUS_ENABLE = 1;

    public static final int NOTIFY_ID = 476;

    public static void start(Context context, String act){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(act);

        context.startActivity(intent);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isForeground(Context ctx, String myPackage){
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ctx.ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) {
            return true;
        }
        return false;
    }

    public static void resetAData(Context context){
        LocationManager locationManagerGPS = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
        locationManagerGPS.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data", null);
        Bundle bundle = new Bundle();
        locationManagerGPS.sendExtraCommand("gps", "force_xtra_injection", bundle);
        locationManagerGPS.sendExtraCommand("gps", "force_time_injection", bundle);
        Toast.makeText(context, "Request A-GPS data ...", Toast.LENGTH_SHORT).show();

    }

    private static String _title;
    private static String _msg;
    private static boolean _isUp;

    final static Intent intentBroadcast = new Intent(Common.BROADCAST_ACTION);
    static PendingIntent pIntent = null;//PendingIntent.getBroadcast(context, Common.BROADCAST_STOP, intentBroadcast, PendingIntent.FLAG_UPDATE_CURRENT);

    static PendingIntent pi;

    public static  void notify(Context context, String title,  String msg, boolean isUp){

        if(title.equals(_title)  && msg.equals(_msg) && isUp == _isUp)
            return;

        _title = title;
        _msg = msg;
        _isUp = isUp;

        if (pIntent == null) {
            intentBroadcast.putExtra(Common.BROADCAST_TYPE, Common.BROADCAST_STOP);
            pIntent = PendingIntent.getBroadcast(context, Common.BROADCAST_STOP, intentBroadcast, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        if (pi == null) {
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pi = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if (Build.VERSION.SDK_INT < 11){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(isUp ? R.drawable.ic_notify_up : R.drawable.ic_notify_proc)
                            .setLargeIcon(LargeIcon(context))
                            .setContentTitle(title)
                            .setContentIntent(pi)
                            .setContentText(msg)
                            .setOnlyAlertOnce(true)
                            .setOngoing(true)
                            .addAction(R.drawable.close_icon, "Exit", pIntent)
                    ;

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFY_ID, mBuilder.build());

        }
        else {
            Notification notification;
            Notification.Builder builder = new Notification.Builder(context)
                    //.setLargeIcon(R.drawable.ic_launcher)
                    .setSmallIcon(isUp ? R.drawable.ic_notify_up : R.drawable.ic_notify_proc)
                    .setLargeIcon(LargeIcon(context))
                    .setContentTitle(title)
                    .setContentIntent(pi)
                    .setContentText(msg)
                    .setOnlyAlertOnce(true)

                    .setOngoing(true);

            if (Build.VERSION.SDK_INT >= 16)
                builder.addAction(R.drawable.close_icon, "Exit", pIntent);

             notification = builder.build();

             NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
             notificationManager.notify(NOTIFY_ID, notification);
        }

    }
    static Bitmap _LargeIcon;
    static private Bitmap LargeIcon(Context context) {
        return  _LargeIcon != null
                    ? _LargeIcon
                    :(_LargeIcon =  (((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap()));
    }

    public static String getStringWithLengthAndFilledWithCharacter(int length, char charToFill) {
        if (length > 0) {
            char[] array = new char[length];
            Arrays.fill(array, charToFill);
            return new String(array);
        }
        return "";
    }


}
