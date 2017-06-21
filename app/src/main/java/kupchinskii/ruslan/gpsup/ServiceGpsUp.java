package kupchinskii.ruslan.gpsup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceGpsUp extends Service {

    static private Timer mTimer;
    static private MyTimerTask mMyTimerTask;
    final Intent intentBroadcast = new Intent(Common.BROADCAST_ACTION);

    BroadcastReceiver br;

    public void onCreate() {

        InitBroadcastReceiver();

        Common.notify(getApplicationContext()
                ,"sat : 0 / 0"
                ,""
                ,false
        );
        if (Build.VERSION.SDK_INT < 11){
           NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notify_proc)
                            .setContentTitle("GPS Up")
                            .setContentText("Service create")
                            .setOnlyAlertOnce(true)
                            .setOngoing(true);
                    ;

            Notification notification = mBuilder.getNotification();
            notification.contentIntent = PendingIntent.getActivity(this,
                    0, new Intent(getApplicationContext(), MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
            startForeground(Common.NOTIFY_ID, notification);



        }
        else {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_notify_proc)
                    .setContentTitle("GPS Up")
                    .setContentText("Service create")
                    .setOnlyAlertOnce(true)
                    .setOngoing(true);
            Notification notification;
            if (Build.VERSION.SDK_INT < 16)
                notification = builder.getNotification();
            else
                notification = builder.build();

            startForeground(Common.NOTIFY_ID, notification);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if(mMyTimerTask != null){
            mMyTimerTask.stop();
            mMyTimerTask = null;
        }

        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 1000, Common.CONST_TIMER_INTERVAL);

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        onStop();

       // broadcastMsg("Service stoped", false);
    }

    private void onStop(){
        if (mTimer != null) {
            mTimer.cancel();
            mMyTimerTask.stop();
            mTimer = null;
        }
    }

    void broadcastMsg(String msg, boolean powerOn) {
        intentBroadcast.putExtra(Common.BROADCAST_TYPE, Common.BROADCAST_INFO)
                .putExtra(Common.BROADCAST_VALUE, msg )
                .putExtra(Common.BROADCAST_VALUE_STATE, powerOn)
        ;

        sendBroadcast(intentBroadcast);
    }


    private void InitBroadcastReceiver() {
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(Common.BROADCAST_TYPE, -1);
                if(type == Common.BROADCAST_STOP){
                    stopService(new Intent(getApplicationContext(), ServiceGpsUp.class));
                }
            }
        };
        IntentFilter intFilt = new IntentFilter(Common.BROADCAST_ACTION);
        registerReceiver(br, intFilt);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
/*----------------------------------------------------------------------------------------------*/
    public class MyTimerTask extends TimerTask {

        GPS g;
        int cntUp = 0;

        public MyTimerTask() {

            dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

            g = new GPS(getApplicationContext());
        }

        @Override
        public void run() {

            GPS_Result gps = g.getResult();


                if (gps.accuracy > 0 && gps.accuracy < 50 && cntUp < 10){
                    cntUp++;
                }

                if (cntUp == 20 ){
                    broadcastStop();
                }

            broadcastMsg(GetFormatInfo(gps), true);
        }

        public void stop() {
            if (g != null) {
                g.stop();
                g = null;
            }
        }

    int ni = 0;
    int lineW = 20;
    int linePos = 0;
    boolean isBack = false;

    DateFormat dateFormat;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


    private String GetFormatInfo(GPS_Result val){
            StringBuilder s = new StringBuilder();

            if(val.status != Common.STATUS_DISABLE) {
                s.append(String.format("spd : %s\n+/- : %s\nlat : %s\nlon : %s ", val.speed, val.accuracy, val.latitude, val.longitude));

                if (val.time > 0)
                    s.append(String.format("gps time : %s\n", dateFormat.format(val.time) + " " + timeFormat.format(val.time)));
            }

            s.append(String.format("\nsat : %s / %s", val.satAct, val.satCnt));

            if(val.satAct ==0 && val.satCnt == 0){
                if(val.satTotal > 0)
                    s.append("\nAGPS : ok");

                if(isBack)
                    linePos -- ;
                else
                    linePos++;

                if(linePos > lineW) {
                    isBack = true;
                    linePos = lineW;
                }
                if(linePos < 0) {
                    isBack = false;
                    linePos = 0;
                }

                s.append( String.format("\n%s%s", Common.getStringWithLengthAndFilledWithCharacter(linePos, ' ' ), '▓' ));

            }

            if(ni++ == 0) {
                if(val.status != Common.STATUS_DISABLE) {
                    Common.notify(getApplicationContext()
                            , String.format("sat: %s / %s", val.satAct, val.satCnt)
                            , String.format("spd : %s     +/- : %s "
                                    ,Math.round(val.speed)
                                    ,Math.round(val.accuracy)

                            )
                            ,true
                    );
                }else
                {
                    Common.notify(getApplicationContext()
                            , String.format("\nsat : %s / %s", val.satAct, val.satCnt)
                            ,""
                            ,false
                    );
                }

            }
            else {
                if (ni == 2)
                    ni = 0;
            }



            for(int i= 0 ; i < val.satCnt; i++)
                s.append(GetSatInfo (val.SInfo[i].num, val.SInfo[i].isFix, val.SInfo[i].snr ));


            return  s.toString();

        }

        private String GetSatInfo(int num, boolean isFix, float snr){
            String strNum = String.valueOf(num);

            return String.format("\n %s %s %s %s"
                    ,  ("   " + strNum).substring(strNum.length() + 1 )
                    , isFix ? " ✓ " : "   "
                    , snr >0 && snr <10 ? " " + (int)snr : snr >= 10 ? (int)snr : " "
                    , getStringWithLengthAndFilledWithCharacter(Math.round(snr/5), '▓' )

            );
        }
        protected String getStringWithLengthAndFilledWithCharacter(int length, char charToFill) {
            if (length > 0) {
                char[] array = new char[length];
                Arrays.fill(array, charToFill);
                return new String(array);
            }
            return "";
        }

        void broadcastStop() {
            intentBroadcast.putExtra(Common.BROADCAST_TYPE, Common.BROADCAST_STOP)

            ;

            sendBroadcast(intentBroadcast);
        }
    }

}
