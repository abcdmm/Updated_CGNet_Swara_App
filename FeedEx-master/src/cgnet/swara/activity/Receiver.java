package cgnet.swara.activity;

import java.io.File;  
import android.util.Log;
import android.content.Intent;
import android.os.Environment;
import android.content.Context; 
import android.net.ConnectivityManager; 
import android.content.BroadcastReceiver;

/** A BroadcastReciever responds to broadcast messages from the system, 
 *  this class specifically responds to changes in network connectivity.  
 *  @author Krittika D'Silva
 * */
public class Receiver extends BroadcastReceiver {
	private static final String TAG = "Receiver";
	  
	/** CGNet Swara's main directory with audio files. */
	private String mMainDir;
	
	/** Folder containing all audio files that have yet to be sent. */
	private final String mInnerDir = "/Logs";
	  
	/** Called when there's a change in connectivity. Iterates through files 
	 * that need to be sent and sends each one if there's Internet. */ 
    @Override
    public void onReceive(Context context, Intent intent) { 
    	mMainDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		mMainDir += "/Android/data/com.MSRi.ivr.cgnetswara"; 
		
    	ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (cm == null)
            return; 
        if (cm.getActiveNetworkInfo() != null || intent.getAction().equals("com.android.CUSTOM_INTENT")) {
        	File dir = new File(mMainDir + mInnerDir); // Contains files to be sent
        	File[] directoryListing = dir.listFiles();
         
        	if (directoryListing != null) {
        		for (File child : directoryListing) { 
        	    	String fileName = child.getName();
 
        			SendEmailAsyncTask task = new SendEmailAsyncTask(context, 
        										  mMainDir, mInnerDir, "/" + fileName);
        			task.execute(); 
        	    }
        	}       	
        }   
    }  
}