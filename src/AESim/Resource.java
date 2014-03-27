package AESim;

import java.util.ArrayList;

import Funciones.MathFunctions;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Resource extends SimObject {

	private static int count;
	private boolean available;
	private String resourceType;
	private static int countTypeXRay = 1;
	private static int countTypeTest = 1;
	private static int countTypeTriage = 1;
	private static int countTypeTrolley = 1;
	private static int countTypeMinor = 1;
	private static int countTypeMajor = 1;
	private static int countTypeResus = 1;
	
	private int numAvailableXray;
	private int numAvailableTest;
	private int typeResource;
	private ArrayList<Double> testTsample = new ArrayList<>();
	private ArrayList<Double> xRayTsample = new ArrayList<>();
	private double processTime[] = new double[3];
	
	public Resource(String resourceType, String rName, Grid<Object> grid) {
		this.available = true;
		this.resourceType = resourceType;
		switch (resourceType) {
		case "triage cubicle ":
			this.setId(resourceType + countTypeTriage++);
			break;
		case "trolley ":
			this.setId(resourceType + countTypeTrolley++);
			break;
		case "minor cubicle ":
			this.setId(resourceType + countTypeMinor++);
			break;
		case "major cubicle ":
			this.setId(resourceType + countTypeMajor++);
			break;
		case "resus cubicle ":
		this.setId(resourceType + countTypeResus++);
			break;
		default:
			break;
		}
		if (resourceType.equals("xRayRoom ")) {
			this.setId(resourceType + countTypeXRay++);
			this.setNumAvailableXray(1);
			processTime[0] = 20;
			processTime[1] = 40;
			processTime[2] = 32;
		} else if (resourceType.equals("testRoom ")) {
			this.setId(resourceType + countTypeTest++);
			this.setNumAvailableTest(1);
			processTime[0] = 10;
			processTime[1] = 25;
			processTime[2] = 20;
		} else {
			processTime[0] = 0;
			processTime[1] = 0;
			processTime[2] = 0;
		}
		this.setTypeResource(0);
		this.grid = grid;
	}
	
	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueXRay", triggerCondition = "$watcher.getNumAvailableXray()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, pick = 1)
	public void checkConditionsForXRay() {
		printTime();
		// watchedAgent.increaseTestCounterXray();
//		System.out.println(" XRay has started ");
		Resource rAvailable = findResourceAvailable("xRayRoom ");
		if (rAvailable != null) {
			GridPoint locQueue = this.getQueueLocation("qXRay ", grid);
			int locQX = locQueue.getX();
			int locQY = locQueue.getY();
			Patient fstpatient = null;
			// The head of the queue is at (x,y-1)
			// XXX aqui yo cambio (locX, locY-1) por (11, locY-1) porque
			// hay dos salas de test y una cola.
			Object o = grid.getObjectAt(locQX, locQY + 1);
//			System.out
//					.println(" the object that is ahead of QXray is "
//							+ o
//							+ "\n this means that there is a patient to be seen in Xray ");
			if (o != null) {
				if (o instanceof Patient) {
					fstpatient = (Patient) o;
					startXRay(rAvailable, fstpatient);
				}

			} else {
			
			}

		} else {
//			System.out.println("There is not an available resource");
		}

	}

	public void startXRay(Resource rAvailable, Patient fstpatient) {
		fstpatient.increaseTestCounterXray();
		GridPoint loc = rAvailable.getLoc();
		GridPoint locQueue = this.getQueueLocation("qXRay ", grid);
		int locX = loc.getX();
		int locY = loc.getY();
		grid.moveTo(fstpatient, locX, locY);
		if (fstpatient.getMyNurse() != null){
			grid.moveTo(fstpatient.getMyNurse(), locX, locY);
		}
		
//		System.out
//				.println(fstpatient.getId()
//						+ " hast moved to "
//						+ fstpatient.getLoc().toString()
//						+ "\n that is the same position that the resource he will be using has: "
//						+ rAvailable.getLoc().toString());
		rAvailable.setAvailable(false);

		fstpatient.setMyResource(rAvailable);

		Doctor doctor = fstpatient.getMyDoctor();
//		System.out.println(" at start Xray " + fstpatient.getId()
//				+ " has in mind the doctor " + doctor.getId());

		QueueSim queue = ((QueueSim) grid.getObjectAt(locQueue.getX(),
				locQueue.getY()));
		queue.removeFromQueue(fstpatient);
		queue.elementsInQueue();
		rAvailable.setNumAvailableXray(0);
		scheduleEndXRay(fstpatient, rAvailable);
		Patient newfst = null;
		Object o2 = grid.getObjectAt(locQueue.getX(),
				locQueue.getY());
		if (o2 instanceof QueueSim) {
			QueueSim newQueue = (QueueSim) o2;
			if (newQueue.firstInQueue() != null) {
				newfst = newQueue.firstInQueue();
				grid.moveTo(newfst, locQueue.getX(),
						(locQueue.getY() + 1));
			} else{
//				System.out.println(" there is not patient in "
//						+ newQueue.getId());
			}
		}
	}
	private void scheduleEndXRay(Patient fstpatient, Resource resource) {

		double min = fstpatient.getMyResource().getProcessTime()[0];
		double mean = fstpatient.getMyResource().getProcessTime()[1];
		double max = fstpatient.getMyResource().getProcessTime()[2];

		double serviceTime = MathFunctions.distLognormal(min, mean, max);
		fstpatient.settXray(serviceTime);
		this.xRayTsample.add(serviceTime);
		if (fstpatient.getTriageNum()==1){
//			System.out.println(" triage blue, service time " + fstpatient.gettXray());
		}
		if (fstpatient.getTriageNum()==2){
//			System.out.println(" triage green, service time " + fstpatient.gettXray());
		}
		
		if (fstpatient.getTriageNum()==3){
//			System.out.println(" triage yellow, service time " + fstpatient.gettXray());
		}
		
		if (fstpatient.getTriageNum()==4){
//			System.out.println(" triage orange, service time " + fstpatient.gettXray());
		}
		if (fstpatient.getTriageNum()==5){
//			System.out.println(" triage red, service time " + fstpatient.gettXray());
		}
//		System.out.println(" Xray times: " + this.xRayTsample);

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		double timeEndService = schedule.getTickCount() + serviceTime;

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndXRay action2 = new EndXRay(resource, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

//		System.out.println("Start Xray " + fstpatient.getId()
//				+ " expected to end at " + timeEndService);

	}

	private static class EndXRay implements IAction {
		;
		private Patient patient;
		private Resource resource;

		private EndXRay(Resource resource, Patient patient) {
			this.resource = resource;
			this.patient = patient;

		}

		@Override
		public void execute() {
			resource.endXRay(this.resource, this.patient);

		}

	}

	public void endXRay(Resource resource, Patient patient) {
		// grid.moveTo(nurse, this.initPosX, this.initPosY);
		printTime();
//		System.out.println("End Xray " + patient.getId() + " is at "
//				+ patient.getLoc().toString());
		resource.setAvailable(true);
		patient.setMyResource(null);
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess + 1);
		resource.setNumAvailableXray(1);
		patient.getTimeInSystem();
		patient.setInSystem(true);
		patient.setWasInXray(true);
		if (patient.getNextProc() == 1) {
//			System.out.println(patient.getId() + " is joining q test ");
			patient.addToQ("qTest ");

		} else if (patient.getNextProc() == 2) {
			patient.decideWhereToGo();
		}

//		System.out.println(" start Xray about to start");
		checkConditionsForXRay();

	}

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueTest", triggerCondition = "$watcher.getNumAvailableTest()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void checkConditionsForTest() {
		printTime();
		Resource rAvailable = findResourceAvailable("testRoom ");
//		System.out.println(" who is this in Start test " + this.getId());
		if (rAvailable != null) {
			GridPoint locQueue = this.getQueueLocation("qTest ", grid);
			int locQX = locQueue.getX();
			int locQY = locQueue.getY();
			if (this.available) {
				Patient fstpatient = null;
				// The head of the queue is at (x,y-1)
				// aqui yo cambio (locX, locY-1) por (11, locY-1) porque hay dos
				// salas de test y una cola.
				Object o = grid.getObjectAt(locQX, locQY + 1);
				if (o != null) {
					if (o instanceof Patient) {
						fstpatient = (Patient) o;
						startTest(rAvailable, fstpatient);
					}

				}
			}

			else {

			}

		}

	}

	public void startTest(Resource rAvailable, Patient fstpatient) {
		GridPoint loc = rAvailable.getLoc();
		int locX = loc.getX();
		int locY = loc.getY();
		GridPoint locQueue = this.getQueueLocation("qTest ", grid);
		fstpatient.increaseTestCounterTest();
		// grid.moveTo(this, locX, locY);
		grid.moveTo(fstpatient, locX, locY);
		if (fstpatient.getMyNurse()!=null){
			grid.moveTo(fstpatient.getMyNurse(), locX, locY);
		}
		
		fstpatient.setMyResource(rAvailable);
		Doctor doctor = fstpatient.getMyDoctor();
//		System.out.println(" at start of test "
//				+ fstpatient.getId()
//				+ " has in mind the doctor " + doctor.getId());
//		System.out
//				.println("checking if doctor has this patient in mind in test");
		if (doctor.getMyPatientsInTests().contains(fstpatient)) {
//			System.out.println(doctor.getId() + " has "
//					+ fstpatient.getId()
//					+ " in ' my patients in test' ");
		} else {
//			System.err
//					.println(" ERROR: Patient is in test and doctor has no memory of him in test. Adding "
//							+ fstpatient.getId()
//							+ " to "
//							+ doctor.getId());
			doctor.getMyPatientsInTests().add(fstpatient);
		}

		QueueSim queue = ((QueueSim) grid.getObjectAt(
				locQueue.getX(), locQueue.getY()));
		queue.removeFromQueue(fstpatient);
		queue.elementsInQueue();
		rAvailable.setAvailable(false);
		rAvailable.setNumAvailableTest(0);
		scheduleEndTest(fstpatient, rAvailable);
		Patient newfst = null;
		Object o2 = grid.getObjectAt(locQueue.getX(),
				locQueue.getY());
		if (o2 instanceof QueueSim) {
			QueueSim newQueue = (QueueSim) o2;
			if (newQueue.firstInQueue() != null) {
				newfst = newQueue.firstInQueue();
				grid.moveTo(newfst, locQueue.getX(),
						(locQueue.getY() + 1));
			}
		}
	}
	
	private void scheduleEndTest(Patient fstpatient, Resource resource) {
		double min = fstpatient.getMyResource().getProcessTime()[0];
		double mean = fstpatient.getMyResource().getProcessTime()[1];
		double max = fstpatient.getMyResource().getProcessTime()[2];

		double serviceTime = MathFunctions.distLognormal(min, mean, max);
fstpatient.settTest(serviceTime);
		this.testTsample.add(serviceTime);
//		System.out.println(" test times: " + this.testTsample);

		// // double serviceTime = triangularObs(fstpatient.getMyResource()
		// // .getProcessTime()[0], fstpatient.getMyResource()
		// // .getProcessTime()[1], fstpatient.getMyResource()
		// // .getProcessTime()[2]);
		//
		// double serviceTime = 10;

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		double timeEndService = schedule.getTickCount() + serviceTime;

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndTest action2 = new EndTest(resource, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

//		System.out.println("Start test " + fstpatient.getId()
//				+ " expected to end at " + timeEndService);
		
	}
	
	private static class EndTest implements IAction {

		private Patient patient;
		private Resource resource;

		private EndTest(Resource resource, Patient patient) {
			this.resource = resource;
			this.patient = patient;

		}

		@Override
		public void execute() {
			resource.endTest(this.resource, this.patient);

		}

	}

	public void endTest(Resource resource, Patient patient) {
		printTime();
//		System.out.println("End test " + patient.getId() + " is at "
//				+ patient.getCurrentQueue().getId());
		resource.setAvailable(true);
		patient.setWasInTest(true);
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess + 1);
		patient.setMyResource(null);
		resource.setNumAvailableTest(1);
		patient.decideWhereToGo();
//		System.out.println(" new test is about to start ");
		checkConditionsForTest();
	}

	public static void initSaticVars() {
		count = 0;		
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Resource.count = count;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public static int getCountTypeXRay() {
		return countTypeXRay;
	}

	public static void setCountTypeXRay(int countTypeXRay) {
		Resource.countTypeXRay = countTypeXRay;
	}

	public static int getCountTypeTest() {
		return countTypeTest;
	}

	public static void setCountTypeTest(int countTypeTest) {
		Resource.countTypeTest = countTypeTest;
	}

	public static int getCountTypeTriage() {
		return countTypeTriage;
	}

	public static void setCountTypeTriage(int countTypeTriage) {
		Resource.countTypeTriage = countTypeTriage;
	}

	public static int getCountTypeTrolley() {
		return countTypeTrolley;
	}

	public static void setCountTypeTrolley(int countTypeTrolley) {
		Resource.countTypeTrolley = countTypeTrolley;
	}

	public static int getCountTypeMinor() {
		return countTypeMinor;
	}

	public static void setCountTypeMinor(int countTypeMinor) {
		Resource.countTypeMinor = countTypeMinor;
	}

	public static int getCountTypeMajor() {
		return countTypeMajor;
	}

	public static void setCountTypeMajor(int countTypeMajor) {
		Resource.countTypeMajor = countTypeMajor;
	}

	public static int getCountTypeResus() {
		return countTypeResus;
	}

	public static void setCountTypeResus(int countTypeResus) {
		Resource.countTypeResus = countTypeResus;
	}

	public int getNumAvailableXray() {
		return numAvailableXray;
	}

	public void setNumAvailableXray(int numAvailableXray) {
		this.numAvailableXray = numAvailableXray;
	}

	public int getNumAvailableTest() {
		return numAvailableTest;
	}

	public void setNumAvailableTest(int numAvailableTest) {
		this.numAvailableTest = numAvailableTest;
	}

	public double[] getProcessTime() {
		return processTime;
	}

	public void setProcessTime(double processTime[]) {
		this.processTime = processTime;
	}

	public int getTypeResource() {
		return typeResource;
	}

	public void setTypeResource(int typeResource) {
		this.typeResource = typeResource;
	}

	public ArrayList<Double> getTestTsample() {
		return testTsample;
	}

	public void setTestTsample(ArrayList<Double> testTsample) {
		this.testTsample = testTsample;
	}


}
