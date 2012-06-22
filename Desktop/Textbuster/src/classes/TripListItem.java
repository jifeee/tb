package classes;

import java.util.Date;

import org.joda.time.DateTime;

public class TripListItem {
	
	private int id;
	private String startLocation;
	private String endLocation;
	private DateTime startTime; 
	private DateTime endTime;
	private double distance; 
	
	public TripListItem (int id, String startLocation, String endLocation, DateTime startTime, DateTime endTime, double distance) {
		this.id = id;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
		this.startTime = startTime; 
		this.endTime = endTime; 
		this.distance = distance;	
	}
	
	
	public DateTime getStartTime() {
		return startTime;
	}
	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}
	public String getEndLocation() {
		return endLocation;
	}
	public void setEndLocation(String endLocation) {
		this.endLocation = endLocation;
	}
	public String getStartLocation() {
		return startLocation;
	}
	public void setStartLocation(String startLocation) {
		this.startLocation = startLocation;
	}
	public DateTime getEndTime() {
		return endTime;
	}
	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	} 

}
