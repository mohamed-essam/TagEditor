package com.me.tageditor.FileBrowser;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import com.me.tageditor.R;
import com.me.tageditor.settings.SettingsContainer;

public class FileBrowser extends Activity {
	
	BrowserListAdapter adapter;
	ListView list;

	private boolean isDirectoryWritable(File file){
		try{
			File nfile = new File(file.getPath() + "/temptemptemp.temptemptemp");
			nfile.createNewFile();
			nfile.delete();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browser);
		adapter = new BrowserListAdapter();
		adapter.context = this;
		String DP = SettingsContainer.SettingsValues.get("DP");
		if(!new File(DP).exists() || !new File(DP).isDirectory() || !isDirectoryWritable(new File(DP))){
			DP = Environment.getExternalStorageDirectory().getPath();
			Toast.makeText(this, "Path specified in settings doesn't exist, or can't be accessed, resetting value...", Toast.LENGTH_LONG).show();
			SettingsContainer.SettingsValues.put("DP", DP);
			SettingsContainer.Save();
			adapter.paths.add(DP);
		}
		else{
			String path = new File(DP).getPath();
			//String arr[] = path.split("\\/");
			//adapter.paths.add(Environment.getExternalStorageDirectory().getPath() + "/");
			//for(int i = Environment.getExternalStorageDirectory().getPath().split("\\/").length; i < arr.length; i++){
				//Log.w("adapter.paths[" + (adapter.paths.size() - 1) + "]", adapter.paths.get(adapter.paths.size()- 1));
				//adapter.paths.add(adapter.paths.get(adapters.paths.size() - 1) + arr[i] + "/");
			//}
			//Log.w("adapter.paths[" + (adapter.paths.size() - 1) + "]", adapter.paths.get(adapter.paths.size()- 1));
			adapter.paths.add(path);
		}
		adapter.init();
		list = (ListView)findViewById(R.id.BrowserView);
		list.setAdapter(adapter);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_file_browser, menu);
		return false;
	}
	
	protected void retFile(String path){
		Intent intent = getIntent();
		intent.putExtra("com.me.tageditor.chosenFilePath", path);
		setResult(RESULT_OK, intent);
		finish();
	}

	protected void refresh(){
		BrowserListAdapter adapter = new BrowserListAdapter();
		adapter.context = this;
		adapter.paths = this.adapter.paths;
		adapter.init();
		list.setAdapter(adapter);
		this.adapter = adapter;
	}
	
	@Override
	public void onBackPressed(){
		if(adapter.paths.size() >= 2){
			BrowserListAdapter adap = new BrowserListAdapter();
			adap.context = adapter.context;
			adap.paths = adapter.paths;
			adap.paths.remove(adap.paths.size() - 1);
			adap.init();
			list.setAdapter(adap);
			this.adapter = adap;
		}
		else{
			super.onBackPressed();
		}
	}
}
