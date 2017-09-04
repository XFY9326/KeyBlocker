package tool.xfy9326.keyblocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

public class RemoteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(context);
        if (intent.getAction().equals(Config.REMOTE_CONTROL_ACTION)) {
            boolean displayToast = intent.getBooleanExtra("RESPOND", true);
            if (BaseMethod.isAccessibilitySettingsOn(context)) {
                BaseMethod.KeyLockBroadcast(context, true, true);
            } else if (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) && mSp.getBoolean(Config.ROOT_FUNCTION, false)) {
                BaseMethod.controlAccessibilityServiceWithRoot(true, false);
            } else {
                if (displayToast) {
                    Toast.makeText(context, R.string.start_service_first, Toast.LENGTH_SHORT).show();
                    Intent access_intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    context.startActivity(access_intent);
                }
            }
        }
    }

}
