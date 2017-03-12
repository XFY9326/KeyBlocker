package tool.xfy9326.keyblocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class GuideActivity extends Activity 
{
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();
		ButtonSet();
    }

	private void ButtonSet()
	{
		Button start = (Button) findViewById(R.id.button_start);
		start.setOnClickListener(new OnClickListener(){
				public void onClick(View v)
				{
					Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
					startActivity(intent);
				}
			});
		CheckBox volume = (CheckBox) findViewById(R.id.checkbox_volumebutton_blocked);
		volume.setChecked(sp.getBoolean("VolumeButton_Block", false));
		volume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton cb, boolean b)
				{
					sped.putBoolean("VolumeButton_Block", b);
					sped.commit();
				}
			});
		CheckBox force_notify = (CheckBox) findViewById(R.id.checkbox_force_notification_control);
		force_notify.setChecked(sp.getBoolean("ForceNotify", false));
		force_notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton cb, boolean b)
				{
					sped.putBoolean("ForceNotify", b);
					sped.commit();
					Toast.makeText(GuideActivity.this, R.string.restart_service, Toast.LENGTH_SHORT).show();
				}
			});
	}

}
