package tool.xfy9326.keyblocker.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.activity.SettingsActivity;
import tool.xfy9326.keyblocker.config.Config;

public class BaseMethod {

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        } else {
            //noinspection deprecation
            return powerManager.isScreenOn();
        }
    }

    public static void showPhoneStatAlert(final Activity activity, final CheckBoxPreference checkBoxPreference, final SettingsActivity.PrefsFragment prefsFragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(R.string.keyblock_activity_advanced_scan_mode);
        builder.setMessage(R.string.keyblock_activity_advanced_scan_mode_warn);
        builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (hasPhoneStatPermission(activity)) {
                        prefsFragment.findPreference(Config.KEYBLOCK_ACTIVITY_TEXT_SET).setEnabled(false);
                    } else {
                        getPhoneStatPermission(activity, Config.REQUEST_CODE_READ_PHONE_STAT);
                    }
                } else {
                    prefsFragment.findPreference(Config.KEYBLOCK_ACTIVITY_TEXT_SET).setEnabled(false);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkBoxPreference.setChecked(false);
            }
        });
        builder.show();
    }

    public static String[] getCurrentPackageByManager(Context context) {
        String pkg_name = null;
        String act_name = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager != null) {
                long now = System.currentTimeMillis();
                List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 6000, now);
                if (stats != null && !stats.isEmpty()) {
                    int top = 0;
                    for (int last = 0; last < stats.size(); last++) {
                        if (stats.get(last).getLastTimeUsed() > stats.get(top).getLastTimeUsed()) {
                            top = last;
                        }
                    }
                    pkg_name = stats.get(top).getPackageName();
                }
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
            List list = activityManager.getRunningTasks(1);
            pkg_name = ((ActivityManager.RunningTaskInfo) list.get(0)).topActivity.getPackageName();
            act_name = ((ActivityManager.RunningTaskInfo) list.get(0)).topActivity.getClassName();
        }
        if (pkg_name == null) {
            pkg_name = "";
        }
        if (act_name == null) {
            act_name = pkg_name;
        }
        return new String[]{pkg_name, act_name};
    }

    public static boolean hasPhoneStatPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        }
        return false;
    }

    private static void getPhoneStatPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            activity.startActivityForResult(new Intent().setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS), requestCode);
        }
    }

    public static String[] getCurrentActivityByRoot() throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        BufferedWriter mOutputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader mInputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        mOutputWriter.write(Config.RUNTIME_GET_CURRENT_ACTIVITY + "\n");
        mOutputWriter.flush();
        String result = mInputReader.readLine();
        mOutputWriter.close();
        mInputReader.close();
        process.getErrorStream().close();
        process.destroy();
        if (!TextUtils.isEmpty(result)) {
            result = result.substring(result.indexOf("{"), result.lastIndexOf("}"));
            String[] data = result.split(" ");
            String act_data = data[2];
            String pkg_data;
            if (act_data.contains("/")) {
                pkg_data = act_data.substring(0, act_data.indexOf("/"));
                act_data = act_data.replace("/", "");
            } else {
                pkg_data = act_data;
            }
            return new String[]{pkg_data, act_data};
        }
        return null;
    }

    public static void controlAccessibilityServiceWithRoot(final boolean isOpen, final boolean isRestart) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream mRuntimeStream = new DataOutputStream(process.getOutputStream());
                    mRuntimeStream.writeBytes(Config.RUNTIME_ROOT_OPEN_SERVICE_REGISTER + "\n");
                    if (isRestart) {
                        mRuntimeStream.writeBytes(Config.RUNTIME_ROOT_OPEN_SERVICE_CLOSE_REFRESH + "\n");
                        mRuntimeStream.writeBytes(Config.RUNTIME_ROOT_OPEN_SERVICE_OPEN_REFRESH + "\n");
                    } else if (isOpen) {
                        mRuntimeStream.writeBytes(Config.RUNTIME_ROOT_OPEN_SERVICE_OPEN_REFRESH + "\n");
                    } else {
                        mRuntimeStream.writeBytes(Config.RUNTIME_ROOT_OPEN_SERVICE_CLOSE_REFRESH + "\n");
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

    public static void orderPackageList(final Context ctx, List<PackageInfo> list) {
        Collections.sort(list, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                String str1 = o1.applicationInfo.loadLabel(ctx.getPackageManager()).toString();
                String str2 = o2.applicationInfo.loadLabel(ctx.getPackageManager()).toString();
                str1 = HanziToPinyin.getInstance().convert(str1.toLowerCase());
                str2 = HanziToPinyin.getInstance().convert(str2.toLowerCase());
                return str1.compareTo(str2);
            }
        });
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

    public static void getRoot() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    process.getOutputStream().write("exit\n".getBytes());
                    process.getOutputStream().flush();
                    process.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static boolean isRooted_old() {
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

    public static boolean isRooted() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";
        return new File(binPath).exists() && isExecutable(binPath) || new File(xBinPath).exists() && isExecutable(xBinPath);
    }

    private static boolean isExecutable(String filePath) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String str = in.readLine();
            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                if (flag == 's' || flag == 'x')
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
        return false;
    }

    public static void RunAccessibilityService(Context context) {
        SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(context);
        if (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) && mSp.getBoolean(Config.ROOT_FUNCTION, false)) {
            controlAccessibilityServiceWithRoot(true, false);
        } else {
            Toast.makeText(context, R.string.start_service_first, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void RestartAccessibilityService(Context context) {
        if (isAccessibilitySettingsOn(context)) {
            SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(context);
            if (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) && mSp.getBoolean(Config.ROOT_FUNCTION, false)) {
                controlAccessibilityServiceWithRoot(false, true);
            } else {
                Toast.makeText(context, R.string.restart_service, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                context.startActivity(intent);
            }
        }
    }

    public static void KeyLockBroadcast(Context mContext, boolean updateWidget, boolean isAuto) {
        Intent intent = new Intent();
        intent.setAction(Config.NOTIFICATION_CLICK_ACTION);
        intent.putExtra(Config.DISPLAY_APPWIDGET, updateWidget);
        intent.putExtra(Config.AUTO_BLOCK, isAuto);
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
