package kupchinskii.ruslan.gpsup;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link widget_sConfigureActivity widget_sConfigureActivity}
 */
public class widget_s extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_s);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
         //   widget_sConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    /**/
    static void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
                             int widgetID) {

        RemoteViews widgetView = new RemoteViews(ctx.getPackageName(), R.layout.widget_s);
     //   widgetView.setTextViewText(R.id.appwidget_text, "+GpsUp" + String.valueOf(widgetID));

        Intent intent;
        intent = new Intent(ctx,  ServiceGpsUp.class);
        PreferencesHelper.init(ctx);
        widgetPref p = PreferencesHelper.GetWidgetPref(widgetID);

        Common.logInFile("!!!",  String.valueOf(widgetID));
        Common.logInFile("!!!", p.Package );



        try
        {
            intent.putExtra(Common.PARM_SERVICE_START, p.Package);
            intent.putExtra(Common.PARM_SERVICE_MODE, p.Mode.getValue());
            widgetView.setImageViewBitmap(R.id.btWidget, Common.drawableToBitmap(ctx.getPackageManager().getApplicationIcon(p.Package)));
        }
        catch (Exception e)
        {
            return;
        }


        PendingIntent configPendingIntent = PendingIntent.getService(ctx, 0, intent, 0);

        widgetView.setOnClickPendingIntent(R.id.btWidget, configPendingIntent);


        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }


     /**/
}

