package tool.xfy9326.keyblocker.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import tool.xfy9326.keyblocker.base.BaseMethod;

public class ShortCutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (BaseMethod.isAccessibilitySettingsOn(this)) {
                BaseMethod.KeyLockBroadcast(this);
            } else {
                BaseMethod.RunAccessibilityService(this);
            }
        }
        finish();
    }
}
