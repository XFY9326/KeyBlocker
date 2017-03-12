package tool.xfy9326.keyblocker;

import android.app.*;
import android.content.*;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class KeyBlockService extends AccessibilityService
{
	private boolean KeyBlocked = false;
	private ButtonBroadcastReceiver bbr = null;
	private Notification.Builder notification = null;
	private SharedPreferences sp = null;
	private SharedPreferences.Editor sped = null;
	private boolean QuickSettingControl = false;

	@Override
	public void onCreate()
	{
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();
		super.onCreate();
	}

	@Override
	protected void onServiceConnected()
	{
		ControlModeSet();
		ReceiverRegister();
		if (!QuickSettingControl)
		{
			ShowNotification();
		}
		sped.putBoolean("KeyBlocked", KeyBlocked);
		sped.commit();
		super.onServiceConnected();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent p1)
	{}

	@Override
	public void onInterrupt()
	{
		ReceiverUnregister();
		if (!QuickSettingControl)
		{
			CloseNotification();
		}
		KeyBlocked = false;
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event)
	{
		int keycode = event.getKeyCode();
		if (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_MUTE || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			if (!sp.getBoolean("VolumeButton_Block", false))
			{
				return false;
			}
		}
		return KeyBlocked;
	}

	private void ControlModeSet()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			if (!sp.getBoolean("ForceNotify", false))
			{
				QuickSettingControl = true;
			}
		}
	}
	
	private void ReceiverRegister()
	{
		IntentFilter filter = new IntentFilter(Methods.Notify_Action);
		bbr = new ButtonBroadcastReceiver();
		registerReceiver(bbr, filter);
	}
	
	private void ReceiverUnregister()
	{
		if (bbr != null)
		{
			unregisterReceiver(bbr);
		}
	}

	private void ShowNotification()
	{
		Intent intent = new Intent();
		intent.setAction(Methods.Notify_Action);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification = new Notification.Builder(this);
		notification.setOngoing(true);
		notification.setSmallIcon(R.drawable.ic_notification);
		notification.setContentTitle(getString(R.string.app_name));
		notification.setContentText(getString(R.string.notify_mes_on));
		notification.setContentIntent(pendingintent);
		startForeground(Methods.Notify_ID, notification.build());
	}

	private void CloseNotification()
	{
		stopForeground(true);
	}

	private class ButtonBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			if (p2.getAction().equals(Methods.Notify_Action))
			{
				KeyBlocked = !KeyBlocked;
				sped.putBoolean("KeyBlocked", KeyBlocked);
				sped.commit();
				if (!QuickSettingControl)
				{
					if (KeyBlocked)
					{
						notification.setContentText(getString(R.string.notify_mes_off));
					}
					else
					{
						notification.setContentText(getString(R.string.notify_mes_on));
					}
					NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					nm.notify(Methods.Notify_ID, notification.build());
					Methods.collapseStatusBar(p1);
				}
			}
		}
	}
}
