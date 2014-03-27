package AESim;

import java.util.ArrayList;
import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public abstract class Staff extends Agent {

	protected boolean available;
	protected int initPosX;
	protected int initPosY;
	protected int numAvailable;
	protected boolean inShift;
	protected double timeInitShift;
	protected float[][] myShiftMatrix;
	protected float[][] durationOfShift = new float[7][24];
	protected int requiredAtWork;
	protected double nextEndingTime;
	protected int multiTaskingFactor;
	protected ArrayList<Patient> patientsInMultitask;
	protected static final ArrayList<Patient> patientsRedTriage = new ArrayList<>();

	protected void moveOut() {
		resetVariables();
		// System.out.println(this.getId()
		// + "  has finished his shift and has moved out to "
		// + this.getLoc().toString());

	}
//TODO SUMAR SOLO LOS UNOS DEL SHIFT 
	public double calculateWorkedTimeHours() {
		double timeInHours = (getTime()- this.getTimeInitShift())/60;
//		for (int i = 0; i < getHour(); i++) {
//			timeInHours = timeInHours
//					+ (int) this.getMyShiftMatrix()[i][getDay()];
//		}
//		if (this.inShift) {
//			double timeHours = getTime() / 60;
//			double timeFloor = Math.floor(getTime() / 60);
//			double minute = (timeHours - timeFloor);
//			timeInHours = timeInHours + (float) minute;
//		}

		return timeInHours;
	}

	public void engageWithPatient(Patient patient) {
		if (!this.patientsInMultitask.contains(patient)){
		this.patientsInMultitask.add(patient);
		// System.out.println(this.getId() +
		// " has added from patientsInMultitask " + patient.getId());
		this.updateMultitask(true);
		// System.out.println(this.getId()
		// + " is updating multitasking "
		// + patient.getId() + " is in multitasking.  ");

		// System.out.println("My multitasking factor is "
		// + this.multiTaskingFactor);
		printElementsArray(this.patientsInMultitask,
				" patients in multitasking");
	}
	}
	public void releaseFromPatient(Patient patient) {
		this.patientsInMultitask.remove(patient);
		// System.out.println(this.getId()
		// + " is updating multitasking "
		// + patient.getId() + " is in multitasking.  ");
		// System.out.println(this.getId() +
		// " has removed from patientsInMultitask " + patient.getId());
		this.updateMultitask(false);

		// System.out.println("My multitasking factor is "
		// + this.multiTaskingFactor);
		printElementsArray(this.patientsInMultitask,
				" patients in multitasking");
		// System.out.println(this.getId() + " will decide what to do next");
		if (!patient.isFromOtherDoctor()) {
			this.decideWhatToDoNext();
		}
		if (this.getClass().equals(Nurse.class)) {
			this.decideWhatToDoNext();
		}

	}

	public void updateMultitask(boolean startSomethingWithAPatient) {
		if (this.isInShift()) {
			if (startSomethingWithAPatient) {
				this.numAvailable -= 1;
				if (this.numAvailable == 0) {
					this.setAvailable(false);
				}
			} else {
				if (this.numAvailable < this.multiTaskingFactor) {
					this.numAvailable += 1;
					setAvailable(true);
				}
			}

		}
	}

	@ScheduledMethod(start = 0, interval = 60, priority = 90, shuffle = false)
	public void scheduleWork() {
		int hour = getHour();
		int day = getDay();
		// System.out.println("\n \n " + this.getId() +
		// " is doing method : SCHEDULE WORK ");
		int requiredAtWork = (int) this.getMyShiftMatrix()[hour][day];
		if (requiredAtWork == 0) {
			// TODO voy a cambiar inSHift = 0 aqui para evitar que reciba mas
			// gente. Ojo que puede haber error: 06/03/2014
			this.setInShift(false);

			// System.out.println(this.getId() +
			// " is not required at work at time " + getTime());
			printTime();
			if (this.patientsInMultitask.size()==0) {
				this.canMoveOut();
			} else {
				this.canNotMoveOut();
			}
		} else if (requiredAtWork == 1) {
			this.requiredAtWork();
		}

	}

	// XXX NURSE MULTITASK
	@ScheduledMethod(start = 0, interval = 60, priority = 89, shuffle = false)
	public void selectNurseForTriage() {
		if (this.getClass().equals(Nurse.class)) {
			Nurse nurse = null;
			int counter = 0;
			// System.out.println(this.getId()+" this is");
			Context<Object> nuevoContexto = getContext();
			// for (Object object : nuevoContexto.getObjects(Nurse.class) ){
			// if (object!= null){
			// System.out.println("object");
			// }
			// }
			//
			for (Object o : nuevoContexto.getObjects(Nurse.class)) {
				nurse = (Nurse) o;
				// System.out.println(nurse.getId());
				if (nurse.isInShift()) {
					// System.out.println(nurse.getId() + " is in shift");
					if (nurse.multiTaskingFactor == 1) {
						// System.out.println(nurse.getId() + " MTF=1");
						counter += 1;

						break;
					}

				}
			}
			if (counter == 0) {
				for (Object n : context.getObjects(Nurse.class)) {
					nurse = (Nurse) n;
					if (nurse.isInShift()
							&& nurse.getNumAvailable() == nurse.multiTaskingFactor) {
						nurse.multiTaskingFactor = 1;
						nurse.setTypeNurse(2); // Nurse type=2 is for triage
						break;
					}
				}
			}
		}
	}

	protected void scheduleEndShift(double timeEnding) {
		// System.out.println(" current time: " + getTime() + " " +this.getId()
		// + " is supposed to move out at: " + timeEnding);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEnding);
		Endshift actionEnd = new Endshift(this);

		schedule.schedule(scheduleParams, actionEnd);
	}

	protected static class Endshift implements IAction {
		private Staff staff;

		public Endshift(Staff staff) {
			this.staff = staff;
		}

		@Override
		public void execute() {
			staff.endShift();
		}

	}

	protected void endShift() {
		printTime();
		// System.out.println(this.getId()
		// + " has finished the shift and will move out at " +getTime());
		this.moveOut();
	}

	protected void canMoveOut() {
		// System.out
		// .println(this.getId()
		// + " has finished his shift and is moving to not working area");
		this.moveOut();
	}

	protected void canNotMoveOut() {
		double timeEnding= this.getNextEndingTime();
		if (getTime() < timeEnding ) {
			
			printTime();
			// System.out
			// .println(this.getId()
			// +
			// " has finished his shift but needs to wait to leave because he still has work to do");
//			double max=0;
//			Patient  patient= null;
//			for (int i = 0; i <this.patientsInMultitask.size(); i++) {
//				patient= this.patientsInMultitask.get(i);
//				if (max>=patient.getTimeEndCurrentService()){
//					max = patient.getTimeEndCurrentService();
//				}
//			}
////			double timeEnding = this.nextEndingTime;
//			double timeEnding= max;
			this.scheduleEndShift(timeEnding + 5);

		}
	}

	public abstract void decideWhatToDoNext();

	public abstract void requiredAtWork();

	public abstract void resetVariables();

	protected Nurse findNurse() {
		Nurse nurse = null;
		Nurse nurseAvailable = null;
		for (int i = 7; i < 12; i++) {
			Object o = getGrid().getObjectAt(i, 2);
			if (o instanceof Nurse) {
				nurse = (Nurse) o;
				if (nurse.getTypeNurse() == 1) {
					// FIXME estamos intendando dejar a una enfermera encargada
					// solamente del triage if (nurse.isAvailable()){}
					if (nurse.isAvailable()
							&& nurse.getMultiTaskingFactor() > 1) {
						nurseAvailable = nurse;
						break;
					}
				}
			}
		}
		return nurseAvailable;
	}

	protected Resource findBed(int triage) {
		Resource resource = null;
		switch (triage) {
		case 1:
			resource = findResourceAvailable("minor cubicle ");
			break;
		case 2:
			resource = findResourceAvailable("minor cubicle ");
			break;
		case 3:
			resource = findResourceAvailable("major cubicle ");
			if (resource == null) {
				resource = findResourceAvailable("minor cubicle ");
			}
			break;
		case 4:
			double rndResus = RandomHelper.createUniform().nextDouble();
			if (rndResus < Patient.PROB_ORANGE_PATIENT_IN_RESUS_ROOM) {
				resource = findResourceAvailable("resus cubicle ");
				break;
			}
			resource = findResourceAvailable("major cubicle ");
			if (resource == null) {
				resource = findResourceAvailable("minor cubicle ");
			}
			break;
		case 5:
			resource = findResourceAvailable("resus cubicle ");
			break;
		}
		return resource;
	}
	
	public void setMyShiftMatrix(){
		int id = this.idNum;
		switch(id) {
		case 1:
			if (this instanceof Doctor){
				
			} 
		}
	}

	public boolean isAvailable() {
		return available;
	}
	
	public int nextHour(int hour){
		return (hour+1)%(7*24);
	}
	
	public int prevHour(int hour){
		return (hour-1+7*24)%(7*24);
	}
	
	public int getShiftAtHour(int day, int hour){
		int cant = 0;
		if (getMyShiftMatrix()[hour][day]==0){
			return cant;
		}
		cant=1;
		int startHour = day*24+hour;
		int currentHour = nextHour(startHour); 
		while(getMyShiftMatrix()[currentHour%24][currentHour/24]>0.5){
			++cant;
			currentHour = nextHour(currentHour);
			if (currentHour == startHour){
				return cant;
			}
		}
		currentHour = prevHour(startHour);
		while(getMyShiftMatrix()[currentHour%24][currentHour/24]>0.5){
			++cant;
			currentHour = prevHour(currentHour);
		}
		return cant;
	}
	
	public void setShifts() {
		this.durationOfShift = new float [7][24];
		float sum = 0;
		
		for (int i = 0; i < 7; i++) {
			sum = 0;
			for (int j = 0; j < 24; j++) {
				sum =this.getShiftAtHour(i,j);
				this.setDurationOfShift(i,j,sum);
			}
		}
		sum = 0;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}

	public int getInitPosX() {
		return initPosX;
	}

	public void setInitPosX(int initPosX) {
		this.initPosX = initPosX;
	}

	public int getInitPosY() {
		return initPosY;
	}

	public void setInitPosY(int initPosY) {
		this.initPosY = initPosY;
	}

	public int getNumAvailable() {
		return numAvailable;
	}

	public void setNumAvailable(int numAvailable) {
		this.numAvailable = numAvailable;
	}

	public boolean isInShift() {
		return inShift;
	}

	public void setInShift(boolean inShift) {
		this.inShift = inShift;
	}

	public double getTimeInitShift() {
		return timeInitShift;
	}

	public void setTimeInitShift(double timeInitShift) {
		this.timeInitShift = timeInitShift;
	}

	public float[][] getMyShiftMatrix() {
		return myShiftMatrix;
	}

	public void setMyShiftMatrix(float[][] myShiftMatrix) {
		this.myShiftMatrix = myShiftMatrix;
	}

	public float[][] getDurationOfShift() {
		return durationOfShift;
	}

	public void setDurationOfShift(int day, int hour, float val){
		this.durationOfShift[day][hour] = val;
	}
	
	public float getDurationOfShift(int day, int hour){
		return this.durationOfShift[day][hour];
	}
	
	public String getDurationOfShiftText(){
		float [][] durationOfShift = getDurationOfShift();
		String ret = "[";
		for (int i = 0; i < durationOfShift.length; ++i) {
			ret += " "+durationOfShift[i]+" ";
		}
		ret += "]";
		return ret;
	}

	public void setDurationOfShift(float[][] durationOfShift) {
		this.durationOfShift = durationOfShift;
	}

	public int getRequiredAtWork() {
		return requiredAtWork;
	}

	public void setRequiredAtWork(int requiredAtWork) {
		this.requiredAtWork = requiredAtWork;
	}

	public double getNextEndingTime() {
		double max=0;
		Patient  patient= null;
		for (int i = 0; i <this.patientsInMultitask.size(); i++) {
			patient= this.patientsInMultitask.get(i);
			if (max>=patient.getTimeEndCurrentService()){
				max = patient.getTimeEndCurrentService();
			}
		}
//		double timeEnding = this.nextEndingTime;
		this.nextEndingTime= max;
		return nextEndingTime;
	}

	public void setNextEndingTime(double nextEndingTime) {
		this.nextEndingTime = nextEndingTime;
	}

	public int getMultiTaskingFactor() {
		return multiTaskingFactor;
	}

	public void setMultiTaskingFactor(int multiTaskingFactor) {
		this.multiTaskingFactor = multiTaskingFactor;
	}

	public ArrayList<Patient> getPatientsInMultitask() {
		return patientsInMultitask;
	}

	public void setPatientsInMultitask(ArrayList<Patient> patientsInMultitask) {
		this.patientsInMultitask = patientsInMultitask;
	}

}
