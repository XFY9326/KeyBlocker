package tool.xfy9326.keyblocker.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

public class WidgetProvider extends AppWidgetProvider {
    private RemoteViews mRv;
    private PendingIntent mPI;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ViewSet(context, true);
        appWidgetManager.updateAppWidget(appWidgetIds, mRv);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Config.APPWIDGET_CLICK_ACTION)) {
            BaseMethod.KeyLockBroadcast(context);
            updateView(context);
        } else if (intent.getAction().equals(Config.APPWIDGET_UPDATE_ACTION)) {
            reloadView(context, true);
        }
        super.onReceive(context, intent);
    }

    private void updateView(Context context) {
        if (BaseMethod.isAccessibilitySettingsOn(context)) {
            reloadView(context, false);
        } else {
            BaseMethod.RunAccessibilityService(context);
        }
    }

    private void reloadView(Context context, boolean init) {
        ComponentName mCN = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager mAWM = AppWidgetManager.getInstance(context);
        ViewSet(context, init);
        mAWM.updateAppWidget(mCN, mRv);
    }

    private void ViewSet(Context context, boolean init) {
        mRv = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
        mPI = PendingIntent.getBroadcast(context, 0, new Intent(Config.APPWIDGET_CLICK_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        mRv.setOnClickPendingIntent(R.id.btn_appwidget, mPI);
        boolean key = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.ENABLED_KEYBLOCK, false);
        if (init) {
            key = !key;
            if (!BaseMethod.isAccessibilitySettingsOn(context)) {
                key = true;
            }
        }
        if (key) {
            mRv.setImageViewResource(R.id.btn_appwidget, R.drawable.ic_appwidget_off);
        } else {
            mRv.setImageViewResource(R.id.btn_appwidget, R.drawable.ic_appwidget_on);
        }
    }

}
