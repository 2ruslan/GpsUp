package kupchinskii.ruslan.gpsup;

import android.Manifest;
import android.annotation.SuppressLint;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class GPS implements LocationListener, GpsStatus.Listener , IGpsResult{
    private LocationManager locationManagerGPS;

    GPS_Result currentResult = new GPS_Result();
    private long lasDate;
    Context context;
    public boolean IsReseting = false;


    public GPS(Context contextParm) {
        context = contextParm;
        lasDate = 0;
        Mediator.RegisterGpsResult(this);
        start();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
        try {
            @SuppressLint("MissingPermission") GpsStatus status = locationManagerGPS.getGpsStatus(null);
            Iterable<GpsSatellite> sats = status.getSatellites();

            currentResult.satTotal = 0;
            currentResult.satAct = 0;
            currentResult.satCnt = 0;

            for (GpsSatellite sat : sats) {

                currentResult.satTotal++;

                if(sat.usedInFix())
                    currentResult.satAct++;

                if(sat.getSnr()>0) {

                    currentResult.SInfo[currentResult.satCnt].isFix = sat.usedInFix();
                    currentResult.SInfo[currentResult.satCnt].num = sat.getPrn();
                    currentResult.SInfo[currentResult.satCnt].snr = sat.getSnr();

                    currentResult.satCnt++;
                }
            }

            if(currentResult.satAct  > 0)
                IsReseting = false;

        }catch (Exception ex) {

        }

       }
    }

    @Override
    public void onLocationChanged(Location location) {

        lasDate = Calendar.getInstance().getTimeInMillis();

        try {
            currentResult.status = Common.STATUS_ENABLE;
            currentResult.time = location.getTime();
            currentResult.speed = (int) ((location.getSpeed() * 3600) / 1000);
            currentResult.accuracy = location.getAccuracy();
            currentResult.latitude = location.getLatitude();
            currentResult.longitude = location.getLongitude();
            currentResult.altitude = location.getAltitude();

        } catch (Exception ex) {
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
       // if (status != LocationProvider.AVAILABLE)
       //     currentResult.reset();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void xtra(){
        Bundle bundle = new Bundle();

        locationManagerGPS.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", bundle);
        locationManagerGPS.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_time_injection", bundle);
    }

    private void start(){
        try{

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

            
            xtra();
        }
        catch (Exception ex){
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

    public GPS_Result getGpsResult(){
        if( lasDate > 0 && Calendar.getInstance().getTimeInMillis() - lasDate > 15000 && currentResult.fixCnt == 0) {
            currentResult.reset();
        }

        if(currentResult != null &&  lasDate > 0 && currentResult.fixCnt == 0 ) {
            long p = Calendar.getInstance().getTimeInMillis() - lasDate;

            if (!IsReseting && currentResult.satAct == 0 && p > 10000){
                IsReseting = true;
                lasDate = Calendar.getInstance().getTimeInMillis();

                xtra();
            }
            if( IsReseting && p > 25000 && currentResult.satCnt == 0) {
                IsReseting = false;
                lasDate = Calendar.getInstance().getTimeInMillis();

                xtra();
            }
        }

        return currentResult;
    }

}
