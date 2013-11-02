package com.me.tageditor.AlbumArt;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.me.tageditor.R;

public class AlbumArtChooser extends Activity {
	ProgressDialog dialog = null;
	AlbumArtChooser ActivityInstance = null;
	List<String> Links; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ActivityInstance = this;
		final Intent data = this.getIntent();
		setContentView(R.layout.albumchooser_layout);
		final GridView grid = (GridView)findViewById(R.id.Grid);
		final String artistName = data.getStringExtra("com.me.tageditor.ArtistName");
		final String title = data.getStringExtra("com.me.tageditor.SongTitle");
		String album = data.getStringExtra("com.me.tageditor.AlbumTitle");
		String q = "";
		if(album != ""){
			q = (artistName + " " + album).replace(' ', '+');
		}
		else{
			q = (artistName + " " + artistName).replace(' ', '+');
		}
		final String query = q;
		Links = new ArrayList<String>();
		final List<ImageView> images = new ArrayList<ImageView>();
		Log.w("ART", query);
		new Thread(new Runnable() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
					Document SearchPage = null;
					try {
						SearchPage = Jsoup.connect("http://www.amazon.com/gp/search/ref=sr_adv_m_pop/?search-alias=popular&unfiltered=1&field-keywords=&field-label=&field-binding=&sort=relevancerank&Adv-Srch-Music-Album-Submit.x=38&Adv-Srch-Music-Album-Submit.y=15&" + "field-artist=" + artistName.replace(' ', '+') + "&field-title=" + title.replace(' ', '+'))
								.timeout(10000)
								.userAgent("Mozilla")
								.get();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Elements searchResults = SearchPage
							.getElementsByTag("html")
							.get(0)
							.getElementsByTag("body")
							.get(0)
							.getElementById("main")
							.getElementById("searchTemplate")
							.getElementById("rightContainerATF")
							.getElementById("rightResultsATF")
							.getElementById("center")
							.getElementById("atfResults")
							.getElementsByTag("div");
					Log.w("ART", "searchResults = " + searchResults.size());
					int x2 = 0;
					for(int i = 0; i < searchResults.size() && i < 10; i++){
						if(!searchResults.get(i).id().contains("result")){
							continue;
						}
						Element a = searchResults
								.get(i)
								.getElementsByAttributeValue("class", "image imageContainer")
								.get(0)
								.getElementsByTag("a")
								.get(0);
						Element img = a
								.getElementsByTag("img")
								.get(0);
						String href = a.attr("href");
						String imgSrc = img.attr("src");
						Log.w("ART", "Got img src : " + imgSrc);
						HttpURLConnection conn = null;
						try {
							conn = ((HttpURLConnection)new URL(imgSrc).openConnection());
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(conn == null)
							continue;
						conn.setRequestProperty("User-agent", "Mozilla/4.0");
						try {
							conn.connect();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						InputStream stream = null;
						try {
							stream = conn.getInputStream();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Bitmap x = BitmapFactory.decodeStream(stream);
						BitmapDrawable d = new BitmapDrawable(x);
						final ImageView view = new ImageView(ActivityInstance);
						view.setBackgroundDrawable(d);
						view.setId(x2);
						x2++;
						Links.add(href);
						view.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								data.putExtra("com.me.tageditor.AlbumLink", Links.get(arg0.getId()));
								setResult(RESULT_OK, data);
								finish();
							}
						});
						ActivityInstance.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								images.add(view);
							}
						});
					}
					ActivityInstance.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							grid.setAdapter(new ImageAdapter(ActivityInstance, R.id.Grid, images));
						}
					});
			}
		}).start();
	}
}
