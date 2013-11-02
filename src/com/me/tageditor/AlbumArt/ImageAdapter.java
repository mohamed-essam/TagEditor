package com.me.tageditor.AlbumArt;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ImageAdapter extends ArrayAdapter<ImageView> {

	List<ImageView> images;
	
	public ImageAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		
	}
	
	public ImageAdapter(Context context, int textViewResourceId, List<ImageView> arr){
		super(context, textViewResourceId, arr);
		images = arr;
	}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup parent){
		return images.get(pos);
	}
	
}
