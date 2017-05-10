package tool.xfy9326.keyblocker.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class SettingsActivity extends PreferenceActivity {
	private SharedPreferences mSp;
	private SharedPreferences.Editor mSpEditor;
	private String[] AppNames, PkgNames;
	private boolean[] AppState;
	private boolean ButtonVibrateCancel = false;
	private String mCustomKeycodeRegEx = "^(\\d+ )*\\d+$";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_settings);
		mSp = PreferenceManager.getDefaultSharedPreferences(this);
		mSpEditor = mSp.edit();
		mSpEditor.apply();
		Settings();
	}

	private void Settings() {
		CheckBoxPreference mCbEnabledVolumeKey = (CheckBoxPreference) findPreference(Config.ENABLED_VOLUME_KEY);
		mCbEnabledVolumeKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference p, Object o) {
					displayToast((boolean)o);
					return true;
				}
			});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			CheckBoxPreference mCbDisplayNotification = (CheckBoxPreference) findPreference(Config.DISPLAY_NOTIFICATION);
			mCbDisplayNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference p, Object o) {
						BaseMethod.RestartAccessibilityService(SettingsActivity.this);
						return true;
					}
				});

			CheckBoxPreference mCbAutoCloseStatusBar = (CheckBoxPreference) findPreference(Config.AUTO_CLOSE_STATUSBAR);
			mCbAutoCloseStatusBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference p, Object o) {
						displayToast((boolean)o);
						return true;
					}
				});
		}

		CheckBoxPreference mCbRootFunction = (CheckBoxPreference) findPreference(Config.ROOT_FUNCTION);
		mCbRootFunction.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference p, Object o) {
					boolean isChecked = o;
					if (isChecked) {
						if (BaseMethod.isRoot()) {
							if (displayToast(true)) {
								BaseMethod.RestartAccessibilityService(SettingsActivity.this);
							}
						}
					} else {
						if (BaseMethod.isRoot()) {
							displayToast(false);
							if (displayToast(false)) {
								BaseMethod.RestartAccessibilityService(SettingsActivity.this);
							}
						}
					}
					return true;
				}
			});

		CheckBoxPreference mCbButtonVibrate = (CheckBoxPreference) findPreference(Config.BUTTON_VIBRATE);
		mCbButtonVibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference p, Object o) {
					boolean isChecked = o;
					if (isChecked) {
						AlertDialog.Builder vibrate_warn = new AlertDialog.Builder(SettingsActivity.this)
							.setTitle(R.string.button_vibrate)
							.setMessage(R.string.vibrate_warn)
							.setCancelable(false)
							.setPositiveButton(R.string.continuedo, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface di, int i) {
									BaseMethod.RestartAccessibilityService(SettingsActivity.this);
								}
							})
							.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface di, int i) {
									ButtonVibrateCancel = true;
								}
							});
						vibrate_warn.show();
					} else {
						if (!ButtonVibrateCancel) {
							BaseMethod.RestartAccessibilityService(SettingsActivity.this);
						}
						ButtonVibrateCancel = false;
					}
					return true;
				}
			});

		CheckBoxPreference mCbNotificationIcon = (CheckBoxPreference) findPreference(Config.NOTIFICATION_ICON);
		mCbNotificationIcon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference p, Object o) {
					BaseMethod.RestartAccessibilityService(SettingsActivity.this);
					return true;
				}
			});

		CheckBoxPreference mCbRemoveNotification = (CheckBoxPreference) findPreference(Config.REMOVE_NOTIFICATION);
		mCbRemoveNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object o) {
					BaseMethod.RestartAccessibilityService(SettingsActivity.this);
					return true;
				}
			});


		Preference mBtnSettingCustomKeycode = findPreference(Config.CUSTOM_SETTINGS);
		mBtnSettingCustomKeycode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				View mSubView;
				EditText mEtCustomKeycode;
				AlertDialog mAdCustomKeycode;
				AlertDialog.Builder mAdBuilderCustomKeycode;
				Button mBtnCancel, mBtnSubmit, mBtnHelp;

				@Override
				public boolean onPreferenceClick(Preference p) {
					LayoutInflater mLiContent = LayoutInflater.from(SettingsActivity.this);
					mSubView = mLiContent.inflate(R.layout.v_custom_keycode, null);
					mEtCustomKeycode = (EditText) mSubView.findViewById(R.id.et_custom_keycode);
					mBtnCancel = (Button) mSubView.findViewById(R.id.btn_cancel);
					mBtnSubmit = (Button) mSubView.findViewById(R.id.btn_submit);
					mBtnHelp = (Button) mSubView.findViewById(R.id.btn_help);

					mAdBuilderCustomKeycode = new AlertDialog.Builder(SettingsActivity.this)
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
										Toast.makeText(SettingsActivity.this, R.string.wrong_format, Toast.LENGTH_SHORT).show();
									}
								}
							}
						});

					mAdCustomKeycode = mAdBuilderCustomKeycode.show();
					return true;
				}
			});
		Preference mKeyBlockActivitySet = findPreference(Config.KEYBLOCK_ACTIVITY_SET);
		mKeyBlockActivitySet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference p) {
					Toast.makeText(SettingsActivity.this, R.string.loading, Toast.LENGTH_LONG).show();
					new Thread(new Runnable() {
							public void run() {
								Looper.prepare();
								final ArrayList<String> FilterApplication = BaseMethod.StringToStringArrayList(mSp.getString(Config.CUSTOM_KEYBLOCK_ACTIVITY, Config.EMPTY_ARRAY));
								getAppInfo(SettingsActivity.this, FilterApplication);
								final AlertDialog.Builder KeyBlockActivityAlert = new AlertDialog.Builder(SettingsActivity.this)
									.setTitle(R.string.keyblock_activity_settings)
									.setMultiChoiceItems(AppNames, AppState, new DialogInterface.OnMultiChoiceClickListener(){
										public void onClick(DialogInterface dialog, int position, boolean isChecked) {
											AppState[position] = isChecked;
										}
									})
									.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog, int i) {
											FilterApplication.clear();
											for (int a = 0; a < AppState.length; a ++) {
												if (AppState[a]) {
													FilterApplication.add(PkgNames[a]);
												}
											}
											mSpEditor.putString(Config.CUSTOM_KEYBLOCK_ACTIVITY, FilterApplication.toString());
											mSpEditor.commit();
										}
									})
									.setNegativeButton(R.string.cancel, null);
								runOnUiThread(new Runnable() {
										public void run() {
											KeyBlockActivityAlert.show();
										}
									});
							}
						}).start();
					return true;
				}
			});
	}

	private void getAppInfo(Context ctx, ArrayList<String> PkgHave) {
		PackageManager pm = ctx.getPackageManager();
		List<PackageInfo> info = pm.getInstalledPackages(0);


		Iterator<PackageInfo> it = info.iterator();
		while (it.hasNext()) {
			try {
				ActivityInfo[] actInfo = pm.getPackageInfo(it.next().packageName, PackageManager.GET_ACTIVITIES).activities;
				if (actInfo == null) {
					it.remove();
				}
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
				break;
			}
		}

		BaseMethod.orderPackageList(ctx, info);
		AppNames = new String[info.size() - 1];
		PkgNames = new String[info.size() - 1];
		AppState = new boolean[info.size() - 1];
		int countnum = 0;
		for (int i = 0; i < info.size();i++) {
			String pkgname = info.get(i).packageName;
			if (!pkgname.equalsIgnoreCase(ctx.getPackageName())) {
				AppNames[countnum] = info.get(i).applicationInfo.loadLabel(ctx.getPackageManager()).toString();
				PkgNames[countnum] = pkgname;
				if (PkgHave.contains(pkgname)) {
					AppState[countnum] = true;
				} else {
					AppState[countnum] = false;
				}
				countnum++;
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
