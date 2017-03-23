package tool.xfy9326.keyblocker.config;

/**
 * KeyBlocker.git
 * Created by frowhy on 2017/3/13.
 */

public class Config {
    public static int NOTIFICATION_ID = 5000;

    public static String APPWIDGET_CLICK_ACTION = "tool.xfy9326.keyblocker.action.APPWIDGET_CLICK_ACTION";
    public static String APPWIDGET_UPDATE_ACTION = "tool.xfy9326.keyblocker.action.APPWIDGET_UPDATE_ACTION";
    public static String REMOTE_CONTROL_ACTION = "tool.xfy9326.keyblocker.action.REMOTE_CONTROL_ACTION";
    public static String NOTIFICATION_CLICK_ACTION = "tool.xfy9326.keyblocker.Notification.OnClick";
    public static String NOTIFICATION_DELETE_ACTION = "tool.xfy9326.keyblocker.Notification.OnDelete";

    public static String ENABLED_CUSTOM_KEYCODE = "EnabledCustomKeycode";
    public static String ENABLED_VOLUME_KEY = "EnabledVolumeKey";
    public static String CUSTOM_KEYCODE = "CustomKeycode";
    public static String DISPLAY_KEYCODE = "DisplayKeycode";
    public static String ENABLED_KEYBLOCK = "EnabledKeyBlock";
    public static String DISPLAY_APPWIDGET = "DiaplayAppWidget";
    public static String ROOT_FUNCTION = "RootFunction";
    public static String BUTTON_VIBRATE = "ButtonVibrate";
    public static String DISPLAY_NOTIFICATION = "DisplayNotification";
	public static String NOTIFICATION_ICON = "NotificationIcon";
	public static String REMOVE_NOTIFICATION = "RemoveNotification";
	public static String AUTO_CLOSE_STATUSBAR = "AutoCloseStatusBar";

    public static String RUNTIME_BUTTONLIGHT_ON = "echo 100 > /sys/class/leds/button-backlight/brightness";
	public static String RUNTIME_BUTTONLIGHT_OFF = "echo 0 > /sys/class/leds/button-backlight/brightness";
	public static String RUNTIME_BUTTONLIGHT_CHMOD_STICK = "chmod 444 /sys/class/leds/button-backlight/brightness";
	public static String RUNTIME_BUTTONLIGHT_CHMOD_CHANGE = "chmod 644 /sys/class/leds/button-backlight/brightness";
    public static String RUNTIME_VIBRATE_ON = "echo 100 > /sys/class/timed_output/vibrator/vtg_level";
    public static String RUNTIME_VIBRATE_OFF = "echo 0 > /sys/class/timed_output/vibrator/vtg_level";
	public static String RUNTIME_VIBRATE_CHMOD_STICK = "chmod 444 /sys/class/timed_output/vibrator/vtg_level";
	public static String RUNTIME_VIBRATE_CHMOD_CHANGE = "chmod 644 /sys/class/timed_output/vibrator/vtg_level";

}
