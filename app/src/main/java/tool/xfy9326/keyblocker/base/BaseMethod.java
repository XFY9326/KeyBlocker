package tool.xfy9326.keyblocker.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.config.Config;

public class BaseMethod {
    public static List<PackageInfo> orderPackageList(final Context ctx, List<PackageInfo> list) {
        Collections.sort(list, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                String str1 = o1.applicationInfo.loadLabel(ctx.getPackageManager()).toString();
                String str2 = o2.applicationInfo.loadLabel(ctx.getPackageManager()).toString();
                return str1.compareTo(str2);
            }
        });
        return list;
    }

    public static ArrayList<String> StringToStringArrayList(String str) {
        ArrayList<String> arr = new ArrayList<>();
        if (str.contains("[") && str.length() >= 3) {
            str = str.substring(1, str.length() - 1);
            if (str.contains(",")) {
                String[] temp = str.split(",");
                for (int i = 0; i < temp.length; i++) {
                    if (i != 0) {
                        temp[i] = temp[i].substring(1, temp[i].length());
                    }
                    arr.add(temp[i]);
                }
            } else {
                arr.add(str);
            }
        }
        return arr;
    }

    public static void BlockNotify(Context ctx, boolean blocked) {
        if (!PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(Config.CONTROL_NOTIFICATION, false)) {
            if (blocked) {
                Toast.makeText(ctx, R.string.button_blocked, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, R.string.button_unblocked, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean isRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            int i = process.waitFor();
            if (i == 0) {
                Runtime.getRuntime().exec("su");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void RunAccessibilityService(Context context) {
        Toast.makeText(context, R.string.start_service_first, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }

    public static void RestartAccessibilityService(Context context) {
        if (isAccessibilitySettingsOn(context)) {
            Toast.makeText(context, R.string.restart_service, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        }
    }

    public static void KeyLockBroadcast(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(Config.NOTIFICATION_CLICK_ACTION);
        mContext.sendBroadcast(intent);
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }
        return false;
    }

    public static void collapseStatusBar(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.AUTO_CLOSE_STATUSBAR, true)) {
            try {
                //noinspection WrongConstant
                Object statusBarManager = context.getSystemService("statusbar");
                Method collapse;
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
                collapse.invoke(statusBarManager);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }
    }
}
