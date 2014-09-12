package cgnet.swara.activity;

import java.io.File;  

import android.net.Uri;  
import android.util.Log;  
import android.view.View;
import net.fred.feedex.R;
import android.os.Bundle;

import java.util.Calendar;
import java.io.IOException; 

import android.app.Activity;  
import android.view.KeyEvent;

import java.io.FileInputStream; 

import android.app.AlertDialog;
import android.widget.Toast;
import android.os.SystemClock; 
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.database.Cursor;
import android.graphics.Bitmap; 
import android.widget.ImageView; 
import android.location.Criteria;
import android.location.Location;
import android.media.MediaPlayer;  
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.provider.MediaStore;
import android.graphics.BitmapFactory;
import android.content.DialogInterface;
import net.fred.feedex.MainApplication; 
import android.location.LocationManager;
import android.location.LocationListener;
import android.view.View.OnClickListener;
import android.media.MediaMetadataRetriever;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker; 

import net.fred.feedex.MainApplication.TrackerName;

import com.google.android.gms.analytics.HitBuilders;

import android.media.MediaPlayer.OnCompletionListener; 


/** This screen allows the user to record an audio message.
 *  They can then chose to send the recording off to a central location. 
 *  The user is able to listen to the recording prior to sending the off.  
 *   
 *  @author Krittika D'Silva (krittika.dsilva@gmail.com) */
public class RecordAudio extends Activity implements LocationListener {
	private static final String TAG = "RecordAudio";

	/** CGNet Swara's main directory with audio files. */
	private String mMainDir;

	/** Folder containing all audio files that have yet to be sent. */
	private final String mInnerDir = "/ToBeSent";

	/** Name of the audio file created.	*/
	private String mUniqueAudioRecording;

	/** Plays back the users voice recording. */
	private MediaPlayer mUserAudio; 
   
	/** Starts recording audio.  */
	private ImageButton mStart;

	/** Stops recording audio. */
	private ImageButton mStop;

	/** Plays back the audio that the user recorded. */
	private ImageButton mPlayback;

	/** Discards the audio recording created and returns the user to the
	 *  main menu.  */
	private ImageButton mBack;

	/** Sends audio recording to a central location if there's an Internet 
	 *  connection, if not saves the audio recording in a 
	 *  known folder - to be sent later. */
	private ImageButton mSendAudio;
 
	/** The action code we use in our intent, 
	 *  this way we know we're looking at the response from our own action.  */
	private static final int SELECT_PICTURE = 1;

	/** Saves logs about the user */
	private SaveAudioInfo mUserLogs;

	/** True if the created audio file should be sent, false otherwise. */
	private boolean mFileToBeSent;

	/** Shows the amount of time left - audio recordings must be less 
	 *  than 3 minutes. */
	private Chronometer chronometer;
	
	/** If the user wants to send an image with their audio file, their chosen 
	 *  image is shown as they record audio. */
	private ImageView mUserImage;

	/** The users phone number - inputed on the main screen. */
	private String mPhoneNumber; 
 
	/** Used to show the users chosen image.*/
	private Bitmap bitmap = null;
	    
	/** Audio recorder that records the users' message. */
	private RecMicToMp3 mRecMicToMp3;
	 
	private int latituteField;

	private int longitudeField;
	
	private LocationManager locationManager;
	
	private String provider;
	
	private boolean includePhoto;
	
	private boolean doneRecording;
	 
	private	Tracker t;
	
	private int mCountPlaybacks = 0;
	 
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.record_audio); 

		mStart = (ImageButton) findViewById(R.id.start);
		mStop = (ImageButton) findViewById(R.id.stop);
		mPlayback = (ImageButton) findViewById(R.id.playback);
		mSendAudio = (ImageButton) findViewById(R.id.sendAudio); 
		mUserImage = (ImageView) findViewById(R.id.userImage);
		mBack = (ImageButton) findViewById(R.id.backToMain);
		chronometer = (Chronometer) findViewById(R.id.time);
		
		mFileToBeSent = false; 
		
		// At first, the only option the user has is to record audio
		mStart.setVisibility(View.VISIBLE); 
		mStop.setVisibility(View.GONE);
		mPlayback.setVisibility(View.GONE);
		mSendAudio.setVisibility(View.GONE); 
		mBack.setVisibility(View.INVISIBLE);
		findViewById(R.id.time).setVisibility(View.INVISIBLE);
		
		includePhoto = false;
		doneRecording = false;
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras(); 
		includePhoto = extras.getBoolean("photo"); 
		mPhoneNumber = extras.getString("phone");

		t = ((MainApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
 		        		
		if(includePhoto) { 
			Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(Intent.createChooser(i,
					this.getString(R.string.select_picture)), SELECT_PICTURE);
			t.setScreenName("Record Audio - With an image");
		} else { 
			mUserImage.setVisibility(View.GONE);
			t.setScreenName("Record Audio - Without an image");
		}
		
		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());
 
		// Create folders for the audio files 
		setupDirectory();
		
		latituteField = -1;
	    longitudeField = -1;
	    // Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	     
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);
	    
	    // Initialize the location fields
	    if (location != null) {
	    	Log.e(TAG, "Provider " + provider + " has been selected.");
	    	onLocationChanged(location);
	    } else {
	    	Log.e(TAG, "location null");
	    }
	     
		chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() { 
            @Override
            public void onChronometerTick(Chronometer chronometer) { 
                if(chronometer.getText().toString().equalsIgnoreCase("2:59") || 
                   chronometer.getText().toString().equalsIgnoreCase("02:59")) { 
                  mStop.performClick();
                }
            }
        });
		
		mStart.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) { 
				findViewById(R.id.time).setVisibility(View.VISIBLE);
				mStart.setVisibility(View.GONE);
				mStop.setVisibility(View.VISIBLE);
				chronometer.setBase(SystemClock.elapsedRealtime());
				chronometer.start();
		 
				startRecording(); 
			}  
		}); 

		mStop.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) {  
				chronometer.stop();  
				mStop.setVisibility(View.GONE); 
				mPlayback.setVisibility(View.VISIBLE);
				mSendAudio.setVisibility(View.VISIBLE); 
				mBack.setVisibility(View.VISIBLE); 
				stopRecording(); 
			}  
		});

		mPlayback.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) {
				if(mUserAudio != null && mUserAudio.isPlaying()) {
					mUserAudio.pause();
					mPlayback.setImageResource(R.drawable.play_icon);
				} else {
					mCountPlaybacks++;
					mStart.setVisibility(View.GONE);
					mStop.setVisibility(View.GONE); 
					mPlayback.setVisibility(View.VISIBLE); 
					mSendAudio.setVisibility(View.VISIBLE); 
					startPlaying();
				} 
			}
		});

		mSendAudio.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) {  
				sendData(); 
				Toast.makeText(RecordAudio.this, RecordAudio.this.getString(R.string.file_sent),  
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(RecordAudio.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}); 

		mBack.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) { 
				goBackHome();		  
				if(bitmap != null) { 
					Log.e(TAG, "Recycling bitmap.");
					bitmap.recycle();
					bitmap = null;
				}
			}
		});
		
		mUserImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg) { 
				if(doneRecording) {  
					Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(Intent.createChooser(i,
							RecordAudio.this.getString(R.string.select_picture)), SELECT_PICTURE);
				}
			} 
		});
				
	}
	 
	 
	/**  */
	private void goBackHome() {
		// TODO
		if(includePhoto) {
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Length of recording") 
	         .setAction("Audio recording not sent, button clicked to return home")
	         .setLabel("Photo included") 
	         .setValue(Long.parseLong(mUserLogs.getDuration())) 
	         .build());
		} else { 
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Length of recording") 
	         .setAction("Audio recording not sent, button clicked to return home")
	         .setLabel("Photo not included") 	  
	         .setValue(Long.parseLong(mUserLogs.getDuration())) 						 
	         .build()); 
		}

		if(includePhoto) {
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Number of playbacks") 
	         .setAction("Audio recording not sent, button clicked to return home")
	         .setLabel("Photo included") 
	         .setValue(mCountPlaybacks)
	         .build());
		} else { 
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Number of playbacks") 
	         .setAction("Audio recording not sent, button clicked to return home")
	         .setLabel("Photo not included") 	  
	         .setValue(mCountPlaybacks) 						 
	         .build()); 
		}
		
		
		
		if(bitmap != null) {
			Log.e(TAG, "Recycling bitmap.");
			bitmap.recycle();
			bitmap = null;
		}
		File file = new File(mMainDir + mInnerDir + mUniqueAudioRecording);
		if(file.exists()) {
			Log.e(TAG, "mBack.onClick - Deleting file: " + mMainDir + mInnerDir + mUniqueAudioRecording);
			file.delete();
		}
		Intent intent = new Intent(RecordAudio.this, MainActivity.class);
		startActivity(intent);
		finish();
	}


	/**  */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		if (resultCode == RESULT_OK) { 
			if (requestCode == SELECT_PICTURE) { 
				Uri selectedImageUri = data.getData();
				String selectedImagePath = getPath(selectedImageUri);

				bitmap = BitmapFactory.decodeFile(selectedImagePath);
				if(bitmap != null) {
					while(bitmap.getHeight() > 2000 || bitmap.getWidth() > 2000) {  
						bitmap = halfSize(bitmap);
					}
					
					Log.e(TAG, "Bitmap height: " + bitmap.getHeight() + " width: " + bitmap.getWidth());
					mUserImage.setImageBitmap(bitmap);  
					mUserLogs.setPhotoPath(selectedImagePath); 
				}
			}
		} else { 
			goBackHome(); 
		}
	}
	
	/**   */ 
	private Bitmap halfSize(Bitmap input) { 
		int height = input.getHeight();
		int width = input.getWidth();  
		return Bitmap.createScaledBitmap(input,  width/2, height/2, false);
	}

	/**  */
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/** Sets up file structure for audio recordings. **/
	private void setupDirectory() {
		// This folder should have been created in MainActivity
		// This is just in case it wasn't.
		mMainDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		mMainDir += "/Android/data/com.MSRi.ivr.cgnetswara";
		File dir = new File(mMainDir);
		if(!dir.exists() || !dir.isDirectory()) {
			dir.mkdir();
		} 		

		// This folder will be queried when there's Internet - files that 
		// need to be sent should be stored in here 
		File dirInner = new File(mMainDir + mInnerDir);
		if(!dirInner.exists() || !dirInner.isDirectory()) {
			dirInner.mkdir();
		} 

		Calendar c = Calendar.getInstance();
		
		// Name of audio file is the data and then time the audio was created. 
		String date = c.get(Calendar.YEAR) + "_"+ c.get(Calendar.MONTH)
				+ "_" + c.get(Calendar.DAY_OF_MONTH);
		String time = c.get(Calendar.HOUR_OF_DAY) + "_" 
				+ c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND);


		// Different format for the name and date - used for the body of the email.
		String dateAudio = c.get(Calendar.YEAR) + "-"+ c.get(Calendar.MONTH)
				+ "-" + c.get(Calendar.DAY_OF_MONTH);

		String timeAudio = c.get(Calendar.HOUR_OF_DAY) + ":" 
				+ c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

		
		mUniqueAudioRecording = "/" + date + "__" + time;
		
		mUserLogs = new SaveAudioInfo(mMainDir, mUniqueAudioRecording, mPhoneNumber); 
		mUserLogs.setAudioDateTime(dateAudio + " " + timeAudio);

		mUniqueAudioRecording += ".mp3";  
	}

	/** Releases resources back to the system.  */
	private void stopPlayingAudio(MediaPlayer mp) {
		if(mp != null) {
			mp.stop();
			mp.reset();   
			mp = null;	
		}
	}

	/** Releases resources back to the system.  */
	private void stopRecording() { 
		doneRecording = true;
		if(mRecMicToMp3 != null) {
			mRecMicToMp3.stop();
		}
		if(includePhoto) { 
			TextView textLimit = (TextView) findViewById(R.id.limit);
			textLimit.setText(this.getString(R.string.tap_image_to_choose_another));
		} 
		
		
		MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
		
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(mMainDir + mInnerDir + mUniqueAudioRecording);
			metaRetriever.setDataSource(inputStream.getFD()); 
			inputStream.close();
		 	
			Long durationms = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			long duration = durationms / 1000;
		      
			mUserLogs.setAudioLength(duration);
		    
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			long duration = 0;
		     
			
			mUserLogs.setAudioLength(duration);
		    
		} 
	}

	/** Called when the activity is paused; releases resources back to the 
	 *  system and stops audio recordings that may be playing. */
	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		
		// If the user pauses the app when they're recording a message 
		// we're going to treat it like they stopped recording before 
		// pausing the app
		if(mStop.getVisibility() == View.VISIBLE) {  
			stopRecording(); 
		}  
		stopPlayingAudio(mUserAudio); 
	}

	/** Called when the activity is paused; begins playing the audio recording
	 *  for the user. */
	@Override
	public void onResume() {
		super.onResume(); 
		locationManager.requestLocationUpdates(provider, 400, 1, this);
		if(mRecMicToMp3 != null) {
			mRecMicToMp3.stop();
		} 
	}

	/** Creates an audio recording using the phone mic as the audio source. */
	private void startRecording() {   
		mRecMicToMp3 = new RecMicToMp3(mMainDir + mInnerDir + mUniqueAudioRecording, 8000); 
		mRecMicToMp3.start();
	}	

	/** Plays the generated audio recording. */
	private void startPlaying() {
		mUserAudio = new MediaPlayer();
		 
		try {
			// Saved in the main folder 
			mUserAudio.setDataSource(mMainDir + mInnerDir + mUniqueAudioRecording);
			mUserAudio.prepare();
			mUserAudio.start();
  			 
			mPlayback.setImageResource(R.drawable.stop_icon);
			
			mUserAudio.setOnCompletionListener(new OnCompletionListener() {
	            public void onCompletion(MediaPlayer mp) { 
	            	mPlayback.setImageResource(R.drawable.play_icon);
	            }
	        }); 
			
		} catch (IOException e) {
			Log.e(TAG, "StartPlaying() : prepare() failed");
		} catch (Exception e) { 
			Log.e(TAG, e.toString());
		}
	}
 

	/** Sends the audio file to a central location. */
	private void sendData() { 
		Log.e(TAG, "Send data is being called");
		
		// TODO - Leave 
		if(includePhoto) {
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Length of recording") 
	         .setAction("Audio recording sent")
	         .setLabel("Photo included") 
	         .setValue(Long.parseLong(mUserLogs.getDuration())) 
	         .build());
		} else { 
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Length of recording") 
	         .setAction("Audio recording sent")
	         .setLabel("Photo not included") 	  
	         .setValue(Long.parseLong(mUserLogs.getDuration())) 						 
	         .build()); 
		}
		   
		if(includePhoto) {
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Number of playbacks") 
	         .setAction("Audio recording sent")
	         .setLabel("Photo included") 
	         .setValue(mCountPlaybacks)
	         .build());
		} else { 
			 t.send(new HitBuilders.EventBuilder()
	         .setCategory("Number of playbacks") 
	         .setAction("Audio recording sent")
	         .setLabel("Photo not included") 	  
	         .setValue(mCountPlaybacks) 						 
	         .build()); 
		}
		
		
		 
		mFileToBeSent = true; 
		mUserLogs.writeToFile();
		 
		Intent intent = new Intent();  
		GoogleAnalytics.getInstance(this.getBaseContext()).dispatchLocalHits();
		intent.setAction("com.android.CUSTOM_INTENT");
		sendBroadcast(intent);  
	} 
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {   
		if(keyCode == KeyEvent.KEYCODE_BACK && mStop.getVisibility() == View.VISIBLE) { 
			mStop.performClick();
		}  
			
		
	    //Handle the back button
	    if(keyCode == KeyEvent.KEYCODE_BACK && mBack.getVisibility() == View.VISIBLE) {
	        //Ask the user if they want to quit
	        new AlertDialog.Builder(this) 
	        .setMessage(this.getString(R.string.discard_message))
	        .setPositiveButton(this.getString(R.string.yes_message), new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) { 
	        		if(includePhoto) {
	       			 t.send(new HitBuilders.EventBuilder()
	       	         .setCategory("Length of recording") 
	       	         .setAction("Audio recording not sent, soft key clicked to return home")
	       	         .setLabel("Photo included") 
	       	         .setValue(Long.parseLong(mUserLogs.getDuration())) 
	       	         .build());
	       		} else { 
	       			 t.send(new HitBuilders.EventBuilder()
	       	         .setCategory("Length of recording") 
	       	         .setAction("Audio recording not sent, soft key clicked to return home")
	       	         .setLabel("Photo not included") 	  
	       	         .setValue(Long.parseLong(mUserLogs.getDuration())) 						 
	       	         .build()); 
	       		}
	            	RecordAudio.this.finish();
	                // TODO - leave 
	            } 
	        })
	        .setNegativeButton(this.getString(R.string.no_message), null)
	        .show(); 
	        return true;
	    }
	    else {
	        return super.onKeyDown(keyCode, event);
	    }

	}

	/** Called when the activity is destroyed, deletes the 
	 *  audio file if it shouldn't be sent. */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(!mFileToBeSent) { 
			File file = new File(mMainDir + mInnerDir + mUniqueAudioRecording);
			if(file.exists()) {
				Log.e(TAG, "onDestroy, deleting file: " + mMainDir + mInnerDir + mUniqueAudioRecording);
				file.delete();
			}
			if(bitmap != null) { 
				Log.e(TAG, "Recycling bitmap.");
				bitmap.recycle();
				bitmap = null;
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		int lat = (int) (location.getLatitude());
	    int lng = (int) (location.getLongitude());
	    Log.e(TAG, "lat: " + lat + " lng: " + lng); 
	}

	@Override
	public void onProviderDisabled(String provider) { 
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) { 
	} 
}