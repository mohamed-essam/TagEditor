package com.me.tageditor.Main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.me.tageditor.R;
import com.me.tageditor.AlbumArt.AlbumArtChooser;
import com.me.tageditor.FileBrowser.FileBrowser;
import com.me.tageditor.getMP3.GetMusic;
import com.me.tageditor.settings.Settings;
import com.me.tageditor.settings.SettingsContainer;

public class MainActivity extends Activity {

	AudioFile file = null;
	EditText LyricsText, ArtistText, AlbumText, TitleText;

	Menu optionsMenu = null;

	ProgressDialog LyricsFillDialog = null;
	ProgressDialog OpenDialog = null;
	ProgressDialog SaveDialog = null;
	ProgressDialog DownloadDialog = null;

	static MainActivity ActivityInstance;

	Thread filler = null;

	OnClickListener OpenButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ActivityInstance, FileBrowser.class);
			startActivityForResult(intent, 1);
		}
	};
	OnClickListener SaveButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (file == null) {
				Toast.makeText(ActivityInstance, "Open a file first!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			SaveDialog = ProgressDialog.show(ActivityInstance, "Saving",
					"Saving..");
			new Thread(new Runnable() {

				@Override
				public void run() {
					Tag tag = file.getTag();
					Log.e("Clicked", "Saving");
					try {
						tag.setField(FieldKey.LYRICS, LyricsText.getText()
								.toString());
						tag.setField(FieldKey.ARTIST, ArtistText.getText()
								.toString());
						tag.setField(FieldKey.ALBUM_ARTIST, ArtistText
								.getText().toString());
						tag.setField(FieldKey.ALBUM, AlbumText.getText()
								.toString());
						tag.setField(FieldKey.TITLE, TitleText.getText()
								.toString());
					} catch (KeyNotFoundException e) {
						e.printStackTrace();
					} catch (FieldDataInvalidException e) {
						e.printStackTrace();
					}
					try {
						file.commit();
					} catch (CannotWriteException e) {
						e.printStackTrace();
					}
					
//					try {
//						MusicMetadataSet set = new MyID3().read(file.getFile());
//						MusicMetadata d = (MusicMetadata) set.getSimplified();
//						d.clearPictureList();
//						ImageView img = (ImageView)findViewById(R.id.AlbumArt);
//						Bitmap p = ((BitmapDrawable)img.getDrawable()).getBitmap();
//						ByteArrayOutputStream str = new ByteArrayOutputStream();
//						p.compress(Bitmap.CompressFormat.PNG, 100, str);
//						byte[] data = str.toByteArray();
//						d.addPicture(new ImageData(data, "", "", 3));
//						File dst = new File(Environment.getExternalStorageDirectory().getPath() + "/.cache");
//						new MyID3().write(file.getFile(), dst, set, d);
//						FileInputStream is = new FileInputStream(dst);
//						FileOutputStream os = new FileOutputStream(file.getFile());
//						byte[] buffer = new byte[1024];
//						int count = 0;
//						while((count = is.read(buffer)) != -1){
//							os.write(buffer, 0, count);
//						}
//						is.close();
//						os.close();
//						dst.delete();
//					} catch (IOException e) {
//						e.printStackTrace();
//					} catch (ID3WriteException e) {
//						e.printStackTrace();
//					}
					
					ActivityInstance.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							SaveDialog.dismiss();
						}
					});
				}
			}).start();
		}
	};
	OnClickListener FillButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			LyricsFillDialog = ProgressDialog.show(ActivityInstance,
					"Loading Lyrics", "Loading Lyrics from the internet");
			final String artist = ArtistText.getText().toString();
			final String title = TitleText.getText().toString();
			filler = new Thread(new Runnable() {

				@Override
				public void run() {
					if (file == null)
						return;
					String searchQuery = artist + " " + title;
					searchQuery = searchQuery.replace(' ', '+');
					Document SearchDoc = null;
					try {
						SearchDoc = Jsoup
								.connect(
										"http://www.songlyrics.com/index.php?section=search&searchW="
												+ searchQuery)
								.userAgent("Mozilla").timeout(10000).get();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (SearchDoc == null)
						return;
					Element FirstResult = null;
					try {
						FirstResult = SearchDoc.getElementsByTag("html").get(0)
								.getElementsByTag("body").get(0)
								.getElementById("wrapper")
								.getElementsByClass("wrapper-inner").get(0)
								.getElementsByClass("coltwo-wide-2").get(0)
								.getElementsByClass("serpresult").get(0);
					} catch (NullPointerException e) {
						e.printStackTrace();
						Toast.makeText(ActivityInstance, "No lyrics found",
								Toast.LENGTH_SHORT).show();
					}
					try {
						Element ResultLink = FirstResult.getElementsByTag("a")
								.get(0);
						Document LyricsDoc = Jsoup
								.connect(ResultLink.attr("href"))
								.userAgent("Mozilla").timeout(10000).get();
						Element LyricsDiv = LyricsDoc.getElementsByTag("html")
								.get(0).getElementsByTag("body").get(0)
								.getElementById("wrapper");
						Elements els = LyricsDiv.getElementsByTag("div");
						Element correct = null;
						for (int i = 0; i < els.size(); i++) {
							if (els.get(i).className()
									.contains("wrapper-inner")) {
								correct = els.get(i);
								break;
							}
						}
						els = correct.getElementById("colone-container")
								.getElementsByTag("div");
						for (int i = 0; i < els.size(); i++) {
							if (els.get(i).className().contains("col-one")) {
								correct = els.get(i);
								break;
							}
						}
						LyricsDiv = correct
								.getElementById("songLyricsContainer")
								.getElementById("songLyricsDiv-outer")
								.getElementById("songLyricsDiv");
						final String text = LyricsDiv.html()
								.replaceAll("<br />", "\n")
								.replaceAll("\"", "").replaceAll("\n ", "\n")
								.replaceAll(" \n", "\n");
						LyricsText.post(new Runnable() {

							@Override
							public void run() {
								if (!text
										.contains("We do not have the lyrics for "))
									LyricsText.setText(text);
							}
						});
						ActivityInstance.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								LyricsFillDialog.dismiss();
								if (text.contains("We do not have the lyrics for ")) {
									Toast.makeText(ActivityInstance,
											"No lyrics found!",
											Toast.LENGTH_SHORT).show();
								}
							}
						});
					} catch (NullPointerException e) {
						e.printStackTrace();
						Toast.makeText(ActivityInstance,
								"Unexpected Error : " + e.getMessage(),
								Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			filler.start();

		}
	};
	OnClickListener AlbumChooseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent launcher = new Intent(ActivityInstance,
					AlbumArtChooser.class);
			launcher.putExtra("com.me.tageditor.ArtistName",
					ArtistText.getText().toString())
					.putExtra("com.me.tageditor.SongTitle",
							TitleText.getText().toString())
					.putExtra("com.me.tageditor.AlbumTitle",
							AlbumText.getText().toString());
			startActivityForResult(launcher, 2);
		}
	};
	OnClickListener GetMusicListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Intent launcher = new Intent(ActivityInstance, GetMusic.class);
			launcher.putExtra("com.me.tageditor.ArtistName", ArtistText
					.getText().toString());
			launcher.putExtra("com.me.tageditor.Title", TitleText.getText()
					.toString());
			startActivityForResult(launcher, 3);
		}
	};

	private void initializeSettings() {
		SettingsContainer.Load(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityInstance = this;
		setContentView(R.layout.activity_main);
		LyricsText = (EditText) findViewById(R.id.Lyrics);
		TitleText = (EditText) findViewById(R.id.Title);
		ArtistText = (EditText) findViewById(R.id.Artist);
		AlbumText = (EditText) findViewById(R.id.Album);
		initializeSettings();
		try {
			LyricsText.setText(savedInstanceState.getString("LyricsText"));
			TitleText.setText(savedInstanceState.getString("TitleText"));
			ArtistText.setText(savedInstanceState.getString("ArtistText"));
			AlbumText.setText(savedInstanceState.getString("AlbumText"));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		try {
			if (savedInstanceState.getBoolean("Create Dialog")) {
				try {
					filler.interrupt();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		try {
			file = AudioFileIO.read(new File(savedInstanceState
					.getString("Path")));
		} catch (CannotReadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TagException e) {
			e.printStackTrace();
		} catch (ReadOnlyFileException e) {
			e.printStackTrace();
		} catch (InvalidAudioFrameException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(file
					.getTag().getFirstArtwork().getBinaryData());
			((ImageView) findViewById(R.id.AlbumArt))
					.setBackgroundDrawable(new BitmapDrawable(BitmapFactory
							.decodeStream(stream)));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		menu.findItem(R.id.menu_autofill).setEnabled(file != null);
		menu.findItem(R.id.menu_save).setEnabled(file != null);
		optionsMenu = menu;
		return true;
	}

	protected void openFile(final File file){
		OpenDialog = ProgressDialog.show(ActivityInstance,
				"Loading File", "Loading File");
		new Thread(new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				try {
					ActivityInstance.file = AudioFileIO.read(file);
					Log.w("MUSIC", "Audio File Created");
				} catch (CannotReadException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TagException e) {
					e.printStackTrace();
				} catch (ReadOnlyFileException e) {
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) {
					e.printStackTrace();
				}
				if (ActivityInstance.file == null) {
					ActivityInstance.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(ActivityInstance,
									"Cannot read file!",
									Toast.LENGTH_SHORT).show();
							OpenDialog.dismiss();
						}
					});
					return;
				}
				Tag tag = ActivityInstance.file.getTag();
				final String LyricsString = tag
						.getFirst(FieldKey.LYRICS);
				final String ArtistString = tag
						.getFirst(FieldKey.ARTIST);
				final String TitleString = tag
						.getFirst(FieldKey.TITLE);
				final String AlbumString = tag
						.getFirst(FieldKey.ALBUM);
				if (LyricsString.length() == 0) {
					ActivityInstance.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(ActivityInstance,
									"No lyrics found in the file",
									Toast.LENGTH_SHORT).show();
						}
					});
				}
				ActivityInstance.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						LyricsText.setText(LyricsString);
						ArtistText.setText(ArtistString);
						TitleText.setText(TitleString);
						AlbumText.setText(AlbumString);
					}
				});
				final ImageView img = (ImageView) findViewById(R.id.AlbumArt);
				try {
					Artwork art = tag.getFirstArtwork();
					InputStream stream = new ByteArrayInputStream(
							art.getBinaryData());
					final BitmapDrawable d = new BitmapDrawable(
							BitmapFactory.decodeStream(stream));
					ActivityInstance.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							img.setBackgroundDrawable(d);
						}
					});
				} catch (NullPointerException e) {
					e.printStackTrace();
					ActivityInstance.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(ActivityInstance,
									"No Album Art found!",
									Toast.LENGTH_SHORT).show();
							img.setBackgroundResource(R.drawable.albumart);
						}
					});
				}
				ActivityInstance.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						optionsMenu.findItem(R.id.menu_autofill)
								.setEnabled(file != null);
						optionsMenu.findItem(R.id.menu_save)
								.setEnabled(file != null);
						optionsMenu.findItem(R.id.menu_art)
								.setEnabled(true);
						OpenDialog.dismiss();
					}
				});
			}
		}).start();

	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				try {
					// String path = data.getData().getPath();
					String path = data
							.getStringExtra("com.me.tageditor.chosenFilePath");
					Log.w("MUSIC", path);
					final File file = new File(path);
					Log.w("MUSIC", "File Created");
					openFile(file);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == 2) {
				Log.e("ALBUM RETURNED",
						data.getStringExtra("com.me.tageditor.AlbumLink"));
				final String href = data
						.getStringExtra("com.me.tageditor.AlbumLink");
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Log.w("ART RETURNED", "Initiating connection");
							Document ImagePage = Jsoup
									.connect("http://www.amazon.com" + href)
									.timeout(10000).userAgent("Mozilla").get();
							Log.w("ART RETURNED", "Got Page");
							Element img = ImagePage
									.getElementsByTag("html")
									.get(0)
									.getElementsByTag("body")
									.get(0)
									.getElementsByClass("singlecolumnminwidth")
									.get(0)
									.getElementById("handleBuy")
									.getElementsByClass("productImageGrid")
									.get(0)
									.getElementsByTag("tbody")
									.get(0)
									.getElementsByTag("td")
									.get(0)
									.getElementById("main-image-widget")
									.getElementById("main-image-content")
									.getElementById(
											"main-image-relative-container")
									.getElementById(
											"main-image-fixed-container")
									.getElementById("main-image-wrapper-outer")
									.getElementById("main-image-wrapper")
									.getElementById("holderMainImage")
									.getElementsByTag("noscript").get(0)
									.getElementsByTag("img").get(0);
							Log.w("ART RETURNED", "Got img");
							String src = img.attr("src");
							Log.w("ART RETURNED", "Got img src : " + src);
							HttpURLConnection conn = null;
							try {
								conn = ((HttpURLConnection) new URL(src)
										.openConnection());
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (conn == null)
								return;
							conn.setRequestProperty("User-agent", "Mozilla/4.0");
							try {
								conn.connect();
							} catch (IOException e) {
								e.printStackTrace();
							}
							InputStream stream = null;
							try {
								stream = conn.getInputStream();
							} catch (IOException e) {
								e.printStackTrace();
							}
							Bitmap x = BitmapFactory.decodeStream(stream);
							try {
								FileOutputStream out = new FileOutputStream(
										Environment
												.getExternalStorageDirectory()
												+ "/img.png");
								x.compress(Bitmap.CompressFormat.PNG, 90, out);
								out.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							final BitmapDrawable d = new BitmapDrawable(x);
							ActivityInstance.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									ImageView view = (ImageView) findViewById(R.id.AlbumArt);
									view.setBackgroundDrawable(d);
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			} else if (requestCode == 3) {
				Log.w("GOT SONG HREF!!!", data.getStringExtra("HREF"));
				DownloadDialog = new ProgressDialog(MainActivity.this);
				DownloadDialog.setMessage("Downloading...");
				DownloadDialog.setIndeterminate(true);
				DownloadDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				DownloadDialog.setCancelable(true);

				final DownloadTask task = new DownloadTask(ActivityInstance);
				
				
				
				task.execute(data.getStringExtra("HREF"), Environment
						.getExternalStorageDirectory().getPath()
						+ "/music/"
						+ data.getStringExtra("artist")
						+ "/"
						+ data.getStringExtra("title") + ".mp3");
				DownloadDialog.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						task.cancel(false);
					}
				});
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("TitleText", TitleText.getText().toString());
		outState.putString("LyricsText", LyricsText.getText().toString());
		outState.putString("ArtistText", ArtistText.getText().toString());
		outState.putString("AlbumText", AlbumText.getText().toString());
		if (file != null)
			outState.putString("Path", file.getFile().getPath());
		if (LyricsFillDialog != null && LyricsFillDialog.isShowing()) {
			outState.putBoolean("Create Dialog", true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_open:
			OpenButtonListener.onClick(null);
			break;
		case R.id.menu_save:
			SaveButtonListener.onClick(null);
			break;
		case R.id.menu_autofill:
			FillButtonListener.onClick(null);
			break;
		case R.id.menu_art:
			AlbumChooseListener.onClick(null);
			break;
		case R.id.menu_getsongs:
			GetMusicListener.onClick(null);
			break;
		case R.id.menu_settings:
			Intent intent = new Intent();
			intent.setClass(ActivityInstance, Settings.class);
			startActivity(intent);
			break;
		}
		return false;
	}

}
