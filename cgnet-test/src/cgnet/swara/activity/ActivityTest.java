package cgnet.swara.activity;


import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class ActivityTest {
	MainActivity activity;

    @Before
    public void setup() {
        this.activity = Robolectric.buildActivity(MainActivity.class).create().get();
    }
}
