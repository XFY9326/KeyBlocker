package tool.xfy9326.keyblocker.config;

/**
 * KeyBlocker.git
 * Created by frowhy on 2017/3/13.
 */

public class Config {
    public static final int NOTIFICATION_ID = 5000;

    public static final String EMPTY_ARRAY = "[]";

    public static final String APPWIDGET_CLICK_ACTION = "tool.xfy9326.keyblocker.action.APPWIDGET_CLICK_ACTION";
    public static final String APPWIDGET_UPDATE_ACTION = "tool.xfy9326.keyblocker.action.APPWIDGET_UPDATE_ACTION";
    public static final String REMOTE_CONTROL_ACTION = "tool.xfy9326.keyblocker.action.REMOTE_CONTROL_ACTION";
    public static final String NOTIFICATION_CLICK_ACTION = "tool.xfy9326.keyblocker.Notification.OnClick";
    public static final String NOTIFICATION_DELETE_ACTION = "tool.xfy9326.keyblocker.Notification.OnDelete";

    public static final String ENABLED_CUSTOM_KEYCODE = "EnabledCustomKeycode";
    public static final String CUSTOM_SETTINGS = "CustomSettings";
    public static final String ENABLED_VOLUME_KEY = "EnabledVolumeKey";
    public static final String CUSTOM_KEYCODE = "CustomKeycode";
    public static final String DISPLAY_KEYCODE = "DisplayKeycode";
    public static final String ENABLED_KEYBLOCK = "EnabledKeyBlock";
    public static final String DISPLAY_APPWIDGET = "DisplayAppWidget";
    public static final String ROOT_FUNCTION = "RootFunction";
    public static final String BUTTON_VIBRATE = "ButtonVibrate";
    public static final String DISPLAY_NOTIFICATION = "DisplayNotification";
    public static final String NOTIFICATION_ICON = "NotificationIcon";
    public static final String REMOVE_NOTIFICATION = "RemoveNotification";
    public static final String AUTO_CLOSE_STATUSBAR = "AutoCloseStatusBar";
    public static final String KEYBLOCK_ACTIVITY = "KeyBlockActivity";
    public static final String KEYBLOCK_ACTIVITY_SET = "KeyBlockActivitySet";
    public static final String CUSTOM_KEYBLOCK_ACTIVITY = "CustomKeyBlockActivity";
    public static final String CONTROL_NOTIFICATION = "ControlNotification";
    public static final String KEYBLOCK_ACTIVITY_FILTER = "KeyBlockActivityFilter";

    public static final String RUNTIME_BUTTONLIGHT_ON = "echo 100 > /sys/class/leds/button-backlight/brightness";
    public static final String RUNTIME_BUTTONLIGHT_OFF = "echo 0 > /sys/class/leds/button-backlight/brightness";
    public static final String RUNTIME_BUTTONLIGHT_CHMOD_STICK = "chmod 444 /sys/class/leds/button-backlight/brightness";
    public static final String RUNTIME_BUTTONLIGHT_CHMOD_CHANGE = "chmod 644 /sys/class/leds/button-backlight/brightness";
    public static final String RUNTIME_VIBRATE_ON = "echo 100 > /sys/class/timed_output/vibrator/vtg_level";
    public static final String RUNTIME_VIBRATE_OFF = "echo 0 > /sys/class/timed_output/vibrator/vtg_level";
    public static final String RUNTIME_VIBRATE_CHMOD_STICK = "chmod 444 /sys/class/timed_output/vibrator/vtg_level";
    public static final String RUNTIME_VIBRATE_CHMOD_CHANGE = "chmod 644 /sys/class/timed_output/vibrator/vtg_level";
    public static final String RUNTIME_VIBRATE_CHMOD_AVOIDCHANGE_STICK = "chmod 444 /sys/class/timed_output/vibrator/enable";
    public static final String RUNTIME_VIBRATE_CHMOD_AVOIDCHANGE_CHANGE = "chmod 644 /sys/class/timed_output/vibrator/enable";

}
