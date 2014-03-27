package AESim;

import java.util.ArrayList;

import Datos.Reader;
import Funciones.MathFunctions;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Clerk extends Staff {
	private static int count;
	private static final double[] CLERK_REGISTRATION_TIME_PARAMS = { 3, 5, 8 };

	public Clerk(Grid<Object> grid, int idNum, int x, int y, int multiTasking) {
		this.grid = grid;
		this.id = "Clerk " + idNum;
		this.loc = getLoc();
		this.idNum = idNum;
		this.numAvailable = 1;
		this.initPosX = x;
		this.initPosY = y;
		this.multiTaskingFactor = multiTasking;
		this.available = true;
		this.inShift = false;
		this.patientsInMultitask = new ArrayList<Patient>();
		this.getLoc(grid);
	}
	
	@Override
	public void decideWhatToDoNext() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void requiredAtWork() {
		if (this.isInShift() == false) {
//			System.out.println(this.getId() + " will move to clerk area"
//					+ " method: schedule work"
//					+ " this method is being called by " + this.getId());
			grid.moveTo(this, this.initPosX, this.initPosY);
			this.setInShift(true);
			this.setAvailable(true);
//			System.out.println(this.getId() + " is in shift and is available at "
//					+ getTime());
			if (this.inShift){
				startRegistration();
			}
		}
	}
	
	@Override
	public void resetVariables() {
		// TODO Auto-generated method stub
		this.setInShift(false);
		this.setAvailable(false);
		int i = this.idNum;
		int x = 17;
		int y = i + 4;
		grid.moveTo(this, x, y);
	}
	
	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQr", triggerCondition = "$watchee.getNumWasFstForRegist()>0 && $watcher.getNumAvailable()>0", scheduleTriggerPriority=2, whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void startRegistration() {
		printTime();
		if (this.available) {
			Patient fstpatient = null;
			Object o = grid.getObjectAt(1, 2);
		
			if (o != null) {
				if (o instanceof Patient) {
					fstpatient = (Patient) o;
//					int xClerk= grid.getLocation(this).getX(); 
//					int yClerk= grid.getLocation(this).getY();					
					fstpatient.moveTo(grid, this.getLoc());
					GridPoint locQueue = this.getQueueLocation("queueR ", grid);
					QueueSim queue = ((QueueSim) grid.getObjectAt(locQueue.getX(),
							locQueue.getY()));
					queue.removeFromQueue(fstpatient);
					queue.elementsInQueue();
					this.engageWithPatient(fstpatient);
					double serviceTime = MathFunctions.distTriangular(
							CLERK_REGISTRATION_TIME_PARAMS[0],
							CLERK_REGISTRATION_TIME_PARAMS[1],
							CLERK_REGISTRATION_TIME_PARAMS[2]);
					double timeEndService = scheduleEndRegistration(fstpatient,
							serviceTime);
					//XXX cambié setNumWas... porque estaba entrando dos veces al regist
					fstpatient.setNumWasFstForRegist(0);
//					System.out.println(this.getId() + " Starts registration "
//							+ fstpatient.getId() + " expected to end at "
//							+ timeEndService);
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
			}

		}

		else {

		}

	}

	public double scheduleEndRegistration(Patient fstpatient, double serviceTime) {
		fstpatient.settRegist(serviceTime);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();
		double timeEndService = schedule.getTickCount()
				+ serviceTime;

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndRegistration action2 = new EndRegistration(this,
				fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);
		return timeEndService;
	}
	private static class EndRegistration implements IAction {
		private Clerk clerk;
		private Patient patient;

		private EndRegistration(Clerk clerk, Patient patient) {
			this.clerk = clerk;
			this.patient = patient;

		}

		@Override
		public void execute() {
			clerk.endRegistration(this.patient);
		}

	}
	
	public void endRegistration(Patient patient) {
		printTime();
		GridPoint locQueue = this.getQueueLocation("queueTriage ", grid);
		grid.moveTo(patient, locQueue.getX(), locQueue.getY());
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess+1);;
//		System.out.println("End Registration " + patient.getId() + " is at "
//				+ patient.getLoc().toString());
		this.releaseFromPatient(patient);
//		System.out.println(patient.getId() + " is added to Q triage");
		patient.addToQ("queueTriage ");		
	
		this.startRegistration();
	}
	
	@ScheduledMethod(start = 0, priority = 99, shuffle = false)
	public void initNumClerks() {
		printTime();
//		System.out.println("When simulation starts, the clerk conditions are "
//				+ this.getId());
		GridPoint currentLoc = grid.getLocation(this);
		int currentX = currentLoc.getX();
		int currentY = currentLoc.getY();
		if (currentX == 17) {
			this.setAvailable(false);
			this.setInShift(false);
//			System.out.println(this.getId()
//					+ " is not in shift and is not available, time: "
//					+ getTime());
		} else if (currentY == 4) {
			this.setAvailable(true);
			this.setInShift(true);
//			System.out.println(this.getId()
//					+ " is in shift and is available, time: " + getTime());
		}
		int id = this.idNum;
		this.setMyShiftMatrix(Reader.getMatrixClerk(id));
		this.setShifts();
		

//		System.out.println(this.getId() + " shift's duration ["
//				+ this.durationOfShift[0] + " ," + this.durationOfShift[1]
//				+ "," + this.durationOfShift[2] + " ,"
//				+ this.durationOfShift[3] + " ," + this.durationOfShift[4]
//				+ ", " + this.durationOfShift[5] + ", "
//				+ this.durationOfShift[6] + "]");
		
////		TODO borrar esto 
//		if (this.inShift){
//			startRegistration();
//		}
	}

	private GridPoint getLoc(Grid<Object> grid) {
		loc = grid.getLocation(this);
		return loc;
	}

	public static void initSaticVars() {
		setCount(0);		
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Clerk.count = count;
	}

	


}
