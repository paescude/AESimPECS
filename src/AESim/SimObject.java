package AESim;

import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class SimObject {
	
	protected String id;
	protected int idNum;	
	protected Grid<Object> grid;
//	protected GridPoint loc;
	protected Context<Object> context;
	protected static double minute;
	protected static int hour;
	protected static int day;
	protected static int week;
	protected static int dayTotal;
	protected double averageTimeAllPatients;
	private static double minTotalWeek;
	
	protected static double getTime() {
		double time = (RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount());
		return time;
	}
	
	public void printTime() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay()
						+ " hour: "
						+ getHour()
						+ " minute: "
						+ getMinute() + ")");
	}
	protected void removePatientFromDepartment(Patient patient){
		printTime();
//		System.out.println(patient.getId() + " didn't wait and has left  the department after triage with "+ this.getId());
//		System.out.println(patient.getId() + " goes to qTrolley");
		patient.addToQ("qTrolley ");
		try {
			Exit.addToFile(patient);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		patient.setInSystem(false);
		patient.getTimeInSystem();
		
//		System.out.println(patient.getId() + " is in system = " + patient.isInSystem());
		patient.getAllServicesTimes();
	}
	protected Resource findResourceAvailable(String resourceType) {
		Resource rAvailable = null;
		context = getContext();
		for (Object o : context.getObjects(Resource.class)) {
			Resource resource = (Resource) o;
			if (resource.getResourceType() == resourceType) {
				if (resource.isAvailable() == true) {
//					System.out.println("resource type? " + resourceType
//							+ " is required here, looking if " + resource.getId()
//							+ " is available? " + resource.isAvailable());
					rAvailable = resource;
					break;
				} else {
//					System.out.println("NOT AVAILABLE: resource type? " + resourceType
//							+ " is required here, looking if " + resource.getId()
//							+ " is available? " + resource.isAvailable());
					
				}
			}
		}
		return rAvailable;
	}
	
	public void printElementsQueue(PriorityQueue<Patient> queueToPrint,
			String name) {

		Patient patientQueuing = null;
		Iterator<Patient> iter = queueToPrint.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2){
			System.out.println("" + this.getId() + " " + name + ": "
					+ a.substring(0, a.length() - 2) + "]");
		}
	}

	public void printElementsArray(ArrayList<Patient> arrayToPrint, String name) {

		Patient patientQueuing = null;
		Iterator<Patient> iter = arrayToPrint.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2){
			System.out.println("" + this.getId() + " " + name + ": "
					+ a.substring(0, a.length() - 2) + "]");
		}
	}
	
	public void printElementsLinkedList(LinkedList<Patient> arrayToPrint, String name) {

		Patient patientQueuing = null;
		Iterator<Patient> iter = arrayToPrint.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2){
//			System.out.println("" + this.getId() + " " + name + ": "
//					+ a.substring(0, a.length() - 2) + "]");
		}
	}
	
	public GridPoint getQueueLocation(String name, Grid grid) {
		GridPoint queueLoc = null;
		QueueSim queueR = null;
		context = ContextUtils.getContext(this);

		for (Object o : context.getObjects(QueueSim.class)) {
			queueR = (QueueSim) o;
			if (queueR.getName() == name) {
				queueLoc = grid.getLocation(o);
				// System.out.println("**** "+ queueR.getId()+ " "
				// + queueLoc);
				break;
			}

		}
		return queueLoc;
	}
	
	@ScheduledMethod(start = 10, interval = 10, pick = 1)
	public double [] calculateAverageTimeAllPatients() {
		
		double info [] = new double [3];
		
		double sumTimeInSys = 0;
		double totalPatients = 0;
		double maxTimeInSys = 0;
		context =getContext();
		//context = ContextUtils.getContext(this);
		Patient patient = null;
		for (Object o : context.getObjects(Patient.class)) {
			if (o != null) {
				patient = (Patient) o;
				if (patient.isInSystem()) {
					sumTimeInSys += patient.getTimeInSystem();
					if(patient.getTimeInSystem()>maxTimeInSys){
						maxTimeInSys = patient.getTimeInSystem();
					}
					totalPatients++;

				}
			}

		}
		if (totalPatients != 0) {
			averageTimeAllPatients = sumTimeInSys / totalPatients;
			info[0] = averageTimeAllPatients;
			info[1] = totalPatients;
			info[2] = maxTimeInSys;
		}
		
		
		return info;

	}	
	
	public static void initSaticVar() {
		setMinute(0);
		setMinTotalWeek(0);
		setHour(0);
		setDay(0);
		setWeek(0);
		setDayTotal(0);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIdNum() {
		return idNum;
	}

	public void setIdNum(int idNum) {
		this.idNum = idNum;
	}

	public Grid<Object> getGrid() {
		grid = (Grid) ContextUtils.getContext(this).getProjection("grid");
		return grid;
	}

	public void setGrid(Grid<Object> grid) {
		this.grid = grid;
	}

	public GridPoint getLoc() {
		return grid.getLocation(this);
	}

//	public void setLoc(GridPoint loc) {
//		this.loc = loc;
//	}

	public Context<Object> getContext() {
		context = ContextUtils.getContext(this);
		return context;
	}

	public void setContext(Context<Object> context) {
		this.context = context;
	}
	
	public static void setMinTotalWeek(double min){
		SimObject.minTotalWeek = min;
	}
	public static double getMinTotalWeek(){
		double tick = (RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount());
		minTotalWeek = (tick % 10080);
		return minTotalWeek;
	}

	public static double getMinute() {
		double tick = (RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount());
		minute = (tick % 60);
		return minute;
	}

	public static void setMinute(double minute) {
		SimObject.minute = minute;
	}

	public static int getHour() {
		return hour;
	}

	public static void setHour(int hour) {
		SimObject.hour = hour;
	}

	public static int getDay() {
		return day;
	}

	public static void setDay(int day) {
		SimObject.day = day;
	}

	public static int getWeek() {
		return week;
	}

	public static void setWeek(int week) {
		SimObject.week = week;
	}

	public static int getDayTotal() {
		return dayTotal;
	}

	public static void setDayTotal(int dayTotal) {
		SimObject.dayTotal = dayTotal;
	}
	
}
