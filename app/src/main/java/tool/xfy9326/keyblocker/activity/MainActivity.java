package tool.xfy9326.keyblocker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import tool.xfy9326.keyblocker.R;

public class MainActivity extends Activity {
    private Button
            mBtnStart,
            mBtnSettings,
            mBtnAccessEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);
        SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mSpEditor = mSp.edit();
        mSpEditor.apply();
        initView();
        initHandle();
    }

    private void initHandle() {
        mBtnStart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
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

    @SuppressLint("InflateParams")
    private void initView() {
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnAccessEntry = (Button) findViewById(R.id.btn_access_entry);
        mBtnSettings = (Button) findViewById(R.id.btn_settings);
    }


}
