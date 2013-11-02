package com.me.tageditor.FileBrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.database.DataSetObserver;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.me.tageditor.R;

public class BrowserListAdapter extends BaseAdapter implements ListAdapter {

	public FileBrowser context;
	public ArrayList<String> paths;
	ArrayList<String> cached = null;
	ArrayList<View> cachedViews;
	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			ArrayList<String> all = getAll();
			final String path = all.get(arg0.getId());
			File file = new File(path);
			if(file.isDirectory()){
				if(paths.size() - 2 >= 0){
					if(paths.get(paths.size() - 2).contains(file.getPath()) && paths.get(paths.size() - 2).length() - 1 <= file.getPath().length()){
						paths.remove(paths.size() - 1);
					}
					else{
						paths.add(path);
					}
				}
				else{
					paths.add(path);
				}
				context.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						context.refresh();
					}
				});
			}
			else{
				context.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						context.retFile(path);
					}
				});
			}
		}
	};

	public BrowserListAdapter() {
		paths = new ArrayList<String>();
		cachedViews = new ArrayList<View>();
	}
	
	public void init(){
		for (int i = 0; i < getAll().size(); i++) {
			cachedViews.add(null);
		}
	}

	ArrayList<String> getAll(){
		if(cached != null)
			return cached;
		String[] all = new File(paths.get(paths.size() - 1)).list();
		if(all == null){
			context.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					context.onBackPressed();
					Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show();
				}
			});
		}
		ArrayList<String>filtered = new ArrayList<String>(),
				Folders = new ArrayList<String>();
		for (int i = 0; i < all.length; i++) {
			File ith = new File(paths.get(paths.size()-1) + "/" + all[i]);
			if(ith.isDirectory()){
				Folders.add(ith.getPath());
			}
			else{
				String mime = getMimeType(ith.getPath());
				if(mime == "audio/mpeg" || mime == "audio/x-mpeg"){
					filtered.add(ith.getPath());
				}
			}
		}
		Collections.sort(Folders, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	            return s1.compareToIgnoreCase(s2);
	        }
	    });
		if(paths.size() - 2 >= 0)
			Folders.add(0, paths.get(paths.size() - 2));
		Collections.sort(filtered, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	            return s1.compareToIgnoreCase(s2);
	        }
	    });
		Folders.addAll(filtered);
		this.cached = Folders;
		return Folders;
	}
	
	@Override
	public int getCount() {
		return getAll().size();
	}

	public String getMimeType(String url)
	{
		if(url.endsWith(".mp3")){
			return "audio/mpeg";
		}
	    return "";
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
	public int getItemViewType(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		if(cachedViews.get(arg0) != null){
			return cachedViews.get(arg0);
		}
		ArrayList<String> Folders = getAll();
		try{
			String wantedPath = Folders.get(arg0);
			TextView ret = new TextView(this.context);
			ret.setText(wantedPath.substring(wantedPath.lastIndexOf('/') + 1));
			ret.setTextSize(18);
			int tenDP = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
			ret.setPadding(0, tenDP, 0, tenDP);
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			ImageView img = new ImageView(context);
			if(new File(wantedPath).isDirectory()){
				if(paths.size() >= 2 && wantedPath == paths.get(paths.size() - 2)){
					img.setBackgroundResource(R.drawable.up_folder_icon);
					ret.setText("..");
				}
				else{
					img.setBackgroundResource(R.drawable.folder_icon);
				}
			}
			else{
				img.setBackgroundResource(R.drawable.audio_icon);
			}
			int fiftyDP = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());
			img.setLayoutParams(new LayoutParams(fiftyDP, fiftyDP));
			layout.addView(img);
			layout.addView(ret);
			layout.setOnClickListener(listener);
			layout.setId(arg0);
			cachedViews.set(arg0, layout);
			return layout;
		} catch (IndexOutOfBoundsException e) {
			return new TextView(this.context);
		}
	}

	@Override
	public int getViewTypeCount() {
		return getAll().size();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {

	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}
}
