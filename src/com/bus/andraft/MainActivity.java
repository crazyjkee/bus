package com.bus.andraft;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.basegeo.andraft.ILastLocationFinder;
import com.basegeo.andraft.SharedPreferenceSaver;
import com.bus.andraft.CustomDialog.OnDissDialog;
import com.getgeo.andraft.PlatformSpecificImplementationFactory;
import com.google.android.gms.location.LocationRequestCreator;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.services.andraft.Services;
import com.statica.andraft.GeoConstants;
import com.statica.andraft.Utils;

public class MainActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>,OnDissDialog {
	SupportMapFragment mapFragment;
	GoogleMap map;
	SharedPreferenceSaver sharedPreferenceSaver;

	final String TAG = "myLogs";
	private ILastLocationFinder lastLocationFinder;
	LocationRequestCreator loc;
	CustomDialog dialog;
	private SharedPreferences sf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dialog = new CustomDialog(this);
		GeoConstants.hm = new HashMap<String, LatLng>();
		loc = new LocationRequestCreator();
		lastLocationFinder = PlatformSpecificImplementationFactory
				.getLastLocationFinder(this);
		sharedPreferenceSaver = PlatformSpecificImplementationFactory.getSharedPreferenceSaver(this);
		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		map = mapFragment.getMap();
		dialog.setOnDissDialog(this);

		if (map == null) {
			finish();
			return;
		}

		init();
		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			doSearch(intent.getStringExtra(SearchManager.QUERY));
		} else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
		}
	}

	private void init() {
		Location location = lastLocationFinder.getLastBestLocation(
				GeoConstants.MAX_DISTANCE, GeoConstants.MAX_TIME);
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
				15);
		map.animateCamera(cameraUpdate);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		Log.d("myLogs", location.getLatitude() + ", " + location.getLongitude());

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void onSearchClick(View view) {
		onSearchRequested();
	}

	public void onMenuClick(View view) {
		Log.d("myLogs","onMenuClick hm:"+GeoConstants.hm.toString());
		dialog.setHm(GeoConstants.hm);
		dialog.create().show();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void doSearch(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		getSupportLoaderManager().restartLoader(0, data, this);
	}

	private void getPlace(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		getSupportLoaderManager().restartLoader(1, data, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
		CursorLoader cLoader = null;
		if (arg0 == 0)
			cLoader = new CursorLoader(getBaseContext(),
					PlaceProvider.SEARCH_URI, null, null,
					new String[] { query.getString("query") }, null);
		else if (arg0 == 1)
			cLoader = new CursorLoader(getBaseContext(),
					PlaceProvider.DETAILS_URI, null, null,
					new String[] { query.getString("query") }, null);
		return cLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		showLocations(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
	}

	private void showLocations(Cursor c) {
		MarkerOptions markerOptions = null;
		LatLng position = null;
		map.clear();

		while (c.moveToNext()) {
			Log.d("myLogs", "0:" + c.getString(0));
			markerOptions = new MarkerOptions();
			position = new LatLng(Double.parseDouble(c.getString(1)),
					Double.parseDouble(c.getString(2)));
			markerOptions.position(position);
			markerOptions.title(c.getString(0));
			map.addMarker(markerOptions);
			GeoConstants.hm.put(c.getString(0), position);
		}
		if (position != null) {
			CameraUpdate cameraPosition = CameraUpdateFactory
					.newLatLng(position);
			map.animateCamera(cameraPosition);
			
			sf = getSharedPreferences(Utils.SHARED_PREFERENCE_FILE,MODE_PRIVATE);
			Log.d("myLogs","sf.getBoolean:"+sf.getBoolean(Utils.BOOT, false));
			Editor ed = sf.edit();
			if(sf.getBoolean(Utils.BOOT, false)==false){
				Log.d("myLogs","sf.getBoolean:"+sf.getBoolean(Utils.BOOT, false));
				this.startService(new Intent(this, Services.class));}
	        ed.putBoolean(Utils.BOOT, true);
	        sharedPreferenceSaver.savePreferences(ed, false);
		}
	}

	@Override
	public void _onDiss() {
		Log.d("myLogs","onDiss");
		if(!(dialog.getHm().size()!=GeoConstants.hm.size())){
			GeoConstants.hm = dialog.getHm();
			Log.d("myLogs","_onDiss() "+GeoConstants.hm);
			}
		
	}

}