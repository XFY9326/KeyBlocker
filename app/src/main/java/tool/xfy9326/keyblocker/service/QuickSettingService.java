package tool.xfy9326.keyblocker.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
    private SharedPreferences mSp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateView(false);
    }

    @Override
    public void onClick() {
        super.onClick();
        BaseMethod.KeyLockBroadcast(this);
        updateView(true);
        BaseMethod.collapseStatusBar(this);
    }

    private void updateView(boolean displayToast) {
        Tile tile = getQsTile();
        if (BaseMethod.isAccessibilitySettingsOn(this)) {
            if (mSp.getBoolean(Config.ENABLED_KEYBLOCK, true)) {
                tile.setState(Tile.STATE_ACTIVE);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            if (displayToast) {
                Toast.makeText(this, R.string.start_service_first, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        }
        tile.updateTile();
    }
}
