package tool.xfy9326.keyblocker.activity;

import android.app.Activity;
import android.os.Bundle;

import tool.xfy9326.keyblocker.base.BaseMethod;

public class ShortCutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BaseMethod.isAccessibilitySettingsOn(this)) {
            BaseMethod.KeyLockBroadcast(this);
        } else {
            BaseMethod.RunAccessibilityService(this);
        }
        finish();
    }
}
