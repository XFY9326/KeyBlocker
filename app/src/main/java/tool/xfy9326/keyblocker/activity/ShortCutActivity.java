package tool.xfy9326.keyblocker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;

public class ShortCutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!createShortCut()) {
            startAccessibilityService();
        }
        finish();
    }

    private boolean createShortCut() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getAction().equals(Intent.ACTION_CREATE_SHORTCUT)) {
                Intent result = new Intent(this, ShortCutActivity.class);
                result.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
                result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_shortcut));
                setResult(-1, new Intent().putExtra(Intent.EXTRA_SHORTCUT_INTENT, result));
                return true;
            }
        }
        return false;
    }

    private void startAccessibilityService() {
        if (BaseMethod.isAccessibilitySettingsOn(this)) {
            BaseMethod.KeyLockBroadcast(this);
        } else {
            BaseMethod.RunAccessibilityService(this);
        }
    }
}
