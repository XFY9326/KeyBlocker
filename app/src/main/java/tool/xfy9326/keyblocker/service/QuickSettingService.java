package tool.xfy9326.keyblocker.service;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
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
		updateView(false, true);
	}

	@Override
	public void onClick() {
		super.onClick();
		updateView(true, false);
		BaseMethod.KeyLockBroadcast(this);
		BaseMethod.collapseStatusBar(this);
	}

	private void updateView(boolean displayToast, boolean init) {
		Tile tile = getQsTile();
		if (BaseMethod.isAccessibilitySettingsOn(this)) {
			boolean KeyBlocked = mSp.getBoolean(Config.ENABLED_KEYBLOCK, true);
			if (init) {
				if (KeyBlocked) {
					tile.setState(Tile.STATE_ACTIVE);
				} else {
					tile.setState(Tile.STATE_INACTIVE);
				}
			} else {
				if (tile.getState() == Tile.STATE_ACTIVE) {
					tile.setState(Tile.STATE_INACTIVE);
				} else if (tile.getState() == Tile.STATE_INACTIVE) {
					tile.setState(Tile.STATE_ACTIVE);
				}
			}
		} else {
			tile.setState(Tile.STATE_INACTIVE);
			if (displayToast) {
				BaseMethod.RunAccessibilityService(this);
			}
		}
		tile.updateTile();
	}
}
