package com.bus.andraft;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.basegeo.andraft.ILastLocationFinder;
import com.basegeo.andraft.SharedPreferenceSaver;
import com.getgeo.andraft.PlatformSpecificImplementationFactory;
import com.google.android.gms.location.LocationRequestCreator;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.statica.andraft.GeoConstants;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor>{
	SupportMapFragment mapFragment;
	GoogleMap map;
	TextView tv;
	String image_str;
	EditText secret;
	SharedPreferenceSaver sharedPreferenceSaver;

	final String TAG = "myLogs";
	private ILastLocationFinder lastLocationFinder;
	LocationRequestCreator loc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loc = new LocationRequestCreator();
		lastLocationFinder = PlatformSpecificImplementationFactory
				.getLastLocationFinder(this);
		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		map = mapFragment.getMap();

		if (map == null) {
			finish();
			return;
		}

		init();
		handleIntent(getIntent());
	}

	 private void handleIntent(Intent intent){
	        if(intent.getAction().equals(Intent.ACTION_SEARCH)){
	            doSearch(intent.getStringExtra(SearchManager.QUERY));
	        }else if(intent.getAction().equals(Intent.ACTION_VIEW)){
	            getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
	        }
	    }

	private void init() {
		Location location = lastLocationFinder.getLastBestLocation(
				GeoConstants.MAX_DISTANCE, GeoConstants.MAX_TIME);
		 LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		    map.animateCamera(cameraUpdate);
		    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		
	    

		Log.d("myLogs", location.getLatitude() + ", " + location.getLongitude());

	}

	@Override
	protected void onResume() {
		super.onResume();
	}



	public void OnPressOk(View view) {
		Log.d("myLogs", "OK");
		// this.startService(new Intent(this, Services.class));

	}

	public void OnPressCancel(View view) {
		Log.d("myLogs", "Cancel");
	}

	 @Override
	    protected void onNewIntent(Intent intent) {
	        super.onNewIntent(intent);
	        setIntent(intent);
	        handleIntent(intent);
	    }
	 
	    private void doSearch(String query){
	        Bundle data = new Bundle();
	        data.putString("query", query);
	        getSupportLoaderManager().restartLoader(0, data, this);
	    }
	 
	    private void getPlace(String query){
	        Bundle data = new Bundle();
	        data.putString("query", query);
	        getSupportLoaderManager().restartLoader(1, data, this);
	    }
	 
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu
	        getMenuInflater().inflate(R.menu.main, menu);
	        return true;
	    }
	 
	    @Override
	    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	        switch(item.getItemId()){
	        case R.id.action_search:
	            onSearchRequested();
	            break;
	        }
	        return super.onMenuItemSelected(featureId, item);
	    }
	 
	    @Override
	    public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
	        CursorLoader cLoader = null;
	        if(arg0==0)
	            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{ query.getString("query") }, null);
	        else if(arg0==1)
	            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{ query.getString("query") }, null);
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
	 
	    private void showLocations(Cursor c){
	        MarkerOptions markerOptions = null;
	        LatLng position = null;
	        map.clear();
	        while(c.moveToNext()){
	            markerOptions = new MarkerOptions();
	            position = new LatLng(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)));
	            markerOptions.position(position);
	            markerOptions.title(c.getString(0));
	            map.addMarker(markerOptions);
	        }
	        if(position!=null){
	            CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(position);
	            map.animateCamera(cameraPosition);
	        }
	    }
	}