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

	private Intent mIntent = new Intent(Config.APPWIDGET_CLICK_ACTION);
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
		}
		super.onReceive(context, intent);
	}

	private void updateView(Context context) {
		ComponentName mCN = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager mAWM = AppWidgetManager.getInstance(context);
		ViewSet(context, false);
		mAWM.updateAppWidget(mCN, mRv);
	}

	private void ViewSet(Context context, boolean init) {
		mRv = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
		mPI = PendingIntent.getBroadcast(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRv.setOnClickPendingIntent(R.id.btn_appwidget, mPI);
		boolean key = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.ENABLED_KEYBLOCK, true);
		if (init) {
			key = !key;
		}
		if (key) {
			mRv.setImageViewResource(R.id.btn_appwidget, R.drawable.ic_appwidget_off);
		} else {
			mRv.setImageViewResource(R.id.btn_appwidget, R.drawable.ic_appwidget_on);
		}
	}

}
