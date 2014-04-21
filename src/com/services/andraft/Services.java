package com.services.andraft;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.basegeo.andraft.DBHelper;
import com.basegeo.andraft.ILastLocationFinder;
import com.basegeo.andraft.LocationUpdateRequester;
import com.bus.andraft.R;
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
	private float[] distance;
	private boolean bool_inside;
	private int k = 0;
	private SoundPool sp;
	private int soundId;
	DBHelper dbHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		Intent activeIntent = new Intent(this, LocationChangedReceiver.class);
		locationListenerPendingIntent = PendingIntent.getBroadcast(this, 0,
				activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		sp = new SoundPool(1, AudioManager.STREAM_RING, 0);
		soundId = sp.load(this, R.raw.serg, 1);
		Log.d("myLogs", "onCreate service");
		ar = new ArrayList<LatLng>();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		lastLocationFinder = PlatformSpecificImplementationFactory
				.getLastLocationFinder(this);
		locationUpdateRequester = PlatformSpecificImplementationFactory
				.getLocationUpdateRequester(locationManager);
		locationManager.addGpsStatusListener(new GpsStatus.Listener() {

			@Override
			public void onGpsStatusChanged(int event) {
				switch (event) {
				case GpsStatus.GPS_EVENT_STARTED:
					Log.d("myLogs", "GPS_EVENT_STARTED");
					requestLocationUpdates();
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					Log.d("myLogs", "GPS_EVENT_STOPPED");
					requestLocationUpdates();
					break;
				}

			}
		});

		getLocation();
		requestLocationUpdates();
		dbHelper = new DBHelper(this);
		insertGeoHm();

	}

	private void insertGeoHm() {
		if (GeoConstants.hm.size() == 0) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor c = db.query("bus", null, null, null, null, null, null);

			// ставим позицию курсора на первую строку выборки
			// если в выборке нет строк, вернется false
			if (c.moveToFirst()) {

				// определяем номера столбцов по имени в выборке
				int idColIndex = c.getColumnIndex("id");
				int nameColIndex = c.getColumnIndex("name");
				int latColIndex = c.getColumnIndex("lat");
				int lngColIndex = c.getColumnIndex("lng");

				do {
					// получаем значения по номерам столбцов и пишем все в лог
					Log.d("myLogs", "ID = " + c.getInt(idColIndex)
							+ ", name = " + c.getString(nameColIndex)
							+ ", lat = " + c.getString(latColIndex)
							+ ", lng = " + c.getString(lngColIndex));
					GeoConstants.hm.put(c.getString(nameColIndex), new LatLng(
							Double.parseDouble(c.getString(latColIndex)),
							Double.parseDouble(c.getString(lngColIndex))));
					// переход на следующую строку
					// а если следующей нет (текущая - последняя), то false -
					// выходим из цикла
				} while (c.moveToNext());
			} else
				Log.d("myLogs", "0 rows");
			c.close();

		}
	}

	private void getLocation() {
		Location location = lastLocationFinder.getLastBestLocation(
				GeoConstants.MAX_DISTANCE, GeoConstants.MAX_TIME);
		updateWithNewLocation(location);
	}

	private void updateWithNewLocation(Location location) {
		try {
			Log.d("myLogs", "Широта:" + location.getLatitude() + " Долгота:"
					+ location.getLongitude());
		} catch (NullPointerException e) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Log.d("myLogs", "Ждем 2 минуты");
						Thread.sleep(120000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					getLocation();

				}

			});
			t.start();
		}
	}

	protected void requestLocationUpdates() {
		Log.d("myLogs", "requestLocationUpdates");

		String bestAvailableProvider = locationManager.getBestProvider(
				criteria, true);
		Log.d("myLogs", " bestAvailableProvider=" + bestAvailableProvider);
		if (bestAvailableProvider != null
				&& bestAvailableProvider.equals(LocationManager.NETWORK_PROVIDER)) {
			Log.d("myLogs", "locationManager.requestLocationUpdates");
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					networkLocationProviderListener, getMainLooper());
		}
		if (bestAvailableProvider != null
				&& bestAvailableProvider.equals(LocationManager.GPS_PROVIDER)) {
			Log.d("myLogs", "locationManager.requestLocationUpdates");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					gpsLocationProviderListener, getMainLooper());

		}
	}

	private boolean getInside() {
		k++;
		if (k <= 1)
			return true;
		return false;
	}

	protected LocationListener gpsLocationProviderListener = new LocationListener() {

		public void onLocationChanged(Location l) {
			locationManager.removeUpdates(networkLocationProviderListener);
			initLocationChanged(l);
		}

		public void onProviderDisabled(String provider) {
			Log.d("myLogs", "bestInactive provider disabled:" + provider);
			requestLocationUpdates();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("myLogs", "provider:" + provider + ",status:" + status);
		}

		public void onProviderEnabled(String provider) {
			Log.d("myLogs", "bestProvider enabled:" + provider);
		}
	};

	protected LocationListener networkLocationProviderListener = new LocationListener() {

		public void onLocationChanged(Location l) {
			initLocationChanged(l);
		}

		public void onProviderDisabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {

		}
	};

	private void initLocationChanged(Location l) {
		Log.d("myLogs", "bestInactive Долгота:" + l.getLongitude() + " Широта:"
				+ l.getLatitude());
		if (ar.size() != GeoConstants.hm.size()) {
			Log.d("myLogs", "ar.size(" + ar.size() + "),hm.size("
					+ GeoConstants.hm.size() + ")");
			ar.clear();
			for (LatLng latlng : GeoConstants.hm.values()) {
				Log.d("myLogs", "LatLng:" + latlng.latitude + ", "
						+ latlng.longitude);
				ar.add(latlng);
				Log.d("myLogs", "ar:" + ar);
			}
		}
		if (ar.size() != 0) {
			for (LatLng lt : ar) {
				distance = new float[2];
				Location.distanceBetween(lt.latitude, lt.longitude,
						l.getLatitude(), l.getLongitude(), distance);

				if (distance[0] > 300) {
					Log.d("myLogs", "OutSide");
					k = 0;
					bool_inside = false;

				} else {
					Log.d("myLogs", "Inside");
					ar.remove(lt);
					ar.add(0, lt);
					bool_inside = getInside();
					break;
				}

			}
		}

		if (bool_inside) {
			sp.play(soundId, 1, 1, 1, 0, 1);
		}

	}

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