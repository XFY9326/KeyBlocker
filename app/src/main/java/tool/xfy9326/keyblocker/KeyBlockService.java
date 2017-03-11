package tool.xfy9326.keyblocker;

import android.app.*;
import android.content.*;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import java.lang.reflect.Method;

public class KeyBlockService extends AccessibilityService
{
	private boolean KeyBlocked = true;
	private ButtonBroadcastReceiver bbr;
	private String Notify_Action = "tool.xfy9326.keyblocker.Notification.OnClick";

	@Override
	protected void onServiceConnected()
	{
		ShowNotification();
		super.onServiceConnected();
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent p1)
	{}

	@Override
	public void onInterrupt()
	{
		CloseNotification();
		KeyBlocked = false;
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event)
	{
		return KeyBlocked;
	}

	private void ShowNotification()
	{
		IntentFilter filter = new IntentFilter(Notify_Action);
		bbr = new ButtonBroadcastReceiver();
		registerReceiver(bbr, filter);

		Intent intent = new Intent();
		intent.setAction(Notify_Action);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder notification = new Notification.Builder(this);
		notification.setOngoing(true);
		notification.setSmallIcon(R.drawable.ic_notification);
		notification.setContentTitle(getString(R.string.app_name));
		notification.setContentText(getString(R.string.notify_mes));
		notification.setContentIntent(pendingintent);
		startForeground(1, notification.build());
	}

	private void CloseNotification()
	{
		stopForeground(true);
		if (bbr != null)
		{
			unregisterReceiver(bbr);
		}
	}

	private static void collapseStatusBar(Context context)
	{
		try
		{
			Object statusBarManager = context.getSystemService("statusbar");
			Method collapse;
			if (Build.VERSION.SDK_INT <= 16)
			{
				collapse = statusBarManager.getClass().getMethod("collapse");
			}
			else
			{
				collapse = statusBarManager.getClass().getMethod("collapsePanels");
			}
			collapse.invoke(statusBarManager);
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
		}
	}

	private class ButtonBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			if (p2.getAction() == Notify_Action)
			{
				KeyBlocked = !KeyBlocked;
				collapseStatusBar(p1);
			}
		}
	}
}
