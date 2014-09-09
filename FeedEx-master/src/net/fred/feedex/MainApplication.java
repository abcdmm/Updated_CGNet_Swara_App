/**
 * FeedEx
 *
 * Copyright (c) 2012-2013 Frederic Julian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.fred.feedex;

import java.util.HashMap; 
import net.fred.feedex.R;
import net.fred.feedex.utils.PrefUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker; 

import android.app.Application;
import android.content.Context;

import android.util.Log;

public class MainApplication extends Application {
	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	
	public MainApplication() {
		super(); 
	}
	 
	public enum TrackerName {
		APP_TRACKER, 		// Tracker used only in this app.
		GLOBAL_TRACKER, 	// Tracker used by all the apps from a company. eg: roll-up tracking.
		ECOMMERCE_TRACKER, 	// Tracker used by all ecommerce transactions from a company.
	}
	
	public synchronized Tracker getTracker(TrackerName appTracker) {
		if (!mTrackers.containsKey(appTracker)) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			
			if (appTracker == TrackerName.APP_TRACKER) {
				Tracker t = analytics.newTracker(R.xml.analytics);
			    mTrackers.put(appTracker, t); 
            } 
		}  
		return mTrackers.get(appTracker);
	}
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        PrefUtils.putBoolean(PrefUtils.IS_REFRESHING, false); // init
    }

    public static Context getContext() {
        return context;
    }
}
