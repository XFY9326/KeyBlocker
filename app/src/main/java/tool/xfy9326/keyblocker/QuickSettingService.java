package tool.xfy9326.keyblocker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
    private SharedPreferences sp = null;

    @Override
    public void onCreate() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate();
    }

    @Override
    public void onStartListening() {
        UpdateView(false);
        super.onStartListening();
    }

    @Override
    public void onClick() {
        Intent intent = new Intent();
        intent.setAction(Methods.Notify_Action);
        sendBroadcast(intent);
        UpdateView(true);
        Methods.collapseStatusBar(this);
        super.onClick();
    }

    private void UpdateView(boolean showtoast) {
        Tile tile = getQsTile();
        if (Methods.isAccessibilitySettingsOn(this)) {
            if (sp.getBoolean("KeyBlocked", false)) {
                tile.setState(Tile.STATE_ACTIVE);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            if (showtoast) {
                Toast.makeText(this, R.string.start_service_first, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        }
        tile.updateTile();
    }
}
