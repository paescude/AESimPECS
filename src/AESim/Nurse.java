package AESim;


import java.util.ArrayList;

import cern.jet.random.Uniform;
import Datos.Reader;
import Funciones.MathFunctions;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Nurse extends Staff {
	public static final double[] NURSE_TRIAGE_PARAMS = { 4,6,8 };
//	XXX aqui se cambiaron los parametros y la distribucion, estaban errados
	private static int count;
	private double[] timeTriage = new double[3];
	private int typeNurse; //type nurse=1 treatment, type=2, triage
	private String typeNurseName;

	public Nurse(Grid<Object> grid, int x1, int y1, int idNum, int multiTasking) {
		this.grid = grid;
		this.available = true;
		this.setId("Nurse " + idNum);
		this.idNum = idNum;
		this.numAvailable = 0;
		this.initPosX = x1;
		this.initPosY = y1;
		this.myResource = null;
		this.multiTaskingFactor = multiTasking; 	
		this.patientsInMultitask = new ArrayList<Patient>();
		this.setTypeNurse(1);
		this.setTimeTriage(NURSE_TRIAGE_PARAMS);
	}
	
	@Override
	public void resetVariables() {
		// TODO Auto-generated method stub
		this.setInShift(false);
		this.setAvailable(false);
		int i = this.idNum;
		int x = 18;
		int y = i + 4;
		grid.moveTo(this, x, y);
	}
	
	@Override
	public void requiredAtWork() {
		// TODO Auto-generated method stub
		if (this.isInShift() == false) {
//			System.out.println(this.getId() + " will move to nurse area"
//					+ " method: schedule work"
//					+ " this method is being called by " + this.getId());
			grid.moveTo(this, this.initPosX, this.initPosY);
			this.numAvailable = this.multiTaskingFactor;
			this.setInShift(true);
			this.setAvailable(true);
//			System.out.println(this.getId() + " is in shift and is available at "
//					+ getTime());
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see AESim.Staff#decideWhatToDoNext()
	 * 	@Override
	public void decideWhatToDoNext() {
		if (this.numAvailable == this.multiTaskingFactor && checkIfAnyForTriage()){
			this.checkConditionsForTriage();
		} else if (this.numAvailable > 0){
			if (checkIfAnyForTriage()){
				
			} else {
				this.moveToNurseArea();
			}			
		}
	}
	 */
	
	
	@Override
	public void decideWhatToDoNext() {
		if (this.getTypeNurse()==2){
		if (this.numAvailable == this.multiTaskingFactor && checkIfAnyForTriage()){
			this.checkConditionsForTriage();
		} else if (this.numAvailable > 0){
			if (checkIfAnyForTriage()){
				
			} else {
				this.moveToNurseArea();
			}			
		}
	}
		else {
			this.moveToNurseArea();
		}
	}
	
	private void moveToNurseArea() {
		if (this.patientsInMultitask.size() < this.multiTaskingFactor) {			
			boolean flag = false;
			int y = 2;
			int x;
			for (int j = 0; j < 2; j++) {
				for (int i = 1; i < 6; i++) {
					Object o = grid.getObjectAt(i + 6, y + j);
					if (o == null) {
						x = i + 6;
						grid.moveTo(this, x, y + j);
//						System.out.println(this.getId()
//								+ " has moved to nurses area "
//								+ this.getLoc().toString()
//								+ " at time " + getTime());
						flag = true;
						break;
					}
					if (flag) {
						break;
					}
				}
				if (flag) {
					break;
				}
			}
		this.setInShift(true);
		this.setAvailable(true);
//		System.out.println(this.getId()
//				+ " is in shift and is available at " + getTime());
	} else {
		this.setAvailable(false);
	}
		
	}

	private boolean checkIfAnyForTriage() {
		Object o = getGrid().getObjectAt(5,2);
		if (o instanceof Patient){
			return true;
		} else {
			return false;
		}
	}
//	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueTriage", triggerCondition = "$watcher.getNumAvailable()==$watcher.getMultiTaskingFactor()", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)

//	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueTriage", triggerCondition = "$watcher.getNumAvailable()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueTriage", triggerCondition = "$watcher.getNumAvailable()>0 && $watcher.getTypeNurse()>1", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void checkConditionsForTriage() {
		printTime();
		Resource rAvailable = findResourceAvailable("triage cubicle ");		
		if (rAvailable != null) {
			GridPoint loc = rAvailable.getLoc();
			int locX = loc.getX();
			int locY = loc.getY();
			if (this.available) {
				// System.out.println(" this is: " + this.getId());
				Patient fstpatient = null;
				// The head of the queue is at (x,y-1)
				Object o = grid.getObjectAt(5, 2);
				if (o != null) {
					if (o instanceof Patient) {
						fstpatient = (Patient) o;
						grid.moveTo(this, locX, locY);
						grid.moveTo(fstpatient, locX, locY);
						this.setMyResource(rAvailable);
						fstpatient.setMyResource(rAvailable);
						GridPoint locQueue = this.getQueueLocation(
								"queueTriage ", grid);
						QueueSim queue = ((QueueSim) grid.getObjectAt(
								locQueue.getX(), locQueue.getY()));
						queue.removeFromQueue(fstpatient);
						queue.elementsInQueue();
						/* se utiliza esto (no engageWithPatient) porque el triage ocupa totalmente a la enfermera */
						this.setNumAvailable(0);
						this.setAvailable(false);
						rAvailable.setAvailable(false);
//						System.out.println("Start triage " + fstpatient.getId() + " with " + this.getId());
						scheduleEndTriage(fstpatient);
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
			} else {
				// System.out.println("estoy en el watch y hay cola");
			}
		}
	}
	
	public void scheduleEndTriage(Patient fstpatient) {

		double serviceTime = MathFunctions.distTriangular(NURSE_TRIAGE_PARAMS[0],
				NURSE_TRIAGE_PARAMS[1], NURSE_TRIAGE_PARAMS[2]);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();
		double timeEndService = schedule.getTickCount() + serviceTime;
		this.nextEndingTime= timeEndService;
		fstpatient.settTriage(serviceTime);
//		System.out.println(" triage " + fstpatient.getId()
//				+ " expected to end at " + timeEndService);
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndTriage action2 = new EndTriage(this, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);	
	}
	
	private static class EndTriage implements IAction {
		private Nurse nurse;
		private Patient patient;

		private EndTriage(Nurse nurse, Patient patient) {
			this.nurse = nurse;
			this.patient = patient;

		}

		@Override
		public void execute() {
			nurse.endTriage(this.nurse, this.patient);

		}

	}

	public void endTriage(Nurse nurse, Patient patient) {
		printTime();
//		System.out.println("end triage " + patient.getId());
		this.getMyResource().setAvailable(true);
		this.setMyResource(null);
		patient.setMyResource(null);
//		System.out.println(patient.getId() + " will get triage category");
		nurse.startTriage(patient);
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess+1);
		this.setAvailable(true);
		/* se utiliza esto (no releaseWithPatient) porque el triage ocupa totalmente a la enfermera */
		this.setNumAvailable(this.multiTaskingFactor);		
		this.decideWhatToDoNext();
	}
	
	private void startTriage(Patient patient) {
		Uniform unif = RandomHelper.createUniform();
		double rnd = unif.nextDouble();
		float[][] probsTriage = Reader.getMatrixTriagePropByArrival();
		//only patients by walk in are triaged by nurse. Ambulance patients are triaged ny ambulanceIn
		double rndDNW = RandomHelper.createUniform().nextDouble();
		float [][] matrixDNW = Reader.getArrayDNW();		
		if (rndDNW <= matrixDNW[getHour()][0]) {
			this.removePatientFromDepartment(patient);
		}

		else {
			if (rnd <= probsTriage[0][0]) {
				patient.setTriage("Blue ");
				patient.setTriageNum(1);

				double rndTreat = Math.random();
				if (rndTreat < Patient.PROB_BLUE_PATIENT_IN_TREATMENT) {
					patient.setGoToTreatRoom(true);
					patient.addToQ("qBlue ");
				} else {
					this.removePatientFromDepartment(patient);
				}

			} else if (probsTriage[0][0] < rnd && rnd <= probsTriage[1][0]) {
				patient.setTriage("Green ");
				patient.setTriageNum(2);
				patient.addToQ("qGreen ");
				patient.setGoToTreatRoom(true);
			} else if (probsTriage[1][0] < rnd && rnd <= probsTriage[2][0]) {
				patient.setTriage("Yellow ");
				patient.setTriageNum(3);
				patient.addToQ("qYellow ");
				patient.setGoToTreatRoom(true);
			} else if (probsTriage[2][0] < rnd && rnd <= probsTriage[3][0]) {
				patient.setTriage("Orange ");
				patient.setTriageNum(4);
				patient.addToQ("qOrange ");
				patient.setGoToTreatRoom(true);
			} else if (probsTriage[3][0] < rnd && rnd <= probsTriage[4][0]) {
				Nurse.patientsRedTriage.add(patient);
				patient.setTriage("Red ");
				patient.setTriageNum(5);
				patient.addToQ("qRed ");				
				patient.setGoToTreatRoom(true);
			}
//			System.out.println(patient.getId() + " triage num = "
//					+ patient.getTriageNum() + " has moved to "
//					+ patient.getCurrentQueue().getId());
		}
	}

	@ScheduledMethod(start = 0, priority = 99, shuffle = false)
	public void initNumNurses() {
		printTime();
//		System.out.println("When simulation starts, the nurse conditions are "
//				+ this.getId());
		GridPoint currentLoc = grid.getLocation(this);
		int currentX = currentLoc.getX();
		int currentY = currentLoc.getY();

		if (currentX == 18) {
			this.setAvailable(false);
			this.setInShift(false);
//			System.out.println(this.getId()
//					+ " is not in shift and is not available, time: "
//					+ getTime());

		} else if (currentY == 2) {
			this.setAvailable(true);
			this.setInShift(true);
//			System.out.println(this.getId()
//					+ " is in shift and is available, time: " + getTime());
		}

		
		int id = this.idNum;
		this.setMyShiftMatrix(Reader.getMatrixNurse(id));
		this.setShifts();
		

//		System.out.println(this.getId() + " shift's duration ["
//				+ this.durationOfShift[0] + " ," + this.durationOfShift[1]
//				+ "," + this.durationOfShift[2] + " ,"
//				+ this.durationOfShift[3] + " ," + this.durationOfShift[4]
//				+ ", " + this.durationOfShift[5] + ", "
//				+ this.durationOfShift[6] + "]");
	}
	
	public static void initSaticVars() {
		setCount(1);		
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Nurse.count = count;
	}

	public double[] getTimeTriage() {
		return timeTriage;
	}

	public void setTimeTriage(double[] timeTriage) {
		this.timeTriage = timeTriage;
	}

	public int getTypeNurse() {
		return typeNurse;
	}

	public void setTypeNurse(int typeNurse) {
		this.typeNurse = typeNurse;
		if (typeNurse==1){
			this.setTypeNurseName("treatment");
		}
		else if (typeNurse==2){
			this.setTypeNurseName("triage");
		}
	}

	private void setTypeNurseName(String typeNurseName) {
		this.typeNurseName= typeNurseName;
		// TODO Auto-generated method stub
		
	}




}
