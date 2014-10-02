package cgnet.swara.activity;

import java.io.File;

import org.cgnet.swara.MainApplication;
import org.cgnet.swara.MainApplication.TrackerName;
import org.cgnet.swara.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
 
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
  	 
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRecordMessage = (Button) findViewById(R.id.one);
		mListenMessages = (Button) findViewById(R.id.two);
		mIncludeAudio = (Button) findViewById(R.id.photo);
		mNumber = (EditText) findViewById(R.id.phone);
		mNumber.clearFocus();
 
 		// Get Google Analytics tracker.
		Tracker t = ((MainApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
		   
        t.setScreenName("Home Screen");
        
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
  
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
 
		// Creates a folder for the app's recordings
		String path_audio = Environment.getExternalStorageDirectory().getAbsolutePath();
		path_audio += "/CGNet_Swara";
		File dir_audio = new File(path_audio); 
		if (!dir_audio.exists()|| !dir_audio.isDirectory()) {
			dir_audio.mkdirs();
		}
		
		if(mNumber != null && mNumber.getText().toString() != null &&  mNumber.getText().toString().length() == 10) {
			mRecordMessage.setEnabled(true);
			mIncludeAudio.setEnabled(true);
		} else { 
			mRecordMessage.setEnabled(false);
			mIncludeAudio.setEnabled(false); 
		}	

 
		// Saves the users phone number
		mNumber.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				mPhoneNumber = s.toString(); 
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putString("Phone", mPhoneNumber); 
				editor.apply();
				Log.e(TAG, ""+mPhoneNumber.length());
				if(mPhoneNumber.length() != 10) {
					mRecordMessage.setEnabled(false);
					mIncludeAudio.setEnabled(false);
				} else {
					mRecordMessage.setEnabled(true);
					mIncludeAudio.setEnabled(true); 
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 
		
		Intent intent = new Intent();   
		intent.setAction("com.android.CUSTOM_INTENT");
		sendBroadcast(intent);  
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

				mNumber.setText(mPhoneNumber);
				if(mPhoneNumber == null || mPhoneNumber.equals("") || mPhoneNumber.equals(" ")) { 
					showPrompt();
				} 
			}
		})
		.setNegativeButton(this.getString(R.string.cancel_phone),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				mPhoneNumber = userInput.getText().toString();
				mNumber.setText(mPhoneNumber);
				dialog.cancel();
				onResume();
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

		if(mNumber != null && mNumber.getText().toString() != null &&  mNumber.getText().toString().length() == 10) {
			mRecordMessage.setEnabled(true);
			mIncludeAudio.setEnabled(true);
		} else { 
			mRecordMessage.setEnabled(false);
			mIncludeAudio.setEnabled(false); 
		}	

		mNumber.clearFocus();
		mNumber.setSelected(false);  
	}

	/** Called when the activity is paused; releases resources back to the 
	 *  system and stops audio recordings that may be playing. */
	@Override
	protected void onPause() { 
		super.onPause(); 
		 
	}

	/** Opens a new activity to allow the user to record audio content. */
	private void recordInput(final boolean includePhoto) {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
		String restoredText = prefs.getString("Phone", null);
		

		InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(mNumber.getWindowToken(), 0);

		if (restoredText != null) { 
			Intent intent = new Intent(MainActivity.this, RecordAudio.class);
			intent.putExtra("photo", includePhoto); 
			intent.putExtra("phone", restoredText); 
			startActivity(intent);
		} else { 
			// this shouldn't happen
			showPrompt(); 
		}
	}


	/** Opens a new activity to allow the user to view and listen to 
	 *  recordings. */
	private void loadRecordings() { 
		Intent intent = new Intent(this, org.cgnet.swara.activity.HomeActivity.class);
		startActivity(intent); 
	} 
}