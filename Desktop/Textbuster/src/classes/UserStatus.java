package classes;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UserStatus implements Serializable {
	
	String TAG = "TEX";
	
	private boolean loggedIn=false;
	private String token="";
	private String email;
	private String password; 
	private String userID;
	private String lastError;
	private String devices;
	private int selectedTripID;
	
	private ArrayList<String> trips = new ArrayList<String>();
	
	private ArrayList<String> details = new ArrayList<String>();
	private ArrayList<Integer> detailsID = new ArrayList<Integer>();
	
	public String getTripByID (int reqID) throws JSONException {
		
		String resultTrip = null;
		for (int i=0; i<trips.size(); i++) {
			JSONObject o = new JSONObject (trips.get(i));
			if (Integer.parseInt(o.getString("trip_id")) == reqID) {
				resultTrip = o.toString();
			}
		}
		
		return resultTrip;
		
		
	}
	

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String userName) {
		this.email = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}
	
	public void logStatus () {
		Log.i(TAG, "logged in " + isLoggedIn() + " token " + getToken() + " password " + getPassword() 
				+ " email " + getEmail() + " userid " + getUserID() + " last error " + getLastError() + " devices " + getDevices());
	}

	public String getDevices() {
		return devices;
	}

	public void setDevices(String devices) {
		this.devices = devices;
	}

	public ArrayList<String> getTrips() {
		return trips;
	}

	public void setTrips(ArrayList<String> trips) {
		this.trips = trips;
	}

	public String getDetails (int pos) {
		return details.get(pos); 
	}
	
	public void addDetails (String details, int detailsID) {
		this.details.add(details);
		this.detailsID.add(detailsID);
	}
	
	public String getDetailsByID (int ID) {
		int index = 0; 
		for (int i=0; i<detailsID.size(); i++) {
			
			
			if (ID == detailsID.get(i)) {
				index = i;
			}
		}
		return details.get(index);
	}
	
	

	public int getSelectedTripID () {
		return selectedTripID;
	}

	public void setSelectedTripID (int selectedTripID) {
		this.selectedTripID = selectedTripID;
	}

}
