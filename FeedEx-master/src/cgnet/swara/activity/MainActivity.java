package cgnet.swara.activity;

import java.io.File;   
import android.util.Log; 
import android.view.View; 
import net.fred.feedex.MainApplication;
import net.fred.feedex.MainApplication.TrackerName;
import net.fred.feedex.R; 
import android.os.Bundle;
import android.app.Activity; 
import android.text.Editable;
import android.widget.Button;
import android.content.Intent;
import android.os.Environment;
import com.flurry.android.FlurryAgent;

import android.app.AlertDialog;
import android.widget.EditText;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.view.LayoutInflater; 
import android.content.DialogInterface;
import android.content.SharedPreferences;  
import android.view.View.OnClickListener;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.HitBuilders;
import com.localytics.android.*;


/** This is the first screen of the CGNet Swara App. 
 *  It allows the user to either record a message (which is then sent to a central location) 
 *  or listen to recordings.
 *  @author Krittika D'Silva (krittika.dsilva@gmail.com) */ 
public class MainActivity extends Activity {
	protected static final String TAG = "MainActivity";

	/** Opens an activity that allows a user to record and send a message. */
	private Button mRecordMessage;

	/** Opens an activity that allows a user to listen to recordings. */
	private Button mListenMessages;

	/** Opens an activity that allows the user to attach a photo, record a message, 
	 *  and send both. */
	private Button mIncludeAudio;

	/** The users' phone number. */	
	private String mPhoneNumber;

	/** The users' phone number. */
	private EditText mNumber;
  
	private LocalyticsAmpSession localyticsSession;

	
	@Override
	protected void onStart() {
		super.onStart();
		Log.e(TAG, "IN ON START!");
		FlurryAgent.onStartSession(this, EmailLogin.api_key);
	    FlurryAgent.setLogEnabled(true);
	    FlurryAgent.setLogEvents(true);
	    FlurryAgent.setLogLevel(Log.VERBOSE);
	    
	}
	 
	@Override
	protected void onStop() {
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRecordMessage = (Button) findViewById(R.id.one);
		mListenMessages = (Button) findViewById(R.id.two);
		mIncludeAudio = (Button) findViewById(R.id.photo);
		mNumber = (EditText) findViewById(R.id.phone);
 
		FlurryAgent.logEvent("User on the main screen");
		
		// Get tracker.
		Tracker t = ((MainApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
		   
        t.setScreenName("Home Screen");
        
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
        t.send(new HitBuilders.EventBuilder()
        .setCategory("Barren Fields")
        .setAction("Rescue")
        .setLabel("Dragon")
        .setValue(1)
        .build());
        
        // Activity Creation Code
        
        // Instantiate the object
        this.localyticsSession = new LocalyticsAmpSession(
                 this.getApplicationContext());  // Context used to access device resources
      
        this.localyticsSession.open();           // open the session
        this.localyticsSession.upload();         // upload any data
     
		String savedText = getPreferences(MODE_PRIVATE).getString("Phone", null); 
		if(savedText != null && !savedText.equals("")) {
			mNumber.setText(savedText);
		} else {
			showPrompt();
		}
		  
		mRecordMessage.setEnabled(true);
		mIncludeAudio.setEnabled(true);


		mRecordMessage.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) {
				mRecordMessage.setEnabled(false);
				recordInput(false);
 
			}  
		}); 

		mIncludeAudio.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg) { 
				mIncludeAudio.setEnabled(false);
				recordInput(true);
 
			}  
		}); 

		mListenMessages.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg) { 
				loadRecordings();
 
			}  
		}); 

		// Creates a folder for the app's recordings
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		path += "/Android/data/com.MSRi.ivr.cgnetswara";
		File dir = new File(path); 
		if (!dir.exists()|| !dir.isDirectory()) {
			dir.mkdirs();
		}



		// Saves the users phone number
		mNumber.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				mPhoneNumber = s.toString(); 
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putString("Phone", mPhoneNumber); 
				editor.apply();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		});
		
		mNumber.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	if(s.toString().equals("") || s.toString().equals(" ")) { 
	        		showPrompt();
	        	}
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
	}

	 

	/** Displays an alert dialog prompting 
	 *  the user to input their phone number. */
	private void showPrompt() {
 		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.phone_prompt, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton(this.getString(R.string.ok_phone),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) { 
				mPhoneNumber = userInput.getText().toString();
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putString("Phone", mPhoneNumber);
				editor.apply();
				Log.e(TAG, "Phone number: " + mPhoneNumber);
				mNumber.setText(mPhoneNumber);
			}
		})
		.setNegativeButton(this.getString(R.string.cancel_phone),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				mPhoneNumber = userInput.getText().toString();
				Log.e(TAG, "phone number: " + mPhoneNumber + "!");
				if(mPhoneNumber == null || mPhoneNumber.equals("") || mPhoneNumber.equals(" ")) {
					Log.e(TAG, "phone number inside");
					showPrompt();
				} else {
					dialog.cancel();
					onResume();
				}
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create(); 
		alertDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		// show it
		alertDialog.show();
	} 

	/** Called when the activity is paused; begins playing the audio recording
	 *  for the user. */
	@Override
	public void onResume() {
		super.onResume();   


		   this.localyticsSession.open();
		   this.localyticsSession.upload(); 
		mRecordMessage.setEnabled(true);
		mIncludeAudio.setEnabled(true);
		mNumber.clearFocus();
		mNumber.setSelected(false);  
	}

	/** Called when the activity is paused; releases resources back to the 
	 *  system and stops audio recordings that may be playing. */
	@Override
	protected void onPause() {
		this.localyticsSession.detach();
		   this.localyticsSession.close();
		   this.localyticsSession.upload();
		super.onPause(); 
		 
	}

	/** Opens a new activity to allow the user to record audio content. */
	private void recordInput(final boolean includePhoto) {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
		String restoredText = prefs.getString("Phone", null);

		if (restoredText != null) { 
			Intent intent = new Intent(MainActivity.this, RecordAudio.class);
			intent.putExtra("photo", includePhoto); 
			intent.putExtra("phone", restoredText); 
			startActivity(intent);
			finish();
		} else { 
			// this shouldn't happen
			showPrompt(); 
		}
	}


	/** Opens a new activity to allow the user to view and listen to 
	 *  recordings. */
	private void loadRecordings() { 
		Intent intent = new Intent(this, net.fred.feedex.activity.HomeActivity.class);
		startActivity(intent); 
	} 
}