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

    @Override
    public void onCreate() {
        super.onCreate();
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mSpEditor = mSp.edit();
        mSpEditor.apply();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        ControlModeSet();
        ReceiverRegister();
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
        if (!mIsQuickSetting) {
            CloseNotification();
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();

        if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
            if (mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false)) {
                if (mSp.getBoolean(Config.DISABLED_VOLUME_KEY, false) && (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_MUTE || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    return true;
                }
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
            }
        }
        if (event.getAction() == ACTION_UP && mSp.getBoolean(Config.DISPLAY_KEYCODE, false)) {
            Toast.makeText(this, "Keycode: " + keycode, Toast.LENGTH_SHORT).show();
        }
        return mSp.getBoolean(Config.ENABLED_KEYBLOCK, true);
    }

    private void ControlModeSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!mSp.getBoolean(Config.DISPLAY_NOTIFICATION, false)) {
                mIsQuickSetting = true;
            }
        }
    }

    private void ReceiverRegister() {
        IntentFilter filter = new IntentFilter(Config.NOTIFICATION_ACTION);
        mBbr = new ButtonBroadcastReceiver();
        registerReceiver(mBbr, filter);
    }

    private void ReceiverUnregister() {
        if (mBbr != null) {
            unregisterReceiver(mBbr);
        }
    }

    @Override
    public void onDestroy() {
        ReceiverUnregister();
        super.onDestroy();
    }

    private void ShowNotification() {
        Intent intent = new Intent(Config.NOTIFICATION_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNBuilder = new Notification.Builder(this);
        mNBuilder.setOngoing(true);
        mNBuilder.setSmallIcon(R.drawable.ic_notification);
        mNBuilder.setContentTitle(getString(R.string.app_name));
        if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
            mNBuilder.setContentText(getString(R.string.notify_mes_off));
        } else {
            mNBuilder.setContentText(getString(R.string.notify_mes_on));
        }
        mNBuilder.setContentIntent(pendingIntent);
        mNBuilder.setPriority(Notification.PRIORITY_MIN);
        startForeground(Config.NOTIFICATION_ID, mNBuilder.build());
    }

    private void CloseNotification() {
        stopForeground(true);
    }

    private class ButtonBroadcastReceiver extends BroadcastReceiver {
        private boolean mIsKeyBlocked;

        @Override
        public void onReceive(Context content, Intent intent) {
            if (intent.getAction().equals(Config.NOTIFICATION_ACTION)) {
                mIsKeyBlocked = !mSp.getBoolean(Config.ENABLED_KEYBLOCK, true);
                mSpEditor.putBoolean(Config.ENABLED_KEYBLOCK, mIsKeyBlocked);
                mSpEditor.commit();
                if (!mIsQuickSetting) {
                    if (mIsKeyBlocked) {
                        mNBuilder.setContentText(getString(R.string.notify_mes_off));
                    } else {
                        mNBuilder.setContentText(getString(R.string.notify_mes_on));
                    }
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(Config.NOTIFICATION_ID, mNBuilder.build());
                    BaseMethod.collapseStatusBar(content);
                }
            }
        }
    }
}
