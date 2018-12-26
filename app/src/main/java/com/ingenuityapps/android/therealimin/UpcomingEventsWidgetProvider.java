package com.ingenuityapps.android.therealimin;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class UpcomingEventsWidgetProvider extends AppWidgetProvider {

    private static final String TAG = UpcomingEventsWidgetProvider.class.getSimpleName();

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = getUpcomingEventsRemoteView(context);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getUpcomingEventsRemoteView(Context context) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.upcoming_events_widget);

        Intent intent = new Intent(context,UpcomingEventsWidgetService.class);
        views.setRemoteAdapter(R.id.gv_events, intent);

        views.setEmptyView(R.id.gv_events, R.id.empty_view);

        Intent appIntent = new Intent(context, LoginActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.gv_events, appPendingIntent);

        Intent intentSync = new Intent(context, UpcomingEventsWidgetProvider.class);
        intentSync.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingSync = PendingIntent.getBroadcast(context,0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_refresh,pendingSync);

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        updateUpcomingEventsWidgets(context,appWidgetManager,appWidgetIds);

        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        final String action = intent.getAction();

        if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
        {
            ComponentName componentName = new ComponentName(context, UpcomingEventsWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(componentName),R.id.gv_events);
            int[] appWidgetIds = mgr.getAppWidgetIds(componentName);
            updateUpcomingEventsWidgets(context,mgr,appWidgetIds);
        }

        super.onReceive(context, intent);
    }

    private void updateUpcomingEventsWidgets(Context context, AppWidgetManager mgr, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, mgr, appWidgetId);
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
}

