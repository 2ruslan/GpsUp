package kupchinskii.ruslan.gpsup;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public final static int ACTIVITY_SETTINGS_ID = 1;
    public final static String SAVE_TXT = "txt";

    BroadcastReceiver br;
    TextView tvInfo;
    boolean isPowerOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
        }catch (Exception ex)
        {
            setContentView(R.layout.activity_main_olddev);
        }

        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvInfo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Terminus.otf"), Typeface.NORMAL);

        if (checkLocationPermission())
            run();
        else
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        PreferenceHelper.init(this);
        PreferenceHelper.SetAutoStartRebooting(true);
    }

    private void run(){
        InitBroadcastReceiver();
        startServ();
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    run();
                else
                    finish();
                return;
            }
        }
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(br != null)
            unregisterReceiver(br);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvInfo.setText(savedInstanceState.getString(SAVE_TXT));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString(SAVE_TXT, tvInfo.getText().toString());
        }
        catch (Exception ex){}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SETTINGS_ID &&  resultCode == RESULT_OK && isPowerOn){
            stopSrv();
            startServ();
        }
    }

    private void InitBroadcastReceiver() {
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(Common.BROADCAST_TYPE, -1);
                if(type == Common.BROADCAST_INFO){
                    isPowerOn = intent.getBooleanExtra(Common.BROADCAST_VALUE_STATE, false);
                    tvInfo.setText(intent.getStringExtra(Common.BROADCAST_VALUE));
                }
                else if (type == Common.BROADCAST_STOP)
                    onClickPower(null);

            }
        };
        IntentFilter intFilt = new IntentFilter(Common.BROADCAST_ACTION);
        registerReceiver(br, intFilt);
    }

    public void startServ() {
        if (!Common.isServiceRunning(this, ServiceGpsUp.class)) {
            startService(new Intent(this, ServiceGpsUp.class));
        }
    }

    public void stopSrv() {
        stopService(new Intent(this, ServiceGpsUp.class));
    }

    public void onClickPower(View v) {
        PreferenceHelper.SetAutoStartRebooting(false);
        stopSrv();
        finish();
    }
    public void onClickExit(View v) {
        finish();
    }

    public void onSendGeo(View v) {


        if (GPS.latitude > 0 && GPS.longitude > 0) {

            String shareBody = (GPS.latitude > 0  ? "N" : "S") + Math.abs(GPS.latitude) + " " + (GPS.longitude > 0  ? "E" : "W") + Math.abs(GPS.longitude) ;
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "share location"));
        }

    }

    public void onClickAGPS(View v) {
        Common.resetAData(this);
    }

}
