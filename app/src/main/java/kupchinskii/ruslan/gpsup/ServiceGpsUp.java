package kupchinskii.ruslan.gpsup;

import android.annotation.TargetApi;
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
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
                    0, new Intent(getApplicationContext(), MainActivity.class),  PendingIntent.FLAG_UPDATE_CURRENT);
            startForeground(Common.NOTIFY_ID, notification);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_notify_proc)
                    .setContentTitle("GPS Up")
                    .setContentText("Service create")
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    ;
            Notification notification;

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
        Mediator.OnDestroy();
        onStop();
    }

    private void onStop(){
        if (mTimer != null) {
            mTimer.cancel();
            mMyTimerTask.stop();
            mTimer = null;
        }
    }

    private void InitBroadcastReceiver() {
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(Common.BROADCAST_TYPE, -1);
                if(type == Common.BROADCAST_STOP){
                    stopService(new Intent(getApplicationContext(), ServiceGpsUp.class));
                    System.exit(0);
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

        GPS m_gps;
        int cntUp = 0;
        GPS_Result gps;

    public MyTimerTask() {

            dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        m_gps = new GPS(getApplicationContext());
        }

        @Override
        public void run() {

             gps = m_gps.getGpsResult();

                if (gps.accuracy > 0 && gps.accuracy < 50 && cntUp < 10){
                    cntUp++;
                }

                if (cntUp == 20 ){
                    broadcastStop();
                }

                if(Mediator.isShowInfo())
                    Mediator.ShowInfo(GetFormatInfo(gps));

        }

        public void stop() {
            if (m_gps != null) {
                m_gps.stop();
                m_gps = null;
            }
        }

    int ni = 0;
    int lineW = 20;
    int linePos = 0;
    boolean isBack = false;

    DateFormat dateFormat;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    StringBuilder s;

    private String GetFormatInfo(GPS_Result val){
        s = new StringBuilder();


            if (val.status != Common.STATUS_DISABLE) {
                addTyStr(s, String.format("spd : %s\n+/- : %s\nlat : %s\nlon : %s\nalt : %s  "
                        ,val.speed
                        ,val.accuracy
                        ,val.latitude
                        ,val.longitude
                        ,val.altitude

                ));

                if (val.time > 0)
                    addTyStr(s, String.format("\ngps time : %s", dateFormat.format(val.time) + " " + timeFormat.format(val.time)));
            }


        addTyStr(s,String.format("\nsat : %s / %s", val.satAct, val.satCnt));

            if(val.satAct ==0 && val.satCnt == 0){
                if(val.satTotal > 0)
                    addTyStr(s,"\nAGPS : ok");

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

                addTyStr(s, String.format("\n%s%s", Common.getStringWithLengthAndFilledWithCharacter(linePos, ' ' ), '▓' ));

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

            String res = s.toString();
            s = null;

            return  res;

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
                String res = new String(array);
                array = null;

                return res;
            }
            return "";
        }

        void broadcastStop() {
            intentBroadcast.putExtra(Common.BROADCAST_TYPE, Common.BROADCAST_STOP)

            ;

            sendBroadcast(intentBroadcast);
        }

        private void addTyStr(StringBuilder sb, String s){
            try {
                sb.append(s);
            }catch (Exception e){}
        }
    }

}
