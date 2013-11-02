package com.me.tageditor.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Environment;

public class SettingsContainer {
	
	public static HashMap<String, String> SettingsValues;
	private static Context context = null;
	
	public static void Save(){
		Iterator<Entry<String, String>> it = SettingsContainer.SettingsValues.entrySet().iterator();
		String output = "";
		while(it.hasNext()){
			Map.Entry<String, String> pairs = (Entry<String, String>) it.next();
			if(output.length() > 0){
				output += ";";
			}
			output += pairs.getKey() + ":" + SettingsContainer.SettingsValues.get(pairs.getKey());
		}
		try {
			FileOutputStream stream = context.openFileOutput("Settings.set", Context.MODE_PRIVATE);
			stream.write(output.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void Load(Context _context){
		context = _context;
		SettingsContainer.SettingsValues = new HashMap<String, String>();
		try {
			FileInputStream settingStream = context.openFileInput("Settings.set");
			byte buffer[] = new byte[4096];
			String settings = "";
			int count = 0;
			while ((count = settingStream.read(buffer)) != -1) {
				settings += new String(buffer, 0, count);
			}
			String arr[] = settings.split(";");
			for (int i = 0; i < arr.length; i++) {
				String arr2[] = arr[i].split(":");
				if (arr2.length != 2) {
					continue;
				}
				String key, val;

				key = arr2[0];
				val = arr2[1];
				
				SettingsContainer.SettingsValues.put(key, val);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				FileOutputStream settingStream = context.openFileOutput("Settings.set",
						Context.MODE_PRIVATE);
				String defaultValue = "DP:" + Environment.getExternalStorageDirectory().getPath();
				settingStream.write(defaultValue.getBytes());
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
