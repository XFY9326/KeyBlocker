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
import java.util.ArrayList;
import java.util.Arrays;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static tool.xfy9326.keyblocker.R.string.app_name;

public class KeyBlockService extends AccessibilityService {
    private String lastBlockedApplication = "";
    private String mLastActivity = "";
    private String mLastPackage = "";
    private ServiceBroadcastReceiver serviceBroadcastReceiver;
    private Notification.Builder mNBuilder;
    private SharedPreferences mSp;
    private SharedPreferences.Editor mSpEditor;
    private Thread ActivityListener_Root = null;
    private Thread ActivityListener_Advanced = null;
    private boolean closeAdvancedFunction = false;
    private boolean AdvancedActivityListener = false;
    private boolean RootActivityListener = false;
    private boolean mIsQuickSetting = false;
    private NotificationManager mNM;
    private boolean isReceiverRegistered = false;
    private boolean isNotificationClosed = true;
    private boolean inRootMode = false;
    private boolean RootScanActivity = false;
    private boolean AdvancedScanActivity = false;
    private boolean allowBlockVibrator = false;
    private boolean allowRemoveNotification = false;
    private long backClickTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mSpEditor = mSp.edit();
        mSpEditor.apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra(Config.CLOSE_SERVICE, false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    disableSelf();
                }
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        closeAdvancedFunction = mSp.getBoolean(Config.CLOSE_ADVANCED_FUNCTIONS, false);
        if (!closeAdvancedFunction) {
            inRootMode = mSp.getBoolean(Config.ROOT_FUNCTION, false);
            RootScanActivity = mSp.getBoolean(Config.ROOT_SCAN_ACTIVITY, false);
            AdvancedScanActivity = mSp.getBoolean(Config.KEYBLOCK_ACTIVITY_ADVANCED_SCAN_MODE, false);
            allowBlockVibrator = mSp.getBoolean(Config.BUTTON_VIBRATE, false);
        }
        allowRemoveNotification = mSp.getBoolean(Config.REMOVE_NOTIFICATION, false);
        ControlModeSet();
        ReceiverRegister();
        if (!closeAdvancedFunction) {
            if (RootScanActivity) {
                RootGetActivitySet();
            } else if (AdvancedScanActivity) {
                AdvancedGetActivitySet();
            }
        }
        BaseMethod.BlockNotify(this, mSp.getBoolean(Config.ENABLED_KEYBLOCK, false));
        if (!mIsQuickSetting) {
            ShowNotification();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!closeAdvancedFunction && mSp.getBoolean(Config.KEYBLOCK_ACTIVITY, false) && !RootScanActivity && !AdvancedScanActivity) {
            int eventType = event.getEventType();
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getClassName() != null) {
                    CurrentActivityFix(event.getClassName().toString(), event.getPackageName().toString());
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        ReceiverUnregister();
        if (!closeAdvancedFunction) {
            if (ActivityListener_Root != null) {
                RootActivityListener = false;
                ActivityListener_Root = null;
            }
            if (ActivityListener_Advanced != null) {
                AdvancedActivityListener = false;
                ActivityListener_Advanced = null;
            }
        }
        boolean mIsKeyBlocked = mSp.getBoolean(Config.ENABLED_KEYBLOCK, false);
        if (mIsKeyBlocked) {
            if (!closeAdvancedFunction) {
                ButtonLightControl(false);
                ButtonVibrateControl(false);
            }
            BaseMethod.BlockNotify(this, false);
            mSpEditor.putBoolean(Config.ENABLED_KEYBLOCK, false);
            mSpEditor.commit();
        }
    }

    @Override
    public void onDestroy() {
        ReceiverUnregister();
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        ReceiverUnregister();
        if (!closeAdvancedFunction) {
            if (ActivityListener_Root != null) {
                RootActivityListener = false;
                ActivityListener_Root = null;
            }
            if (ActivityListener_Advanced != null) {
                AdvancedActivityListener = false;
                ActivityListener_Advanced = null;
            }
            ButtonLightControl(false);
            ButtonVibrateControl(false);
            mLastActivity = "";
            mLastPackage = "";
            lastBlockedApplication = "";
        }
        if (!mIsQuickSetting) {
            CloseNotification();
        }
        return super.onUnbind(intent);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();
        if (keycode == KeyEvent.KEYCODE_POWER) {
            return false;
        }
        boolean enable_keyblock = mSp.getBoolean(Config.ENABLED_KEYBLOCK, false);
        if (closeAdvancedFunction) {
            return enable_keyblock;
        }
        if (event.getAction() == ACTION_DOWN) {
            if (keycode == KeyEvent.KEYCODE_BACK) {
                if (mSp.getBoolean(Config.DOUBLE_CLICK_EXIT, false) && enable_keyblock) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - backClickTime <= 700) {
                        CloseBlock();
                        backClickTime = 0;
                        return true;
                    } else {
                        backClickTime = nowTime;
                    }
                }
            }
            if (mSp.getBoolean(Config.DISPLAY_KEYCODE, false)) {
                Toast.makeText(this, "Keycode: " + keycode, Toast.LENGTH_SHORT).show();
            }
        }
        if (enable_keyblock) {
            if (mSp.getBoolean(Config.ENABLED_VOLUME_KEY, false) && (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_MUTE || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                return false;
            }
            if (mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false)) {
                String[] sourceStrArray = mSp.getString(Config.CUSTOM_KEYCODE, "").split(" ");
                Arrays.sort(sourceStrArray);
                int index = Arrays.binarySearch(sourceStrArray, String.valueOf(keycode));
                boolean isDisabled = index >= 0;
                if (event.getAction() == ACTION_UP && mSp.getBoolean(Config.DISPLAY_KEYCODE, false)) {
                    if (isDisabled) {
                        Toast.makeText(this, "KeyCode: " + keycode + " " + getString(R.string.has_disabled), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "KeyCode: " + keycode, Toast.LENGTH_SHORT).show();
                    }
                }
                return isDisabled;
            }
            return true;
        } else {
            return false;
        }
    }

    private void AdvancedGetActivitySet() {
        if (mSp.getBoolean(Config.KEYBLOCK_ACTIVITY, false)) {
            if (ActivityListener_Advanced != null) {
                AdvancedActivityListener = false;
                try {
                    if (ActivityListener_Advanced.isAlive()) {
                        ActivityListener_Advanced.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ActivityListener_Advanced = null;
            }
            AdvancedActivityListener = true;
            ActivityListener_Advanced = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final int default_sleep_time = 1250;
                        final int wait_sleep_time = 3000;
                        while (AdvancedActivityListener) {
                            if (BaseMethod.isScreenOn(KeyBlockService.this)) {
                                String[] data = BaseMethod.getCurrentPackageByManager(KeyBlockService.this);
                                if (data != null && data.length == 2) {
                                    CurrentActivityFix(data[0], data[1]);
                                }
                                Thread.sleep(default_sleep_time);
                            } else {
                                Thread.sleep(wait_sleep_time);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            ActivityListener_Advanced.start();
        }
    }

    private void RootGetActivitySet() {
        if (mSp.getBoolean(Config.KEYBLOCK_ACTIVITY, false) && inRootMode) {
            if (ActivityListener_Root != null) {
                RootActivityListener = false;
                try {
                    if (ActivityListener_Root.isAlive()) {
                        ActivityListener_Root.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ActivityListener_Root = null;
            }
            RootActivityListener = true;
            ActivityListener_Root = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final int default_sleep_time = 1250;
                        final int wait_sleep_time = 3000;
                        while (RootActivityListener) {
                            if (BaseMethod.isScreenOn(KeyBlockService.this)) {
                                String[] data = BaseMethod.getCurrentActivityByRoot();
                                if (data != null && data.length == 2) {
                                    CurrentActivityFix(data[0], data[1]);
                                }
                                Thread.sleep(default_sleep_time);
                            } else {
                                Thread.sleep(wait_sleep_time);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            ActivityListener_Root.start();
        }
    }

    private void CurrentActivityFix(String mCurrentActivity, String mCurrentPackage) {
        if (mCurrentActivity != null && mCurrentPackage != null && !mCurrentActivity.equals("") && !mCurrentPackage.equals("")) {
            boolean useful_activity = true;
            if (mCurrentPackage.length() >= 7) {
                useful_activity = !(mCurrentActivity.startsWith("android") || mCurrentPackage.equalsIgnoreCase("com.android.systemui"));
            }
            if (useful_activity) {
                if (mLastActivity != null && !mLastActivity.equalsIgnoreCase(mCurrentActivity) && mLastPackage != null && !mLastPackage.equalsIgnoreCase(mCurrentPackage)) {
                    if (mSp.getBoolean(Config.KEYBLOCK_ACTIVITY_TEST, false)) {
                        sendBroadcast(new Intent().setAction(Config.ACTIVITY_AND_PACKAGE_TEST_ACTION).putExtra(Config.TEST_APP_NAME, mCurrentPackage + " / " + mCurrentActivity));
                    }
                    currentUseCheck(mCurrentActivity, mCurrentPackage);
                    mLastActivity = mCurrentActivity;
                    mLastPackage = mCurrentPackage;
                }
            }
        }
    }

    private void CloseBlock() {
        if (!closeAdvancedFunction) {
            ButtonLightControl(false);
            ButtonVibrateControl(false);
        }
        mSpEditor.putBoolean(Config.ENABLED_KEYBLOCK, false);
        mSpEditor.commit();
        UiUpdater(this, true, false);
    }

    private void currentUseCheck(String using_app_pkg_name, String using_app_class_name) {
        if (!using_app_pkg_name.contains(getPackageName())) {
            if (mSp.getBoolean(Config.RECENT_BLOCK_REMEMBER, false) && lastBlockedApplication.equals(using_app_pkg_name)) {
                findActivity(true);
            }
            if (!AdvancedScanActivity || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                String KeyWordsString = mSp.getString(Config.CUSTOM_KEYBLOCK_ACTIVITY_KEY_WORDS, "");
                if (!KeyWordsString.equals("")) {
                    String[] keywords;
                    if (KeyWordsString.contains(" ")) {
                        keywords = KeyWordsString.split(" ");
                    } else {
                        keywords = new String[]{KeyWordsString};
                    }
                    boolean ActivityFound = false;
                    for (String str : keywords) {
                        if (!str.equals("") && using_app_class_name.contains(str)) {
                            ActivityFound = true;
                            break;
                        }
                    }
                    findActivity(ActivityFound);
                }
            }
            String ActivityString = mSp.getString(Config.CUSTOM_KEYBLOCK_ACTIVITY, Config.EMPTY_ARRAY);
            if (!ActivityString.equals(Config.EMPTY_ARRAY)) {
                ArrayList<String> ActivityArray = BaseMethod.StringToStringArrayList(ActivityString);
                if (!ActivityArray.isEmpty()) {
                    boolean ActivityFound = false;
                    for (String FilterActivity : ActivityArray) {
                        if (using_app_pkg_name.contains(FilterActivity)) {
                            ActivityFound = true;
                            break;
                        }
                    }
                    findActivity(ActivityFound);
                }
            }
        }
    }

    private void findActivity(boolean ActivityFound) {
        if (ActivityFound) {
            if (!mSp.getBoolean(Config.ENABLED_KEYBLOCK, false)) {
                BaseMethod.KeyLockBroadcast(this, true);
            }
        } else {
            if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, false)) {
                BaseMethod.KeyLockBroadcast(this, true);
            }
        }
    }

    private void ControlModeSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!mSp.getBoolean(Config.DISPLAY_NOTIFICATION, false)) {
                mIsQuickSetting = true;
            }
        }
    }

    private void ButtonLightControl(final boolean NotInControl) {
        if (inRootMode && allowBlockVibrator) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Process process = Runtime.getRuntime().exec("su");
                        DataOutputStream mRuntimeStream = new DataOutputStream(process.getOutputStream());
                        mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_CHMOD_CHANGE + "\n");
                        if (NotInControl) {
                            mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_OFF + "\n");
                            mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_CHMOD_STICK + "\n");
                        } else {
                            mRuntimeStream.writeBytes(Config.RUNTIME_BUTTONLIGHT_ON + "\n");
                        }
                        mRuntimeStream.flush();
                        process.waitFor();
                        process.getErrorStream().close();
                        mRuntimeStream.close();
                        process.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void ButtonVibrateControl(final boolean NotInControl) {
        if (inRootMode && allowBlockVibrator) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Process process = Runtime.getRuntime().exec("su");
                        DataOutputStream mRuntimeStream = new DataOutputStream(process.getOutputStream());
                        mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_CHMOD_CHANGE + "\n");
                        if (NotInControl) {
                            mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_OFF + "\n");
                            mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_CHMOD_STICK + "\n");
                            mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_CHMOD_AVOIDCHANGE_STICK + "\n");
                        } else {
                            mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_ON + "\n");
                            mRuntimeStream.writeBytes(Config.RUNTIME_VIBRATE_CHMOD_AVOIDCHANGE_CHANGE + "\n");
                        }
                        mRuntimeStream.flush();
                        process.waitFor();
                        process.getErrorStream().close();
                        mRuntimeStream.close();
                        process.destroy();
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
        filter.addAction(Config.ACTIVITY_AND_PACKAGE_TEST_ACTION);
        serviceBroadcastReceiver = new ServiceBroadcastReceiver();
        isReceiverRegistered = true;
        registerReceiver(serviceBroadcastReceiver, filter);
    }

    private void ReceiverUnregister() {
        if (serviceBroadcastReceiver != null && isReceiverRegistered) {
            isReceiverRegistered = false;
            unregisterReceiver(serviceBroadcastReceiver);
        }
    }

    private void ShowNotification() {
        Intent click_intent = new Intent(Config.NOTIFICATION_CLICK_ACTION);
        click_intent.putExtra(Config.DISPLAY_APPWIDGET, true);
        PendingIntent click_pendingIntent = PendingIntent.getBroadcast(this, 0, click_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent delete_intent = new Intent(Config.NOTIFICATION_DELETE_ACTION);
        PendingIntent delete_pendingIntent = PendingIntent.getBroadcast(this, 0, delete_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNBuilder = new Notification.Builder(this);
        mNBuilder.setOngoing(true);
        if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, false)) {
            mNBuilder.setSmallIcon(R.drawable.ic_notification_blocked);
        } else {
            mNBuilder.setSmallIcon(R.drawable.ic_notification_not_blocked);
        }
        mNBuilder.setContentTitle(getString(app_name));
        if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, false)) {
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
        mNBuilder.setDeleteIntent(delete_pendingIntent);
        mNBuilder.setContentIntent(click_pendingIntent);
        if (!mSp.getBoolean(Config.NOTIFICATION_ICON, true)) {
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

    private void UiUpdater(Context content, boolean updateAppWidget, boolean mIsKeyBlocked) {
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
            isNotificationClosed = false;
            if (mIsKeyBlocked) {
                mNBuilder.setSmallIcon(R.drawable.ic_notification_blocked);
            } else {
                mNBuilder.setSmallIcon(R.drawable.ic_notification_not_blocked);
            }
            mNM.notify(Config.NOTIFICATION_ID, mNBuilder.build());
            BaseMethod.collapseStatusBar(content);
        }
        if (updateAppWidget) {
            sendBroadcast(new Intent(Config.APPWIDGET_UPDATE_ACTION));
        }
        BaseMethod.BlockNotify(content, mIsKeyBlocked);
    }

    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        private boolean mIsKeyBlocked;

        @Override
        public void onReceive(Context content, Intent intent) {
            if (intent.getAction().equals(Config.NOTIFICATION_CLICK_ACTION)) {
                BlockAction(content, intent.getBooleanExtra(Config.DISPLAY_APPWIDGET, false));
            } else if (intent.getAction().equals(Config.NOTIFICATION_DELETE_ACTION)) {
                if (allowRemoveNotification) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        disableSelf();
                    } else if (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) && mSp.getBoolean(Config.ROOT_FUNCTION, false)) {
                        BaseMethod.controlAccessibilityServiceWithRoot(false, false);
                    }
                }
                stopSelf();
            } else if (intent.getAction().equals(Config.ACTIVITY_AND_PACKAGE_TEST_ACTION)) {
                String data = intent.getStringExtra(Config.TEST_APP_NAME);
                if (data != null) {
                    Toast.makeText(content, data, Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void BlockAction(Context context, boolean updateWidget) {
            mIsKeyBlocked = !mSp.getBoolean(Config.ENABLED_KEYBLOCK, false);
            ButtonLightControl(mIsKeyBlocked);
            ButtonVibrateControl(mIsKeyBlocked);
            if (mIsKeyBlocked) {
                lastBlockedApplication = mLastPackage;
            }
            mSpEditor.putBoolean(Config.ENABLED_KEYBLOCK, mIsKeyBlocked);
            mSpEditor.commit();
            UiUpdater(context, updateWidget, mIsKeyBlocked);
        }
    }
}
