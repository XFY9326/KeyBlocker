package tool.xfy9326.keyblocker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.net.Uri;

public class MainActivity extends Activity {
	 private SharedPreferences mSp;
	 private SharedPreferences.Editor mSpEditor;
	 private Button
	 mBtnHelp,
	 mBtnStart,
	 mBtnSettingCustomKeycode,
	 mBtnAccessEntry;
	 private CheckBox
	 mCbRemoveNotification,
	 mCbNotificationIcon,
	 mCbRootFunction,
	 mCbButtonVibrate,
	 mCbDisabledVolumeKey,
	 mCbDisplayNotification,
	 mCbDisplayKeycode,
	 mCbEnabledCustomKeycode;
	 private String mCustomKeycodeRegEx = "^(\\d+ )*\\d+$";
	 private boolean ButtonVibrateCancel = false;

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

		  mCbDisabledVolumeKey.setEnabled(!mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
		  mCbDisabledVolumeKey.setChecked(mSp.getBoolean(Config.ENABLED_VOLUME_KEY, false));
		  mCbDisabledVolumeKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 mSpEditor.putBoolean(Config.ENABLED_VOLUME_KEY, displayToast(isChecked) == isChecked);
						 mSpEditor.commit();
						 mCbDisabledVolumeKey.setChecked(mSp.getBoolean(Config.ENABLED_VOLUME_KEY, false));
					}
			   });

		  mCbDisplayNotification.setChecked(mSp.getBoolean(Config.DISPLAY_NOTIFICATION, false));
		  mCbDisplayNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 mSpEditor.putBoolean(Config.DISPLAY_NOTIFICATION, isChecked);
						 mSpEditor.commit();
						 BaseMethod.RestartAccessibilityService(MainActivity.this);
					}
			   });

		  mCbRootFunction.setChecked(mSp.getBoolean(Config.ROOT_FUNCTION, false));
		  mCbRootFunction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 if (isChecked) {
							  if (BaseMethod.isRoot()) {
								   mSpEditor.putBoolean(Config.ROOT_FUNCTION, true);
								   mCbButtonVibrate.setEnabled(true);
								   if (displayToast(true)) {
										BaseMethod.RestartAccessibilityService(MainActivity.this);
								   }
							  } else {
								   buttonView.setChecked(false);
								   mCbButtonVibrate.setEnabled(false);
								   mSpEditor.putBoolean(Config.ROOT_FUNCTION, false);
							  }
						 } else {
							  mCbButtonVibrate.setEnabled(false);
							  mSpEditor.putBoolean(Config.ROOT_FUNCTION, false);
							  if (BaseMethod.isRoot()) {
								   displayToast(false);
								   if (displayToast(false)) {
										BaseMethod.RestartAccessibilityService(MainActivity.this);
								   }
							  }
						 }
						 mSpEditor.commit();
					}
			   });

		  mCbButtonVibrate.setEnabled(mSp.getBoolean(Config.ROOT_FUNCTION, false));
		  mCbButtonVibrate.setChecked(mSp.getBoolean(Config.BUTTON_VIBRATE, false));
		  mCbButtonVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
						 if (isChecked) {
							  AlertDialog.Builder vibrate_warn = new AlertDialog.Builder(MainActivity.this)
								   .setTitle(R.string.button_vibrate)
								   .setMessage(R.string.vibrate_warn)
								   .setCancelable(false)
								   .setPositiveButton(R.string.continuedo, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface di, int i) {
											 mSpEditor.putBoolean(Config.BUTTON_VIBRATE, true);
											 mSpEditor.commit();
											 BaseMethod.RestartAccessibilityService(MainActivity.this);
										}
								   })
								   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface di, int i) {
											 ButtonVibrateCancel = true;
											 buttonView.setChecked(false);
										}
								   });
							  vibrate_warn.show();
						 } else {
							  if (!ButtonVibrateCancel) {
								   mSpEditor.putBoolean(Config.BUTTON_VIBRATE, false);
								   mSpEditor.commit();
								   BaseMethod.RestartAccessibilityService(MainActivity.this);
							  }
							  ButtonVibrateCancel = false;
						 }
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
						 mCbDisabledVolumeKey.setEnabled(!mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
						 mCbEnabledCustomKeycode.setChecked(mSp.getBoolean(Config.ENABLED_CUSTOM_KEYCODE, false));
					}
			   });

		  mCbNotificationIcon.setChecked(mSp.getBoolean(Config.NOTIFICATION_ICON, false));
		  mCbNotificationIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 mSpEditor.putBoolean(Config.NOTIFICATION_ICON, isChecked);
						 mSpEditor.commit();
						 BaseMethod.RestartAccessibilityService(MainActivity.this);
					}
			   });

		  mCbRemoveNotification.setChecked(mSp.getBoolean(Config.REMOVE_NOTIFICATION, false));
		  mCbRemoveNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 mSpEditor.putBoolean(Config.REMOVE_NOTIFICATION, isChecked);
						 mSpEditor.commit();
						 BaseMethod.RestartAccessibilityService(MainActivity.this);
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
						 mBtnHelp = (Button) mSubView.findViewById(R.id.btn_help);

						 mAdBuilderCustomKeycode = new AlertDialog.Builder(MainActivity.this)
							  .setTitle(R.string.custom_setting)
							  .setView(mSubView)
							  .setCancelable(false);

						 mEtCustomKeycode.setText(mSp.getString(Config.CUSTOM_KEYCODE, ""));
						 mEtCustomKeycode.setSelection(mEtCustomKeycode.length());

						 mBtnHelp.setOnClickListener(new OnClickListener() {
								   @Override
								   public void onClick(View v) {
										String url = "https://github.com/XFY9326/KeyBlocker/wiki/DIY-KeyCode-Block";
										Intent intent = new Intent(Intent.ACTION_VIEW);
										Uri content_url = Uri.parse(url);
										intent.setData(content_url);
										startActivity(intent);
								   }
							  });

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
		  mCbRootFunction = (CheckBox) findViewById(R.id.cb_rootfunction);
		  mCbButtonVibrate = (CheckBox) findViewById(R.id.cb_buttonvibrate);
		  mCbNotificationIcon = (CheckBox) findViewById(R.id.cb_notification_icon);
		  mCbRemoveNotification = (CheckBox) findViewById(R.id.cb_remove_notification);
		  mBtnSettingCustomKeycode = (Button) findViewById(R.id.btn_setting_custom);
		  mBtnAccessEntry = (Button) findViewById(R.id.btn_access_entry);

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
			   BaseMethod.RunAccessibilityService(this);
			   return false;
		  }
	 }
}
