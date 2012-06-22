package classes;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;



public class MyLocationListener implements LocationListener {

	String TAG="TEX";
	
	@Override
    public void onLocationChanged(Location myLocation) {
    
		
//		Log.i(TAG, myLocation.toString());
		
    }

    @Override
    public void onProviderDisabled(String provider) {
      
    }

    @Override
    public void onProviderEnabled(String provider) {
     
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
     
    }
	
	
}
