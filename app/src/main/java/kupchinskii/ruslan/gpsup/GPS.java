package kupchinskii.ruslan.gpsup;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;


public class GPS implements LocationListener, GpsStatus.Listener {
    private LocationManager locationManagerGPS;

    GPS_Result currentResult = new GPS_Result();
    private static long lasDate;
    Context context;
    public boolean IsReseting = false;

    public static long getLasDate(){
        return  lasDate;
    }

    public GPS(Context contextParm) {
        context = contextParm;
        lasDate = 0;
        start();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
        try {
            GpsStatus status = locationManagerGPS.getGpsStatus(null);
            Iterable<GpsSatellite> sats = status.getSatellites();
            int Satellites = 0;
            int Fix = 0;

            currentResult.satTotal = 0;
            currentResult.satAct = 0;
            currentResult.satCnt = 0;

            for (GpsSatellite sat : sats) {

                currentResult.satTotal++;

                if(sat.usedInFix())
                    currentResult.satAct++;

                if(sat.getSnr()>0) {

                    currentResult.SInfo[Satellites].isFix = sat.usedInFix();
                    currentResult.SInfo[Satellites].num = sat.getPrn();
                    currentResult.SInfo[Satellites].snr = sat.getSnr();

                    currentResult.satCnt++;
                }
            }

            if(currentResult.satAct  > 0)
                IsReseting = false;

        }catch (Exception ex) {
            Common.logInFile("onGpsStatusChanged", ex);
        }

       }
    }

    @Override
    public void onLocationChanged(Location location) {

        try {
            currentResult.status = Common.STATUS_ENABLE;
            currentResult.speed = (int) ((location.getSpeed() * 3600) / 1000);
            currentResult.accuracy = location.getAccuracy();
            currentResult.latitude = location.getLatitude();
            currentResult.longitude = location.getLongitude();
            lasDate = Calendar.getInstance().getTimeInMillis();
        } catch (Exception ex) {
            Common.logInFile("onLocationChanged", ex);
            currentResult.reset();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status != LocationProvider.AVAILABLE)
            currentResult.reset();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Common.logInFile("onProviderEnabled", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Common.logInFile("onProviderDisabled", provider);
    }

    private void xtra(){

        boolean res;
        String msg;
        Bundle bundle = new Bundle();

        res = locationManagerGPS.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", bundle);
        msg = res? "force xtra injection - ok" : "force xtra injection - err";
        Common.logInFile("FORCE", msg);

        res = locationManagerGPS.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_time_injection", bundle);
        msg = res? "force time injection - ok" : "force time injection - err";
        Common.logInFile("FORCE", msg);
    }

    private void start(){
        try{
            Common.logInFile("START", "begin");
            locationManagerGPS = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManagerGPS != null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }
                }
            }

            locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, Common.CONST_GPS_INTERVAL, (float) 0.0, this, Looper.getMainLooper());
            locationManagerGPS.addGpsStatusListener(this);
            Common.logInFile("START", "end ok");
            
            xtra();
               Common.logInFile("START", "xtra ok");
        }
        catch (Exception ex){
            Common.logInFile("START ERR", Log.getStackTraceString(ex));
        }
    }

    public void stop() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManagerGPS.removeUpdates(this);
        locationManagerGPS.removeGpsStatusListener(this);
        locationManagerGPS = null;
    }

    public GPS_Result getResult(){
        if( lasDate> 0 && Calendar.getInstance().getTimeInMillis() - lasDate > 10000 && currentResult.fixCnt == 0) {
            currentResult.reset();
        }

        if(currentResult != null &&  lasDate > 0 && currentResult.fixCnt == 0 ) {
            long p = Calendar.getInstance().getTimeInMillis() - lasDate;

            if (!IsReseting && currentResult.satAct == 0 && p > 10000){
                IsReseting = true;
                lasDate = Calendar.getInstance().getTimeInMillis();
                Common.logInFile("RESET", "10");
                xtra();
            }
            if( IsReseting && p > 25000 && currentResult.satCnt == 0) {
                IsReseting = false;
                lasDate = Calendar.getInstance().getTimeInMillis();
                Common.logInFile("RESET", "25");
                xtra();
            }
        }

        return currentResult;
    }



}
