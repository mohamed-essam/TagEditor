package com.me.tageditor.getMP3;

import java.util.ArrayList;


import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class FilesListAdapter extends BaseAdapter implements ListAdapter {

	ArrayList<SongClass> songs;
	GetMusic context;
	
	OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(final View arg0) {
			context.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					context.endThis(songs.get(arg0.getId()).HREF);
				}
			});
		}
	};
	
	public FilesListAdapter(ArrayList<SongClass> _songs) {
		songs = _songs;
	}

	@Override
	public int getCount() {
		return songs.size();
	}

	@Override
	public Object getItem(int arg0) {
		
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		SongClass wanted = songs.get(arg0);
		TextView Title = new TextView(context);
		Title.setText(wanted.Title);
		TextView OtherData = new TextView(context);
		String otherText = "Length: " + wanted.Length + " Size: " + wanted.Size + " Bitrate: " + wanted.Bitrate;
		OtherData.setText(otherText);
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(Title);
		layout.addView(OtherData);
		layout.setId(arg0);
		layout.setOnClickListener(listener);
		return layout;
	}

}
