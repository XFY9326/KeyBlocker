package tool.xfy9326.keyblocker.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.PowerManager;
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

    public static String getCurrentActivity() throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        BufferedWriter mOutputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader mInputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        mOutputWriter.write(Config.RUNTIME_GET_CURRENT_ACTIVITY + "\n");
        mOutputWriter.flush();
        String result = mInputReader.readLine();
        //process.waitFor();
        mOutputWriter.close();
        mInputReader.close();
        process.getErrorStream().close();
        process.destroy();
        if (!TextUtils.isEmpty(result)) {
            result = result.substring(result.indexOf("{"), result.lastIndexOf("}"));
            String[] data = result.split(" ");
            String act_data = data[2];
            return act_data.substring(0, act_data.lastIndexOf("/"));
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
