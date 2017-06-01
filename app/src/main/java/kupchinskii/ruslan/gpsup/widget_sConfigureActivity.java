package kupchinskii.ruslan.gpsup;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class widget_sConfigureActivity extends Activity {

    Spinner appList;
    RadioButton rbNoClose;
    RadioButton rbCloseApp;
    RadioButton rbCloseGps;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = widget_sConfigureActivity.this;

            if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
                return;

            widgetPref p = new widgetPref();

            p.Package = ((ApplicationInfo) appList.getSelectedItem()).packageName;

            if (rbCloseGps.isChecked())
                p.Mode = widgetPref.en_w_mode.close_on_up;
            else if(rbCloseApp.isChecked())
                p.Mode = widgetPref.en_w_mode.close_on_app;
            else
                p.Mode = widgetPref.en_w_mode.no_close;

            PreferencesHelper.SetWidgetPref(mAppWidgetId, p );

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            widget_s.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            setResult(RESULT_OK, resultValue);
            finish();
        }
    };


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.widget_s_configure);

        PreferencesHelper.init(this);

        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        appList = (Spinner)findViewById(R.id.appList);
        rbNoClose = (RadioButton)findViewById(R.id.rbNoClose);
        rbCloseApp = (RadioButton)findViewById(R.id.rbCloseOnApp);
        rbCloseGps = (RadioButton)findViewById(R.id.rbCloseOnGps);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }



        PackageManager pm = getPackageManager();

        List<ApplicationInfo> pGPS = new ArrayList<ApplicationInfo>();

        for (ApplicationInfo p  : pm.getInstalledApplications(PackageManager.GET_META_DATA)){

            try {
                PackageInfo pInfo = pm.getPackageInfo(p.packageName, PackageManager.GET_PERMISSIONS);
                String[] reqPermission = pInfo.requestedPermissions;
                if (reqPermission != null){
                    for (String perm : reqPermission) {
                        if (perm.equals("android.permission.ACCESS_FINE_LOCATION"))pGPS.add(p);
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {}

        }

        AppListAdapter appListAdapter = new AppListAdapter(this, pm, pGPS);
        appList.setAdapter(appListAdapter);


    }
}

