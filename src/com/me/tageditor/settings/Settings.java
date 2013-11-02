package com.me.tageditor.settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.me.tageditor.R;

public class Settings extends Activity {
	
	HashMap<String, Integer> bindings = new HashMap<String, Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		bindings.put("DP", R.id.Settings_DefaultPath);
		((EditText)findViewById(R.id.Settings_DefaultPath)).setText(SettingsContainer.SettingsValues.get("DP"));
	}

	@Override
	public void onBackPressed(){
		Iterator<Entry<String, String>> it = SettingsContainer.SettingsValues.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, String> pairs = (Entry<String, String>) it.next();
			SettingsContainer.SettingsValues.put(pairs.getKey(), ((EditText)findViewById(bindings.get(pairs.getKey()))).getText().toString());
		}
		SettingsContainer.Save();
		super.onBackPressed();
	}
}
