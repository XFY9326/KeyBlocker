package tool.xfy9326.keyblocker.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.util.Arrays;
import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

import static android.view.KeyEvent.ACTION_UP;

public class KeyBlockService extends AccessibilityService {
	private ButtonBroadcastReceiver mBbr;
	private Notification.Builder mNBuilder;
	private SharedPreferences mSp;
	private SharedPreferences.Editor mSpEditor;
	private boolean mIsQuickSetting = false;
	private NotificationManager mNM;
	private Runtime mRuntime;
	private Process mProcess;
	private DataOutputStream mRuntimeStream;
	private boolean isReceiverRegistered = false;
	private boolean isNotificationClosed = true;
	private boolean inRootMode = false;
	private boolean allowBlockVibrator = false;
	private boolean allowRemoveNotification = false;

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mSp = PreferenceManager.getDefaultSharedPreferences(this);
		mSpEditor = mSp.edit();
		mSpEditor.apply();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		inRootMode = mSp.getBoolean(Config.ROOT_FUNCTION, false);
		allowBlockVibrator = mSp.getBoolean(Config.BUTTON_VIBRATE, false);
		allowRemoveNotification = mSp.getBoolean(Config.REMOVE_NOTIFICATION, false);
		getRoot();
		ControlModeSet();
		ReceiverRegister();
		BaseMethod.BlockNotify(this, mSp.getBoolean(Config.ENABLED_KEYBLOCK, true));
		if (!mIsQuickSetting) {
			ShowNotification();
		}
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
	}

	@Override
	public void onInterrupt() {
		ReceiverUnregister();
		ButtonLightControl(false);
		ButtonVibrateControl(false);
	}

	@Override
	public void onDestroy() {
		ReceiverUnregister();
		ButtonLightControl(false);
		ButtonVibrateControl(false);
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		ReceiverUnregister();
		ButtonLightControl(false);
		ButtonVibrateControl(false);
		if (!mIsQuickSetting) {
			CloseNotification();
		}
		if (inRootMode) {
			BaseMethod.closeRuntime(mProcess, mRuntimeStream);
		}
		return super.onUnbind(intent);
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		int keycode = event.getKeyCode();
		if (event.getAction() == ACTION_UP && mSp.getBoolean(Config.DISPLAY_KEYCODE, false)) {
			Toast.makeText(this, "Keycode: " + keycode, Toast.LENGTH_SHORT).show();
		}
		if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
			if (mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false)) {
				String[] sourceStrArray = mSp.getString(Config.CUSTOM_KEYCODE, "").split(" ");
				Arrays.sort(sourceStrArray);
				int index = Arrays.binarySearch(sourceStrArray, String.valueOf(keycode));

				boolean isDisabled = index >= 0;

				if (event.getAction() == ACTION_UP && mSp.getBoolean(Config.DISPLAY_KEYCODE, false)) {
					if (isDisabled) {
						Toast.makeText(this, "Keycode: " + keycode + " " + getString(R.string.has_disabled), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "Keycode: " + keycode, Toast.LENGTH_SHORT).show();
					}
				}
				return isDisabled;
			} else if (mSp.getBoolean(Config.ENABLED_VOLUME_KEY, false) && (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_MUTE || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
				return false;
			}
			return mSp.getBoolean(Config.ENABLED_KEYBLOCK, true);
		} else {
			return false;
		}
	}

	private void ControlModeSet() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			if (!mSp.getBoolean(Config.DISPLAY_NOTIFICATION, false)) {
				mIsQuickSetting = true;
			}
		}
	}

	private void getRoot() {
		if (inRootMode) {
			mRuntime = Runtime.getRuntime();
			mProcess = BaseMethod.getRootProcess(mRuntime);
			mRuntimeStream = BaseMethod.getStream(mProcess);
		}
	}

	private void ButtonLightControl(final boolean close) {
		if (inRootMode && mRuntime != null && mProcess != null && mRuntimeStream != null) {
			new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_CHMOD_CHANGE + "\n");
							if (close) {
								mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_OFF + "\n");
								mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_CHMOD_STICK + "\n");
							} else {
								mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_ON + "\n");
							}
							mRuntimeStream.flush();
							mProcess.waitFor();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
		}
	}

	private void ButtonVibrateControl(final boolean close) {
		if (inRootMode && allowBlockVibrator && mRuntime != null && mProcess != null && mRuntimeStream != null) {
			new Thread(new Runnable() {
					public void run() {
						try {
							mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_CHMOD_CHANGE + "\n");
							if (close) {
								mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_OFF + "\n");
								mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_CHMOD_STICK + "\n");
							} else {
								mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_ON + "\n");
							}
							mRuntimeStream.flush();
							mProcess.waitFor();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
		}
	}

	private void ReceiverRegister() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.NOTIFICATION_CLICK_ACTION);
		filter.addAction(Config.NOTIFICATION_DELETE_ACTION);
		mBbr = new ButtonBroadcastReceiver();
		isReceiverRegistered = true;
		registerReceiver(mBbr, filter);
	}

	private void ReceiverUnregister() {
		if (mBbr != null && isReceiverRegistered) {
			isReceiverRegistered = false;
			unregisterReceiver(mBbr);
		}
	}

	private void ShowNotification() {
		Intent click_intent = new Intent(Config.NOTIFICATION_CLICK_ACTION);
		click_intent.putExtra(Config.DISPLAY_APPWIDGET, true);
		PendingIntent click_pendingIntent = PendingIntent.getBroadcast(this, 0, click_intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent delete_intent = new Intent(Config.NOTIFICATION_DELETE_ACTION);
		PendingIntent delete_pendingintent = PendingIntent.getBroadcast(this, 0, delete_intent, PendingIntent.FLAG_UPDATE_CURRENT);

		mNBuilder = new Notification.Builder(this);
		mNBuilder.setOngoing(true);
		mNBuilder.setSmallIcon(R.drawable.ic_notification);
		mNBuilder.setContentTitle(getString(R.string.app_name));
		if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
			ButtonLightControl(true);
			ButtonVibrateControl(true);
			if (allowRemoveNotification) {
				mNBuilder.setOngoing(true);
			}
			mNBuilder.setContentText(getString(R.string.notify_mes_off));
		} else {
			ButtonLightControl(false);
			ButtonVibrateControl(false);
			if (allowRemoveNotification) {
				mNBuilder.setOngoing(false);
			}
			mNBuilder.setContentText(getString(R.string.notify_mes_on));
		}
		mNBuilder.setDeleteIntent(delete_pendingintent);
		mNBuilder.setContentIntent(click_pendingIntent);
		if (!mSp.getBoolean(Config.NOTIFICATION_ICON, false)) {
			mNBuilder.setPriority(Notification.PRIORITY_MIN);
		}
		isNotificationClosed = false;
		mNM.notify(Config.NOTIFICATION_ID, mNBuilder.build());
	}

	private void CloseNotification() {
		if (!isNotificationClosed) {
			isNotificationClosed = true;
			mNM.cancel(Config.NOTIFICATION_ID);
		}
	}

	private class ButtonBroadcastReceiver extends BroadcastReceiver {
		private boolean mIsKeyBlocked;

		@Override
		public void onReceive(Context content, Intent intent) {
			if (intent.getAction().equals(Config.NOTIFICATION_CLICK_ACTION)) {
				mIsKeyBlocked = !mSp.getBoolean(Config.ENABLED_KEYBLOCK, true);
				BaseMethod.BlockNotify(content, mIsKeyBlocked);
				ButtonLightControl(mIsKeyBlocked);
				ButtonVibrateControl(mIsKeyBlocked);
				mSpEditor.putBoolean(Config.ENABLED_KEYBLOCK, mIsKeyBlocked);
				mSpEditor.commit();
				if (!mIsQuickSetting) {
					if (mIsKeyBlocked) {
						if (allowRemoveNotification) {
							mNBuilder.setOngoing(true);
						}
						mNBuilder.setContentText(getString(R.string.notify_mes_off));
					} else {
						if (allowRemoveNotification) {
							mNBuilder.setOngoing(false);
						}
						mNBuilder.setContentText(getString(R.string.notify_mes_on));
					}
					if (intent.getBooleanExtra(Config.DISPLAY_APPWIDGET, false)) {
						sendBroadcast(new Intent(Config.APPWIDGET_UPDATE_ACTION));
					}
					isNotificationClosed = false;
					mNM.notify(Config.NOTIFICATION_ID, mNBuilder.build());
					BaseMethod.collapseStatusBar(content);
				}
			} else if (intent.getAction().equals(Config.NOTIFICATION_DELETE_ACTION)) {
				if (allowRemoveNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					disableSelf();
					stopSelf();
				}
			}
		}
	}
}
