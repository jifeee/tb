package classes;

import java.util.ArrayList;

public class TripDetails {
	
	private ArrayList<String> points = new ArrayList<String>();
	
	public void setPoints (ArrayList<String> points) {
		this.points= points;
	}
	
	public ArrayList<String> getPoints () {
		return this.points;
	}

}
