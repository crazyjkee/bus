package com.services.andraft;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.basegeo.andraft.ILastLocationFinder;
import com.basegeo.andraft.LocationUpdateRequester;
import com.getgeo.andraft.PlatformSpecificImplementationFactory;
import com.google.android.gms.maps.model.LatLng;
import com.receiver.andraft.LocationChangedReceiver;
import com.statica.andraft.GeoConstants;

public class Services extends Service {
	LocationManager locationManager;
	protected ILastLocationFinder lastLocationFinder;
	private PendingIntent locationListenerPendingIntent;
	private LocationUpdateRequester locationUpdateRequester;
	private Criteria criteria;
	private ArrayList<LatLng> ar;

	@Override
	public void onCreate() {
		super.onCreate();
		Intent activeIntent = new Intent(this, LocationChangedReceiver.class);
		locationListenerPendingIntent = PendingIntent.getBroadcast(this, 0,
				activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		Log.d("myLogs", "onCreate service");

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		lastLocationFinder = PlatformSpecificImplementationFactory
				.getLastLocationFinder(this);
		locationUpdateRequester = PlatformSpecificImplementationFactory
				.getLocationUpdateRequester(locationManager);
		getLocation();
		requestLocationUpdates();
		
	}

	private void getLocation() {
		Location location = lastLocationFinder.getLastBestLocation(
				GeoConstants.MAX_DISTANCE, GeoConstants.MAX_TIME);
		updateWithNewLocation(location);
		requestLocationUpdates();
		}

	

	private void updateWithNewLocation(Location location) {
			try{
				Log.d("myLogs", "Широта:"
					+ location.getLatitude()+" Долгота:" + location.getLongitude());
			}catch(NullPointerException e){
				Thread t = new Thread(new Runnable(){

					@Override
					public void run() {
						try {
							Log.d("myLogs","Ждем 2 минуты");
							Thread.sleep(120000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						getLocation();
						
					}
					
				}
				);t.start();
	}}

	protected void requestLocationUpdates() {
		Log.d("myLogs", "requestLocationUpdates");
		// Normal updates while activity is visible.
		locationUpdateRequester.requestLocationUpdates(GeoConstants.MAX_TIME,
				GeoConstants.MAX_DISTANCE, criteria,
				locationListenerPendingIntent);

		// сервис

		// Register a receiver that listens for when the provider I'm using has
		// been disabled.
		IntentFilter intentFilter = new IntentFilter(
				GeoConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
		registerReceiver(locProviderDisabledReceiver, intentFilter);
		// Регистрируем ловлю passive location


		
		String bestAvailableProvider = locationManager.getBestProvider(
				criteria, true);
		Log.d("myLogs", " bestAvailableProvider=" + bestAvailableProvider);
		if (bestAvailableProvider != null && bestAvailableProvider.equals("network")) {
			Log.d("myLogs", "locationManager.requestLocationUpdates");
			locationManager.requestLocationUpdates(bestAvailableProvider, 0, 0,
					bestInactiveLocationProviderListener, getMainLooper());
		}
	}

	protected LocationListener bestInactiveLocationProviderListener = new LocationListener() {
		public void onLocationChanged(Location l) {
			Log.d("myLogs", "bestInactive Долгота:" + l.getLongitude()
					+ " Широта:" + l.getLatitude());
			for(LatLng latlng:GeoConstants.hm.values()){
				Log.d("myLogs","LatLng:"+latlng.latitude+", "+latlng.longitude);
			}
		}

		public void onProviderDisabled(String provider) {
			Log.d("myLogs", "bestInactive provider disabled:" + provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
			Log.d("myLogs", "bestProvider enabled:" + provider);
			//requestLocationUpdates();
		}
	};
	protected BroadcastReceiver locProviderDisabledReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean providerDisabled = !intent.getBooleanExtra(
					LocationManager.KEY_PROVIDER_ENABLED, false);
			Log.d("myLogs", "providerDisabled:" + providerDisabled);
			if (providerDisabled)
				requestLocationUpdates();
		}
	};



	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("myLogs", "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}