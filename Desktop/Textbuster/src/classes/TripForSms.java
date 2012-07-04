package classes;

import java.util.UUID;

import android.location.Location;

public class TripForSms {
	
	private Location startLoc;
	private Location endLoc;
	private String startAdr;
	private String endAdr;
	private String id;
	
	
	public TripForSms (Location startLoc, Location endLoc, String id) {
		this.setStartLoc(startLoc);
		this.setEndLoc(endLoc);
		this.setId(id);
	}


	public Location getStartLoc() {
		return startLoc;
	}


	public void setStartLoc(Location startLoc) {
		this.startLoc = startLoc;
	}


	public Location getEndLoc() {
		return endLoc;
	}


	public void setEndLoc(Location endLoc) {
		this.endLoc = endLoc;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getStartAdr() {
		return startAdr;
	}


	public void setStartAdr(String startAdr) {
		this.startAdr = startAdr;
	}


	public String getEndAdr() {
		return endAdr;
	}


	public void setEndAdr(String endAdr) {
		this.endAdr = endAdr;
	}
	
	public String toString() {
		return "UUID: " + getId() + " startLoc: " + getStartLoc().toString() + " endLoc: " + endLoc.toString() 
				+ " startAdr: " + getStartAdr() + " endAdr: " + getEndAdr();
	}

}
