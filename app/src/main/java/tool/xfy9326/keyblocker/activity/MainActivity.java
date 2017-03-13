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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

public class MainActivity extends Activity {
    private SharedPreferences mSp;
    private SharedPreferences.Editor mSpEditor;
    private Button
            mBtnStart,
            mBtnSettingCustomKeycode;
    private CheckBox
            mCbDisabledVolumeKey,
            mCbDisplayNotification,
            mCbDisplayKeycode,
            mCbEnabledCustomKeycode;
    private String mCustomKeycodeRegEx = "^(\\d+ )*\\d+$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mSpEditor = mSp.edit();
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

        mCbDisabledVolumeKey.setEnabled(mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
        mCbDisabledVolumeKey.setChecked(mSp.getBoolean(Config.DISABLED_VOLUME_KEY, false));
        mCbDisabledVolumeKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSpEditor.putBoolean(Config.DISABLED_VOLUME_KEY, displayToast(isChecked) == isChecked);
                mSpEditor.commit();
                mCbDisabledVolumeKey.setChecked(mSp.getBoolean(Config.DISABLED_VOLUME_KEY, false));
            }
        });

        mCbDisplayNotification.setChecked(mSp.getBoolean(Config.DISPLAY_NOTIFICATION, false));
        mCbDisplayNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSpEditor.putBoolean(Config.DISPLAY_NOTIFICATION, isChecked);
                mSpEditor.commit();
                Toast.makeText(MainActivity.this, R.string.restart_service, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

        mCbDisplayKeycode.setChecked(mSp.getBoolean(Config.DISPLAY_KEYCODE, false));
        mCbDisplayKeycode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSpEditor.putBoolean(Config.DISPLAY_KEYCODE, displayToast(isChecked) == isChecked);
                mSpEditor.commit();
                mCbDisplayKeycode.setChecked(mSp.getBoolean(Config.DISPLAY_KEYCODE, false));
            }
        });

        mCbEnabledCustomKeycode.setChecked(mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
        mCbEnabledCustomKeycode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSpEditor.putBoolean(Config.ENABLED_CUSTOM_KEYCODE, displayToast(isChecked) == isChecked);
                mSpEditor.commit();
                mCbDisabledVolumeKey.setEnabled(mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
                mCbEnabledCustomKeycode.setChecked(mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
            }
        });

        mBtnSettingCustomKeycode.setOnClickListener(new OnClickListener() {
            View mSubView;
            EditText mEtCustomKeycode;
            AlertDialog mAdCustomKeycode;
            AlertDialog.Builder mAdBuilderCustomKeycode;
            Button mBtnCancel, mBtnSubmit;

            @Override
            public void onClick(View v) {
                LayoutInflater mLiContent = LayoutInflater.from(MainActivity.this);
                mSubView = mLiContent.inflate(R.layout.v_custom_keycode, null);
                mEtCustomKeycode = (EditText) mSubView.findViewById(R.id.et_custom_keycode);
                mBtnCancel = (Button) mSubView.findViewById(R.id.btn_cancel);
                mBtnSubmit = (Button) mSubView.findViewById(R.id.btn_submit);

                mAdBuilderCustomKeycode = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.custom_setting)
                        .setView(mSubView)
                        .setCancelable(false);

                mEtCustomKeycode.setText(mSp.getString(Config.CUSTOM_KEYCODE, ""));
                mEtCustomKeycode.setSelection(mEtCustomKeycode.length());

                mBtnCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdCustomKeycode.cancel();
                    }
                });

                mBtnSubmit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mEtCustomKeycode.length() == 0) {
                            mSpEditor.putString(Config.CUSTOM_KEYCODE, "");
                            mSpEditor.commit();
                            if (mAdCustomKeycode != null) {
                                mAdCustomKeycode.dismiss();
                            }
                        } else {
                            String mStringCustomKeycode = mEtCustomKeycode.getText().toString();
                            if (mStringCustomKeycode.matches(mCustomKeycodeRegEx)) {
                                mSpEditor.putString(Config.CUSTOM_KEYCODE, mStringCustomKeycode);
                                mSpEditor.commit();
                                if (mAdCustomKeycode != null) {
                                    mAdCustomKeycode.dismiss();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, R.string.wrong_format, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                mAdCustomKeycode = mAdBuilderCustomKeycode.show();
            }
        });
    }

    @SuppressLint("InflateParams")
    private void initView() {
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mCbDisabledVolumeKey = (CheckBox) findViewById(R.id.cb_disabled_volume_key);
        mCbDisplayNotification = (CheckBox) findViewById(R.id.cb_display_notification);
        mCbDisplayKeycode = (CheckBox) findViewById(R.id.cb_display_keycode);
        mCbEnabledCustomKeycode = (CheckBox) findViewById(R.id.cb_enabled_custom_keycode);
        mBtnSettingCustomKeycode = (Button) findViewById(R.id.btn_setting_custom);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (!mSp.getBoolean(Config.DISPLAY_NOTIFICATION, false)) {
                mCbDisplayNotification.setVisibility(View.GONE);
            }
        }
    }

    private boolean displayToast(boolean enabled) {
        if (BaseMethod.isAccessibilitySettingsOn(this)) {
            String mStrToast;
            if (enabled) {
                mStrToast = getString(R.string.has_enabled);
            } else {
                mStrToast = getString(R.string.has_disabled);
            }
            Toast.makeText(this, mStrToast, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, R.string.start_service_first, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return false;
        }
    }
}
