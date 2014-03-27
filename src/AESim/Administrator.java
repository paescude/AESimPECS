package AESim;

import repast.simphony.engine.schedule.ScheduledMethod;

public class Administrator extends SimObject {
	
	
	private SimObject whoBlockedMe;
	private double averageTimeAllPatients;
	private static Administrator admin = new Administrator();
	
	private Administrator(){
//		System.out.println("Singleton(): Initializing Instance");
		
	}
	
	public static Administrator getAdmin(){
		return admin;
	}
	
	
	
	
	
	
	
	
	@ScheduledMethod(start = 60, interval = 60, pick = 1, priority = 101)
	public void increaseTime() {
		/* Increase hour of the day by 1 */

		hour++;
		if (hour == 24) {
			hour = 0;
			day++;
			if (day == 7) {
				day = 0;
				// Day 0: Monday ... Day=6: Sunday
				week++;
				if (week == 52)
					week = 0;
			}
		}

	}
	
	public Resource findResourceAvailable(String resourceType) {
		Resource rAvailable = null;
		context = getContext();
		for (Object o : context.getObjects(Resource.class)) {
			Resource resource = (Resource) o;
			if (resource.getResourceType() == resourceType) {
			/*	System.out.println("resource type? " + resourceType
						+ " is required here, looking if " + resource.getId()
						+ " is available? " + resource.isAvailable());*/
				if (resource.isAvailable() == true) {
					rAvailable = resource;
					break;
				} else {
					SimObject simObject= this.checkWhoInResource();
				}
			}
		}
		return rAvailable;
	}
	
	@ScheduledMethod(start = 10, interval = 10, pick = 1)
	public void calculateAverageTimeAllPatientsAdmin() {
		
		
		
		double sumTimeInSys = 0;
		double totalPatients = 0;
		context =getContext();
		//context = ContextUtils.getContext(this);
		Patient patient = null;
		for (Object o : context.getObjects(Patient.class)) {
			if (o != null) {
				patient = (Patient) o;
				if (patient.isInSystem()) {
					sumTimeInSys += patient.getTimeInSystem();
					totalPatients++;

				}
			}

		}
		if (totalPatients != 0) {
			setAverageTimeAllPatients(sumTimeInSys / totalPatients);
			
		}
		
		
		

	}	
	
	public SimObject checkWhoInResource() {
		SimObject simObject= null;
//		System.out.println("the objects at  " + this.getId() + " : ");
		grid= getGrid();
		int x= grid.getLocation(this).getX();
		int y= grid.getLocation(this).getY();
		for (Object o : grid.getObjectsAt(x,y)) {
			if (o instanceof SimObject){
				 simObject = (SimObject) o;
//				System.out.print("\t" +simObject.getId() + ", ");
			}
		}
		if (this.getWhoBlockedMe()!=null){
//		System.out.println(this.getWhoBlockedMe().getId() + " has  " + this.getId() + " blocked ");
		}
//		System.out.println("\n");
		
		return simObject;
	}

	public SimObject getWhoBlockedMe() {
		return whoBlockedMe;
	}

	public void setWhoBlockedMe(SimObject whoBlockedMe) {
		this.whoBlockedMe = whoBlockedMe;
	}

	public double getAverageTimeAllPatients() {
		return averageTimeAllPatients;
	}

	public void setAverageTimeAllPatients(double averageTimeAllPatients) {
		this.averageTimeAllPatients = averageTimeAllPatients;
	}
	
}
