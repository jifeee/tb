package com.access2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import classes.Constants;
import classes.Converter;
import classes.PathOverlay;
import classes.UserStatus;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class TripMapActivity extends MapActivity {
	
	UserStatus myUserStatus;
	Context ctx;
	JSONObject details;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		ctx=this;
		String TAG ="TEX";
		
		myUserStatus = Constants.myUserStatus;
		
		
		MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        List<GeoPoint> path = new ArrayList<GeoPoint>();
        
        try {

        	JSONObject o = new JSONObject(myUserStatus.getDetailsByID(myUserStatus.getSelectedTripID()));
        	
            JSONArray array = new JSONArray(o.getString("points"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject points = array.getJSONObject(i);
                Converter c = new Converter();
                
                int lat = c.convertDouble(Double.parseDouble(points.getString("latitude")));
                int lon = c.convertDouble(Double.parseDouble(points.getString("longitude")));
                
                Log.i(TAG, "Point lat: " + lat + " lon: " + lon);
                
                path.add(new GeoPoint(lat, lon));

            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		mapView.getOverlays().add(new PathOverlay(path, Color.CYAN, true));
		mapView.getController().animateTo(path.get(path.size()-1));

	}
	
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    
}
