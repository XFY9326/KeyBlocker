package tool.xfy9326.keyblocker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class GuideActivity extends Activity {
    private SharedPreferences sp;
    private SharedPreferences.Editor sped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();
        sped.apply();
        ButtonSet();
    }

    private void ButtonSet() {
        Button start = (Button) findViewById(R.id.button_start);
        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
        final CheckBox volume = (CheckBox) findViewById(R.id.checkbox_volume_button_blocked);
        volume.setEnabled(sp.getBoolean("EnabledCustomKeycode", false));
        volume.setChecked(sp.getBoolean("VolumeButton_Block", false));
        volume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton cb, boolean b) {
                sped.putBoolean("VolumeButton_Block", b);
                sped.commit();
                showToast(b);
            }
        });
        CheckBox force_notify = (CheckBox) findViewById(R.id.checkbox_force_notification_control);
        force_notify.setChecked(sp.getBoolean("ForceNotify", false));
        force_notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton cb, boolean b) {
                sped.putBoolean("ForceNotify", b);
                sped.commit();
                Toast.makeText(GuideActivity.this, R.string.restart_service, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });


        CheckBox mCbTestKeycode = (CheckBox) findViewById(R.id.checkbox_testKeycode);
        mCbTestKeycode.setChecked(sp.getBoolean("TestKeycode", false));
        mCbTestKeycode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sped.putBoolean("TestKeycode", isChecked);
                sped.commit();
                showToast(isChecked);
            }
        });

        CheckBox mCbEnabledCustom = (CheckBox) findViewById(R.id.checkbox_enabled_custom);
        mCbEnabledCustom.setChecked(sp.getBoolean("EnabledCustomKeycode", false));
        mCbEnabledCustom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sped.putBoolean("EnabledCustomKeycode", isChecked);
                sped.commit();
                volume.setEnabled(isChecked);
                showToast(isChecked);
            }
        });

        Button mBtnSettingCustom = (Button) findViewById(R.id.btn_setting_custom);
        mBtnSettingCustom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                final LayoutInflater inflater = LayoutInflater.from(GuideActivity.this);
                @SuppressLint("InflateParams") final View subView = inflater.inflate(R.layout.view_edit_custom_keycode, null);
                final EditText mEtCustomKeycode = (EditText) subView.findViewById(R.id.et_custom_keycode);
                final Button mBtnCancel = (Button) subView.findViewById(R.id.btn_cancel);
                final Button mBtnSubmit = (Button) subView.findViewById(R.id.btn_submit);
                final String customKeycodeRegEx = "^(\\d+ )*\\d+$";

                mEtCustomKeycode.setText(sp.getString("CustomKeycode", ""));
                mEtCustomKeycode.setSelection(mEtCustomKeycode.length());

                final AlertDialog mAdCustomKeycode = new AlertDialog.Builder(GuideActivity.this)
                        .setTitle(R.string.custom_setting)
                        .setView(subView)
                        .setCancelable(false)
                        .show();

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
                            sped.putString("CustomKeycode", "");
                            sped.commit();
                            mAdCustomKeycode.dismiss();
                        } else {
                            String mStringCustomKeycode = mEtCustomKeycode.getText().toString();
                            if (mStringCustomKeycode.matches(customKeycodeRegEx)) {
                                sped.putString("CustomKeycode", mStringCustomKeycode);
                                sped.commit();
                                mAdCustomKeycode.dismiss();
                            } else {
                                Toast.makeText(GuideActivity.this, R.string.wrong_format, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void showToast(boolean enabled) {
        if (Methods.isAccessibilitySettingsOn(this)) {
            String toastString;
            if (enabled) {
                toastString = getString(R.string.has_enabled);
            } else {
                toastString = getString(R.string.has_disabled);
            }
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.start_service_first, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }
}
