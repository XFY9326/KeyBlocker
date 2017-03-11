package tool.xfy9326.keyblocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GuideActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);
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
	}

}
