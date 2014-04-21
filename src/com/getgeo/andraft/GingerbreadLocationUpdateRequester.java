package com.getgeo.andraft;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;


public class GingerbreadLocationUpdateRequester extends FroyoLocationUpdateRequester{
	

		  public GingerbreadLocationUpdateRequester(LocationManager locationManager) {
		    super(locationManager);
		  }

	
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		  public void requestLocationUpdates(long minTime, long minDistance, Criteria criteria, PendingIntent pendingIntent) {
		    locationManager.requestLocationUpdates(minTime, minDistance, criteria, pendingIntent);
		  }
		}
