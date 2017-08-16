package tool.xfy9326.keyblocker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;
import tool.xfy9326.keyblocker.service.KeyBlockService;

public class MainActivity extends Activity {
    private Button
            mBtnStart,
            mBtnSettings,
            mBtnAccessEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);
        initView();
        initHandle();
    }

    private void initHandle() {
        mBtnStart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                if (BaseMethod.isAccessibilitySettingsOn(MainActivity.this)) {
                    //Close Service
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        ((Button) v).setText(R.string.go_start);
                        Intent intent = new Intent(MainActivity.this, KeyBlockService.class);
                        intent.putExtra(Config.CLOSE_SERVICE, true);
                        startService(intent);
                    } else if (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) && mSp.getBoolean(Config.ROOT_FUNCTION, false)) {
                        //Use this root command may cause some problems
                        BaseMethod.controlAccessibilityServiceWithRoot(false, false);
                        ((Button) v).setText(R.string.go_start);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.warn_service_started, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Open Service
                    if (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) && mSp.getBoolean(Config.ROOT_FUNCTION, false)) {
                        BaseMethod.controlAccessibilityServiceWithRoot(true, false);
                        ((Button) v).setText(R.string.close_service);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                }
            }
        });

        mBtnAccessEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.access_entry)
                        .setMessage(R.string.access_entry_use)
                        .setNegativeButton(R.string.cancel, null);
                dialog.show();
            }
        });

        mBtnSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(this);
        if (BaseMethod.isAccessibilitySettingsOn(this) && (mSp.getBoolean(Config.ROOT_OPEN_SERVICE, false) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            mBtnStart.setText(R.string.close_service);
        } else {
            mBtnStart.setText(R.string.go_start);
        }
        super.onResume();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnAccessEntry = (Button) findViewById(R.id.btn_access_entry);
        mBtnSettings = (Button) findViewById(R.id.btn_settings);
    }

}
