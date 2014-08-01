package cgnet.swara.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/** This class allows to perform background operations and
 *  publish results on the UI thread without having to manipulate threads and/or handlers.
 *  @author Krittika D'Silva
 */
class SendEmailAsyncTask extends AsyncTask <Void, Void, Boolean> {
	private static final String TAG = "SendEmailAsyncTask"; 
	
	/** Email sent with the audio recording. */
	private Mail mMail;
	
	/** CGNet Swara's main directory with audio files. */
	private String mMainDir;
	
	/** Folder containing all audio files that have yet to be sent. */
	private String mInnerDir; 
	
	/** Name of the audio file created.	*/
	private String mUniqueAudioRecording;
	
	/** */
	private final String mFromAdddress = EmailLogin.email; 
	
	/** */
	private final String mFromPassword = EmailLogin.password;
	
	/** */
	private final String mToAddress = "cgnetswaratest@gmail.com";
	  
	/** */
	private boolean mEmailSent = false;
	
	private String mTextFile;
	
	private String mAudioFile;
	/** 
	 * 
	 * */
    public SendEmailAsyncTask(Context context, String outerDir, String innerDir, String fileName) {
    	Log.e(TAG, "5. Trying to send: " + fileName);
    	mTextFile = outerDir + innerDir + fileName;
    	
    	FileInputStream fstream;
    	String firstLine = "";
		try {
			 fstream = new FileInputStream(mTextFile);
	    	 Scanner br = new Scanner(new InputStreamReader(fstream)); 
	    	 while (br.hasNext()) {
	    		 firstLine = br.nextLine(); 
	    	 }
	    	 
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		}

		String[] parts = firstLine.split(",");
		 
		mAudioFile = parts[0] + ".mp3"; 
		String photo = parts[1];
		String phoneNumber = parts[2]; 
		String time = parts[3];
		String length = parts[4];
		
    	mMail = new Mail(mFromAdddress, mFromPassword);  
    	mMainDir = outerDir;
    	mInnerDir = innerDir;
        mUniqueAudioRecording = fileName;
    			 
    	String[] toArr = {mToAddress}; // multiple email addresses can be added here 
        mMail.setTo(toArr);
        mMail.setFrom(mFromAdddress);
        mMail.setSubject(getSubject(phoneNumber, time, length));
         
        String body = getBody(phoneNumber, time, length); 
        		
        mMail.setBody(body); 
        
        Log.e(TAG, "6. Location of file: " + mMainDir + mInnerDir + mUniqueAudioRecording);
        try { 
			if(mAudioFile != null) {
				mMail.addAttachment(mAudioFile);
			}
			if(photo != null) { 
				mMail.addAttachment(photo);
			}
		} catch (Exception e) { 
			Log.e(TAG, "Problem including an attachment " +  e.toString());
		}
    }
     
	/** 
     * 
     * */
    @Override
    protected Boolean doInBackground(Void... params) { 
        try { 
        	Log.e(TAG, "about to send the file");
        	if (mMail.send()) {
        		mEmailSent = true;
        		
        		File audio = new File(mAudioFile);
        		audio.delete();
        		File file = new File(mTextFile);
        		file.delete(); 
        	} else { 
        		mEmailSent = false;
        		Log.e(TAG, "Email not sent"); 
        	}
            return true;
        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "Bad account details: " + e);
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            Log.e(TAG, " " + e);
            e.printStackTrace();
            return false;
        } catch (Exception e) {
        	Log.e(TAG, "" + e);
            e.printStackTrace();
            return false;
        }
    } 
       
    public boolean emailSent() { 
    	return mEmailSent;
    }
    
    private String getSubject(String phoneNumber, String time, String length) {
    	String subject = "Swara-Main||" + length + "|DRAFT|" + phoneNumber + "||" + time;
    	 
		return subject;
	}
    
    private String getBody(String phone_number, String time, String length) { 
    	String body;
    	body =  "******************************************************************************" + 
    			"SERVER/सर्वर                        : Swara-Main" +
    			"******************************************************************************" +
    			"POST ID/पोस्ट क्र                       : " +
    			"******************************************************************************" +
    			"CALLER/नंबर                         : " + phone_number +
    			"******************************************************************************" +
    			"TIME STAMP/समय                  : " + time + 
    			"******************************************************************************" +
    			"NAME OF CALLER/फ़ोन करने वाले का नाम     :" +
    			"******************************************************************************" +
    			"CALL LOCATION/कॉल कहाँ से आई        :" +
    			"******************************************************************************" +
    			"TEL CIRC/ टेलिकॉम सर्किल                : "+
    			"******************************************************************************" +
    			"LNGTH/अवधी                              : " + length +
    			"******************************************************************************" +
    			"STATUS/स्थिति                                           : DRAFT" +
    			"******************************************************************************" +
    			"TEXT SUMMARY/   सन्देश                  :";
    	
    	return body;
    }
    
}