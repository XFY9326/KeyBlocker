package tool.xfy9326.keyblocker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tool.xfy9326.keyblocker.R;
import tool.xfy9326.keyblocker.base.BaseMethod;
import tool.xfy9326.keyblocker.config.Config;

public class SettingsActivity extends Activity {
    private PrefsFragment prefsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsFragment = new PrefsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, prefsFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.REQUEST_CODE_READ_PHONE_STAT) {
            if (BaseMethod.hasPhoneStatPermission(this)) {
                prefsFragment.findPreference(Config.KEYBLOCK_ACTIVITY_TEXT_SET).setEnabled(false);
                BaseMethod.RestartAccessibilityService(this);
            } else {
                ((CheckBoxPreference) prefsFragment.findPreference(Config.KEYBLOCK_ACTIVITY_ADVANCED_SCAN_MODE)).setChecked(false);
                Toast.makeText(this, R.string.warn_no_permission, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class PrefsFragment extends PreferenceFragment {
        private final String mCustomKeycodeRegEx = "^(\\d+ )*\\d+$";
        private final String mActivityKeyWordsRegEx = "^(\\S+ )*\\S+$";
        private SharedPreferences mSp;
        private SharedPreferences.Editor mSpEditor;
        private String[] AppNames, PkgNames;
        private boolean[] AppState;
        private boolean AppListOpening = false;
        private Preference.OnPreferenceChangeListener launchService;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_settings);
            mSp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mSpEditor = mSp.edit();
            mSpEditor.apply();
            launchService = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference p, Object o) {
                    BaseMethod.RestartAccessibilityService(getActivity());
                    return true;
                }
            };
            GeneralSettings();
            ActivityBlockSettings();
            NotificationSettings();
            RootSettings();
            NSettings();
            CustomSettings();
        }

        private void GeneralSettings() {
            CheckBoxPreference mCbEnabledVolumeKey = (CheckBoxPreference) findPreference(Config.ENABLED_VOLUME_KEY);
            mCbEnabledVolumeKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference p, Object o) {
                    displayToast((boolean) o);
                    return true;
                }
            });

            findPreference(Config.CLOSE_ADVANCED_FUNCTIONS).setOnPreferenceChangeListener(launchService);
        }

        private void ActivityBlockSettings() {
            Preference mKeyBlockActivitySet = findPreference(Config.KEYBLOCK_ACTIVITY_LIST_SET);
            mKeyBlockActivitySet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference p) {
                    if (!AppListOpening) {
                        AppListOpening = true;
                        Toast.makeText(getActivity(), R.string.loading, Toast.LENGTH_LONG).show();
                        new Thread(new Runnable() {
                            public void run() {
                                Looper.prepare();
                                final ArrayList<String> FilterApplication = BaseMethod.StringToStringArrayList(mSp.getString(Config.CUSTOM_KEYBLOCK_ACTIVITY, Config.EMPTY_ARRAY));
                                getAppInfo(getActivity(), FilterApplication);
                                final AlertDialog.Builder KeyBlockActivityAlert = new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.keyblock_activity_settings)
                                        .setCancelable(false)
                                        .setMultiChoiceItems(AppNames, AppState, new DialogInterface.OnMultiChoiceClickListener() {
                                            public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                                                AppState[position] = isChecked;
                                            }
                                        })
                                        .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int i) {
                                                FilterApplication.clear();
                                                for (int a = 0; a < AppState.length; a++) {
                                                    if (AppState[a]) {
                                                        FilterApplication.add(PkgNames[a]);
                                                    }
                                                }
                                                mSpEditor.putString(Config.CUSTOM_KEYBLOCK_ACTIVITY, FilterApplication.toString());
                                                mSpEditor.apply();
                                                AppListOpening = false;
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int i) {
                                                AppListOpening = false;
                                            }
                                        });
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        KeyBlockActivityAlert.show();
                                    }
                                });
                            }
                        }).start();
                    }
                    return true;
                }
            });

            CheckBoxPreference mKeyBlockScanMode = (CheckBoxPreference) findPreference(Config.KEYBLOCK_ACTIVITY_ADVANCED_SCAN_MODE);
            mKeyBlockScanMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        BaseMethod.showPhoneStatAlert(getActivity(), (CheckBoxPreference) preference, PrefsFragment.this);
                    } else {
                        findPreference(Config.KEYBLOCK_ACTIVITY_TEXT_SET).setEnabled(true);
                        BaseMethod.RestartAccessibilityService(getActivity());
                    }
                    return true;
                }
            });

            Preference mKeyBlockKeyWordsSet = findPreference(Config.KEYBLOCK_ACTIVITY_TEXT_SET);
            if (mSp.getBoolean(Config.KEYBLOCK_ACTIVITY_ADVANCED_SCAN_MODE, false)) {
                mKeyBlockKeyWordsSet.setEnabled(false);
            }
            mKeyBlockKeyWordsSet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View view = layoutInflater.inflate(R.layout.dialog_activity_keywords_set, (ViewGroup) getActivity().findViewById(R.id.layout_activity_keywords));
                    final EditText editText = (EditText) view.findViewById(R.id.et_activity_keywords);
                    editText.setText(mSp.getString(Config.CUSTOM_KEYBLOCK_ACTIVITY_KEY_WORDS, ""));
                    editText.setSelection(editText.length());

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.keyblock_activity_settings_text);
                    builder.setCancelable(false);
                    builder.setView(view);
                    builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String result = editText.getText().toString();
                            if (editText.length() == 0) {
                                mSpEditor.putString(Config.CUSTOM_KEYBLOCK_ACTIVITY_KEY_WORDS, result);
                                mSpEditor.apply();
                            } else {
                                if (result.matches(mActivityKeyWordsRegEx)) {
                                    mSpEditor.putString(Config.CUSTOM_KEYBLOCK_ACTIVITY_KEY_WORDS, result);
                                    mSpEditor.apply();
                                } else {
                                    Toast.makeText(getActivity(), R.string.wrong_format, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.show();
                    return false;
                }
            });
        }

        private void NotificationSettings() {
            findPreference(Config.NOTIFICATION_ICON).setOnPreferenceChangeListener(launchService);
            findPreference(Config.REMOVE_NOTIFICATION).setOnPreferenceChangeListener(launchService);
        }

        private void RootSettings() {
            CheckBoxPreference mCbRootFunction = (CheckBoxPreference) findPreference(Config.ROOT_FUNCTION);
            mCbRootFunction.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference p, Object o) {
                    boolean isChecked = (boolean) o;
                    if (isChecked) {
                        if (BaseMethod.isRooted()) {
                            BaseMethod.getRoot();
                            return true;
                        } else {
                            if (BaseMethod.isRooted_old()) {
                                return true;
                            } else {
                                Toast.makeText(getActivity(), R.string.root_failed, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                    }
                    BaseMethod.RestartAccessibilityService(getActivity());
                    return true;
                }
            });

            CheckBoxPreference mCbButtonVibrate = (CheckBoxPreference) findPreference(Config.BUTTON_VIBRATE);
            mCbButtonVibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference p, Object o) {
                    boolean isChecked = (boolean) o;
                    if (isChecked) {
                        AlertDialog.Builder vibrate_warn = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.button_vibrate_light_control)
                                .setMessage(R.string.vibrate_warn)
                                .setCancelable(false)
                                .setPositiveButton(R.string.continue_do, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface di, int i) {
                                        BaseMethod.RestartAccessibilityService(getActivity());
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface di, int i) {
                                        ((CheckBoxPreference) p).setChecked(false);
                                    }
                                });
                        vibrate_warn.show();
                    } else {
                        BaseMethod.RestartAccessibilityService(getActivity());
                    }
                    return true;
                }
            });

            CheckBoxPreference mCbRootScanActivity = (CheckBoxPreference) findPreference(Config.ROOT_SCAN_ACTIVITY);
            mCbRootScanActivity.setOnPreferenceChangeListener(launchService);
        }

        private void NSettings() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CheckBoxPreference mCbDisplayNotification = (CheckBoxPreference) findPreference(Config.DISPLAY_NOTIFICATION);
                mCbDisplayNotification.setOnPreferenceChangeListener(launchService);

                CheckBoxPreference mCbAutoCloseStatusBar = (CheckBoxPreference) findPreference(Config.AUTO_CLOSE_STATUSBAR);
                mCbAutoCloseStatusBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference p, Object o) {
                        displayToast((boolean) o);
                        return true;
                    }
                });
            } else {
                CheckBoxPreference mCbDisplayNotification = (CheckBoxPreference) findPreference(Config.DISPLAY_NOTIFICATION);
                CheckBoxPreference mCbAutoCloseStatusBar = (CheckBoxPreference) findPreference(Config.AUTO_CLOSE_STATUSBAR);
                mCbDisplayNotification.setEnabled(false);
                mCbAutoCloseStatusBar.setEnabled(false);
            }
        }

        private void CustomSettings() {
            Preference mBtnSettingCustomKeycode = findPreference(Config.CUSTOM_SETTINGS);
            mBtnSettingCustomKeycode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                View mSubView;
                EditText mEtCustomKeycode;
                AlertDialog.Builder mAdBuilderCustomKeycode;

                @Override
                public boolean onPreferenceClick(Preference p) {
                    LayoutInflater mLiContent = LayoutInflater.from(getActivity());
                    mSubView = mLiContent.inflate(R.layout.dialog_keycode_custom, (ViewGroup) getActivity().findViewById(R.id.layout_custom_keycode));
                    mEtCustomKeycode = (EditText) mSubView.findViewById(R.id.et_custom_keycode);

                    mAdBuilderCustomKeycode = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.custom_setting)
                            .setView(mSubView)
                            .setCancelable(false);
                    mEtCustomKeycode.setText(mSp.getString(Config.CUSTOM_KEYCODE, ""));
                    mEtCustomKeycode.setSelection(mEtCustomKeycode.length());
                    mAdBuilderCustomKeycode.setNeutralButton(R.string.help, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = "https://github.com/XFY9326/KeyBlocker/wiki/DIY-KeyCode-Block";
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri content_url = Uri.parse(url);
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    });
                    mAdBuilderCustomKeycode.setNegativeButton(R.string.cancel, null);
                    mAdBuilderCustomKeycode.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mEtCustomKeycode.length() == 0) {
                                mSpEditor.putString(Config.CUSTOM_KEYCODE, "");
                                mSpEditor.apply();
                            } else {
                                String mStringCustomKeycode = mEtCustomKeycode.getText().toString();
                                if (mStringCustomKeycode.matches(mCustomKeycodeRegEx)) {
                                    mSpEditor.putString(Config.CUSTOM_KEYCODE, mStringCustomKeycode);
                                    mSpEditor.apply();
                                } else {
                                    Toast.makeText(getActivity(), R.string.wrong_format, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    mAdBuilderCustomKeycode.show();
                    return true;
                }
            });
        }

        private void getAppInfo(Context ctx, ArrayList<String> PkgHave) {
            PackageManager pm = ctx.getPackageManager();
            boolean activity_filter = mSp.getBoolean(Config.KEYBLOCK_ACTIVITY_FILTER, true);
            List<PackageInfo> info = activity_filter ? pm.getInstalledPackages(PackageManager.GET_ACTIVITIES) : pm.getInstalledPackages(0);

            Iterator<PackageInfo> it = info.iterator();
            while (it.hasNext()) {
                PackageInfo packageinfo = it.next();
                ActivityInfo[] actInfo = packageinfo.activities;
                if (packageinfo.packageName.equalsIgnoreCase(ctx.getPackageName())) {
                    it.remove();
                    continue;
                }
                if (actInfo == null && activity_filter) {
                    it.remove();
                }
            }

            BaseMethod.orderPackageList(ctx, info);
            AppNames = new String[info.size()];
            PkgNames = new String[info.size()];
            AppState = new boolean[info.size()];
            for (int i = 0; i < info.size(); i++) {
                String pkgname = info.get(i).packageName;
                AppNames[i] = info.get(i).applicationInfo.loadLabel(ctx.getPackageManager()).toString();
                PkgNames[i] = pkgname;
                AppState[i] = PkgHave.contains(pkgname);
            }
        }

        private void displayToast(boolean enabled) {
            if (BaseMethod.isAccessibilitySettingsOn(getActivity())) {
                String mStrToast;
                if (enabled) {
                    mStrToast = getString(R.string.has_enabled);
                } else {
                    mStrToast = getString(R.string.has_disabled);
                }
                Toast.makeText(getActivity(), mStrToast, Toast.LENGTH_SHORT).show();
            } else {
                BaseMethod.RunAccessibilityService(getActivity());
            }
        }
    }

}
