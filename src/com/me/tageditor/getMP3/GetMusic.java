package com.me.tageditor.getMP3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.me.tageditor.R;

@SuppressLint("DefaultLocale")
public class GetMusic extends Activity {

	ListView list = null;
	FilesListAdapter adapter = null;
	
	GetMusic ActivityInstance = null;
	ProgressDialog dialog = null;
	
	String artistName, Title;
	
	OnClickListener InputListener = new OnClickListener() {
		
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			dialog = new ProgressDialog(ActivityInstance);
			dialog.setMessage("Searching...");
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(true);
			String artist, title, bitrate, size, minutes, seconds;

			AlertDialog dialog = (AlertDialog)arg0;
			EditText artistText = (EditText) dialog.findViewById(R.id.ArtistNameSearch);
			EditText TitleText = (EditText) dialog.findViewById(R.id.TitleSearch);
			EditText BitrateText = (EditText) dialog.findViewById(R.id.MinBitrate);
			EditText SizeText = (EditText) dialog.findViewById(R.id.MinSize);
			EditText MinLengthText = (EditText) dialog.findViewById(R.id.MinLengthMin);
			EditText SecLengthText = (EditText) dialog.findViewById(R.id.MinLengthSec);

			artist = artistText.getText().toString();
			artistName = artist;
			title = TitleText.getText().toString();
			Title = title;
			bitrate = BitrateText.getText().toString();
			if(bitrate.length() == 0){
				bitrate = "100";
			}
			size = SizeText.getText().toString();
			if(size.length() == 0){
				size = "1";
			}
			minutes = MinLengthText.getText().toString();
			if(minutes.length() == 0){
				minutes = "0";
			}
			seconds = SecLengthText.getText().toString();
			if(seconds.length() == 0){
				seconds = "0";
			}
			
			final getPageTask task = new getPageTask();
			task.execute(artist, title, bitrate, size, minutes, seconds);

			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface arg0) {
					task.cancel(true);
					setResult(RESULT_CANCELED);
					finish();
				}
			});
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_music);
		ActivityInstance = this;
		list = (ListView)findViewById(R.id.FilesList);
		
		
		
		Builder builder = new Builder(ActivityInstance);
		builder
			.setTitle("Enter Data")
			.setPositiveButton("Search", InputListener)
			.setNegativeButton("Cancel", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					setResult(RESULT_CANCELED);
					finish();
				}
			})
			.setView(getLayoutInflater().inflate(R.layout.mp3dialog, null));
		builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_get_music, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	protected void endThis(String link){
		Intent ret = getIntent();
		ret.putExtra("HREF", link);
		ret.putExtra("artist", artistName);
		ret.putExtra("title", Title);
		setResult(RESULT_OK, ret);
		finish();
	}

	private class getPageTask extends AsyncTask<String, Integer, String>{
		ArrayList<SongClass> songs = new ArrayList<SongClass>();

		private Elements getListDiv(String artist, String title){
			String query = artist + " " + title;
			query = query.replace(' ', '_');
			String URL = "http://mp3skull.com/mp3/" + query + ".html";
			Document SearchDoc = null;
			try {
				SearchDoc = Jsoup.connect(URL)
				.userAgent("Mozilla")
				.timeout(10000)
				.get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Elements SongsListDiv = null;
			try{
				SongsListDiv = SearchDoc.getElementsByAttributeValue("id", "song_html");
				if(SongsListDiv == null){
					throw new Exception();
				}
			}
			catch(Exception e){
				try{
					query = title + " " + artist;
					query = query.replace(' ', '_');
					URL = "http://mp3skull.com/mp3/" + query + ".html";
					SearchDoc = null;
					try {
						SearchDoc = Jsoup.connect(URL)
						.userAgent("Mozilla")
						.timeout(10000)
						.get();
					} catch (IOException e2) {
						e.printStackTrace();
					}
					SongsListDiv = SearchDoc.getElementsByAttributeValue("id", "song_html");
					if(SongsListDiv == null){
						throw new Exception();
					}
				} catch (Exception ee){
					ee.printStackTrace();
					if(SearchDoc.text().contains("The content was removed due to copyrights owners' request."))
						ActivityInstance.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(ActivityInstance, "Song search copyrighted, try removing part of the artist name, or removing it totally!", Toast.LENGTH_LONG).show();
							}
						});
					else{
						ActivityInstance.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(ActivityInstance, "No results!", Toast.LENGTH_LONG).show();
							}
						});
					}
					return null;
				}
			}
			return SongsListDiv;
		}
		
		private String getBitrateFromString(String DIV){
			Pattern bitratePattern = Pattern.compile("[0-9]+ kbps");
			Matcher bitrateMatcher = bitratePattern.matcher(DIV);
			String bitrateStr = "";
			if(bitrateMatcher.find()){
				bitrateStr = bitrateMatcher.group();
			}
			else{
				bitrateStr = "0 kbps";
			}
			return bitrateStr;
		}
		
		private String getLengthFromString(String DIV){
			Pattern LengthPattern = Pattern.compile("[0-9]+:[0-9]+");
			Matcher LengthMatcher = LengthPattern.matcher(DIV);
			String LengthStr = "";
			if(LengthMatcher.find()){
				LengthStr = LengthMatcher.group();
			}
			else{
				LengthStr = "0:0";
			}
			return LengthStr;
		}
		
		private String getSizeFromString(String DIV){
			Pattern SizePattern = Pattern.compile("[0-9\\.] mb");
			Matcher SizeMatcher = SizePattern.matcher(DIV);
			String SizeStr;
			if(SizeMatcher.find()){
				SizeStr = SizeMatcher.group();
			}
			else{
				SizeStr = "0 mb";
			}
			return SizeStr;
		}
		
		private void initSongs(Elements searchResults, int bitrateThreshold, int SizeThreshold, int LengthInSecondsThreshold){
			for (int i = 0; i < searchResults.size(); i++) {
				Element Left, Right;
				Element Div = searchResults.get(i);
				Left = Div
						.getElementsByClass("left")
						.get(0);
				Right = Div
						.getElementById("right_song");
				String bitrateStr, LengthStr, SizeStr;
				String leftText = Left.html();
				bitrateStr = getBitrateFromString(leftText);
				LengthStr = getLengthFromString(leftText);
				SizeStr = getSizeFromString(leftText);
				int bitrate = Integer.parseInt(bitrateStr.substring(0, bitrateStr.indexOf(' ')));
				if(bitrate < bitrateThreshold){
					continue;
				}
				int lengthInSeconds = Integer.parseInt(LengthStr.substring(0, LengthStr.indexOf(':'))) * 60 + Integer.parseInt(LengthStr.substring(LengthStr.indexOf(':') + 1));
				if(lengthInSeconds < LengthInSecondsThreshold){
					continue;
				}
				double Size = Double.parseDouble(SizeStr.substring(0, SizeStr.indexOf(' ')));
				if(Size < SizeThreshold){
					continue;
				}
				Element TitleElement = Right
						.getElementsByAttributeValue("style", "font-size:15px;")
						.get(0);
				Element DownloadHREF = Right
						.getElementsByAttributeValue("style", "float:left;")
						.get(0)
						.getElementsByAttributeValueContaining("style", "float:left;")
						.get(0)
						.getElementsByAttributeValueContaining("style", "float:left;")
						.get(0)
						.getElementsByTag("a")
						.get(0);
				String TitleStr = TitleElement.text();
				String TitleStrLower = TitleElement.text().toLowerCase();
				if(TitleStrLower.contains("remix") || TitleStrLower.contains("mix") || TitleStrLower.contains("dj") || TitleStrLower.contains("edit") || TitleStrLower.contains("extended") || TitleStrLower.contains("demo")){
					continue;
				}
				String downloadLink = DownloadHREF.attr("href");
				SongClass song = new SongClass(downloadLink, TitleStr, bitrateStr, LengthStr, SizeStr);
				songs.add(song);
			}

		}
		
		@Override
		protected String doInBackground(String... arg0) {
			try{
				String artistName = arg0[0];
				String Title = arg0[1];
				Elements SongsListDiv = getListDiv(artistName, Title);
				if(SongsListDiv == null){
					return null;
				}
				String bitrate, size, minutes, seconds;
				bitrate = arg0[2];
				size = arg0[3];
				minutes = arg0[4];
				seconds = arg0[5];
				initSongs(SongsListDiv, Integer.parseInt(bitrate), Integer.parseInt(size), Integer.parseInt(minutes) * 60 + Integer.parseInt(seconds));
				Log.w("GET", "Got songs<SongClass>");
			} catch (Exception e){
				
			}
			return null;
		}
		
		@Override
		protected void onPreExecute(){
			dialog.show();
		}
		
		@Override
		protected void onProgressUpdate(Integer...integers){
			
		}
		
		@Override
		protected void onPostExecute(String result){
			if(songs != null){
				if(songs.size() == 0){
					//Toast.makeText(ActivityInstance, "No Results Found!", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					setResult(RESULT_CANCELED);
					finish();
					return;
				}
				FilesListAdapter adapter = new FilesListAdapter(songs);
				adapter.context = ActivityInstance;
				list.setAdapter(adapter);
			}
			dialog.dismiss();
			setResult(RESULT_CANCELED);
			finish();
		}
		
	}
}
