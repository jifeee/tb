package classes;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class Trips {
	
	ArrayList<Integer> id = new ArrayList<Integer>();
	ArrayList<String> startLocation = new ArrayList<String>();
	ArrayList<String> endLocation = new ArrayList<String>();
	ArrayList<DateTime> startTime = new ArrayList<DateTime>();
	ArrayList<DateTime> endTime = new ArrayList<DateTime>();
	ArrayList<ArrayList<String>> points = new ArrayList<ArrayList<String>>();
	ArrayList<Double> speed = new ArrayList<Double>();
	ArrayList<Double> distance = new ArrayList<Double>();
	
	public void addTrip (int id, String startLocation, String endLocation, DateTime startTime, DateTime endTime, 
			ArrayList<String> points, double speed, double distance) {
		
		this.id.add(id);
		this.startLocation.add(startLocation);
		this.endLocation.add(endLocation);
		this.startTime.add(startTime);
		this.endTime.add(endTime);
		this.points.add(points);
		this.speed.add(speed);
		this.distance.add(distance);
	}

	public Integer getId (int pos) {
		return id.get(pos);
	}
	public String getStartLocation (int pos) {
		return startLocation.get(pos);
	}
	public String endStartLocation (int pos) {
		return endLocation.get(pos);
	}
	public DateTime getStartTime (int pos) {
		return startTime.get(pos);
	}
	public DateTime getEndTime (int pos) {
		return endTime.get(pos);
	}
	public ArrayList<String> getPoints (int pos) {
		return points.get(pos);
	}
	public Double getSpeed (int pos) {
		return speed.get(pos);
	}
	public Double getDistance (int pos) {
		return distance.get(pos);
	}
	
}
