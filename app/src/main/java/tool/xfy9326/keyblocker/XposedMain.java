package tool.xfy9326.keyblocker;

import android.os.Build;
import android.view.KeyEvent;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import tool.xfy9326.keyblocker.config.Config;

/**
 * Block
 * Created by frowhy on 2017/3/15.
 */

public class XposedMain implements IXposedHookLoadPackage {
    private final String PACKAGE_NAME = XposedMain.class.getPackage().getName();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final Class<?> phoneWindowManager;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            phoneWindowManager = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", lpparam.classLoader);
        } else {
            phoneWindowManager = XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);
        }

        XposedBridge.hookAllMethods(phoneWindowManager, "interceptKeyBeforeQueueing", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int keyCode = ((KeyEvent) param.args[0]).getKeyCode();
                XSharedPreferences mXsp = new XSharedPreferences(PACKAGE_NAME);
                if (mXsp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
                    if (mXsp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false)) {
                        if (mXsp.getBoolean(Config.DISABLED_VOLUME_KEY, false) && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                            param.setResult(0);
                            return;
                        }
                        String[] sourceStrArray = mXsp.getString(Config.CUSTOM_KEYCODE, "").split(" ");
                        Arrays.sort(sourceStrArray);
                        int index = Arrays.binarySearch(sourceStrArray, String.valueOf(keyCode));

                        boolean isDisabled = index >= 0;
                        if (isDisabled) {
                            param.setResult(0);
                            return;
                        }
                    }
                }
                if (mXsp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
                    param.setResult(0);
                }
            }
        });
    }
}
