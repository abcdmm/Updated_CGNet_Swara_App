package cgnet.swara.activity;


import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.cgnet.swara.R;

import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import android.widget.Button;
import android.widget.EditText;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
 
@Config(manifest = "../FeedEx-master/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class ActivityTest {
	MainActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
 
        Button recordMessage =  (Button) activity.findViewById(R.id.one);
		Button listenMessages = (Button) activity.findViewById(R.id.two);
		EditText results =   (EditText) activity.findViewById(R.id.phone);
		results.setText("1");
		assertEquals("Go", "Go"); 
    }
      
    
    @Test
    public void testTest() throws Exception {
    	assertEquals("Go", "o"); 
    }
}
