package AESim;

import java.util.ArrayList;

import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Agent extends SimObject {
	
	protected Resource myResource;
	protected static  ArrayList<Patient> patientsWaitingForCubicle = new ArrayList<Patient>();
	
	public GridPoint moveTo(Grid<Object> grid, GridPoint newLoc) {
		GridPoint loc = newLoc;
		grid.moveTo(this, loc.getX(), loc.getY());
		this.setLoc(grid.getLocation(this));
		return this.getLoc();
	}

	public Resource getMyResource() {
		return myResource;
	}

	public void setMyResource(Resource myResource) {
		this.myResource = myResource;
	}
}
