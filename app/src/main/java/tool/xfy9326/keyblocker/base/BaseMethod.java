package tool.xfy9326.keyblocker.base;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.config.Config;
import android.preference.PreferenceManager;

public class BaseMethod {
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

	 public static Process getRootProcess(Runtime r) {
		  Process p;
		  try {
			   p = r.exec("su");
		  } catch (IOException e1) {
			   e1.printStackTrace();
			   p = null;
		  }
		  return p;
	 }

	 public static DataOutputStream getStream(Process p) {
		  if (p != null) {
			   DataOutputStream o = new DataOutputStream(p.getOutputStream());
			   return o;
		  }
		  return null;
	 }

	 public static void closeRuntime(Process p, DataOutputStream o) {
		  try {
			   if (o != null) {
					o.writeBytes("exit\n");
					o.close();
			   }
			   p.destroy();
		  } catch (Exception e) {
			   e.printStackTrace();
		  }
	 }

	 public static void RunAccessibilityService(Context context) {
		  Toast.makeText(context, R.string.start_service_first, Toast.LENGTH_SHORT).show();
		  Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
		  context.startActivity(intent);
	 }

	 public static void RestartAccessibilityService(Context context) {
		  Toast.makeText(context, R.string.restart_service, Toast.LENGTH_SHORT).show();
		  Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
		  context.startActivity(intent);
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
