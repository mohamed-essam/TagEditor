package com.me.tageditor.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<String, Integer, String> {

	MainActivity context;
	String path = "";
	
	public DownloadTask(MainActivity _context){
		context = _context;
	}
	
	@SuppressWarnings("resource")
	@Override
	protected String doInBackground(String... arg0) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wl.acquire();
		path = arg0[1];
		try{
			InputStream is = null;
			OutputStream os = null;
			HttpURLConnection http = null;
			
			try{
				String url = arg0[0];
				String b4, aft;
				b4 = url.substring(0, url.lastIndexOf('/'));
				aft = url.substring(url.lastIndexOf('/') + 1);
				aft = URLEncoder.encode(aft, "UTF-8");
				url = b4 + "/" + aft;
				url = url.replaceAll("\\+", "%20").replaceAll(" ", "%20");
				Log.w("HREF", url);
				
				HttpGet get = new HttpGet(url);
				HttpResponse response = new DefaultHttpClient().execute(get);
				
				if(response.getStatusLine().getStatusCode() != 200){
					if(wl.isHeld())
						wl.release();
					return "Error Server: " + response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase();
				}
				
				
				long fileLength = response.getEntity().getContentLength();
				
				is = response.getEntity().getContent();
				File file = new File(arg0[1]);
				file.mkdirs();
				file.delete();
				file.createNewFile();
				os = new FileOutputStream(arg0[1]);
				
				byte data[] = new byte[4096];
				long total = 0;
				int count = 0;
				while((count = is.read(data)) != -1){
					if(isCancelled()){
						os.close();
						is.close();
						if(wl.isHeld())
							wl.release();
						return "Error Cancelled";
					}
					total += count;
					if(fileLength > 0){
						publishProgress((int)total/1024, (int)fileLength/1024);
					}
					os.write(data, 0, count);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try{
					if(is != null)
						is.close();
					if(os != null)
						os.close();
				} catch (Exception ignored) { }
				if(http != null)
					http.disconnect();
				if(wl.isHeld())
					wl.release();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if(wl.isHeld())
				wl.release();
			return "Error " + e.getMessage();
		}
		if(wl.isHeld())
			wl.release();
		return context.moveToCorrectLocation();
		//return "Success";
	}

	
	
	@Override
	protected void onPreExecute(){
		super.onPreExecute();
		context.DownloadDialog.show();
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress){
		super.onProgressUpdate(progress);

		context.DownloadDialog.setIndeterminate(false);
		context.DownloadDialog.setMax(progress[1]);
		context.DownloadDialog.setProgress(progress[0]);
	}
	
	@Override
	protected void onPostExecute(String result){
		if(result.contains("Error")){
			Toast.makeText(context, result, Toast.LENGTH_LONG).show();
		}
		else{
			Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
		}
		context.DownloadDialog.dismiss();
		
		context.openFile(new File(result));
	}
	
}
