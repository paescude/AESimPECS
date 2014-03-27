package AESim;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Patient extends Agent {
	
	public static final double PROB_BLUE_PATIENT_IN_TREATMENT = 0.7;
	public static final double PROB_ORANGE_PATIENT_IN_RESUS_ROOM = 0.8;
	private static int count;
	private int testCountX;
	private int testCountT;	
	private int totalProcesses;
	private int weekOutSystem;
	private int dayOutSystem;
	private int hourOutSystem;	
	private Doctor myDoctor;
	private Nurse myNurse;
	private int weekInSystem;
	private int dayInSystem;
	private int hourInSystem;
	private int totalNumTest; 
	private boolean hasReachedtarget;	
	private boolean isFromOtherDoctor;
	private boolean isInSystem;
	private boolean isEnteredSystem;
	private boolean wasInTest;
	private boolean wasInXray;
	private boolean waitInCublicle;
	private boolean backInBed;
	private String triage;
	private int triageNum;
	private double queuingTimeQr;
	private double queuingTimeQt;
	private double queuingTimeQa;
	private double timeOfArrival;
	private double timeInSystem;
	private QueueSim currentQueue;
	private double timeEnteredCurrentQ;
	private double timeOutCurrentQueue;
	private int nextProc;
	private double timeEndCurrentService;
	// 1 if goes to test and 2 if goes back to bed
	private String typeArrival;
	private boolean wasFirstInQr;
	private double testRatio;
	private boolean wasFirstInQueueTriage;
	private boolean wasFirstInQueueXRay;
	private boolean wasFirstInQueueTest;
	private boolean wasFirstforAsses;	
	private int isWaitingBedReassessment;
	private Resource myBedReassessment;	
	private int numWasFstForAssess;
	private int numWasFstForRegist;
	private int numWasFstForTriage;
	private int numWasFstForTest;
	private int numWasFstForXRay;
	
	private boolean goToResusRoom;
	private boolean goToTreatRoom;
	private boolean needsTests;
	private int reassessmentDone;
	private double tRegist;
	private double tTriage;
	private double tFirstAssessment;
	private double tReasssesment;
	private double tXray;
	private double tTest;

	
	public Patient(Grid<Object> grid, String typeArrival, double time) {
		count++;
		this.setId("Patient " + count);
		this.idNum = count;
		this.grid = grid;
		this.queuingTimeQr = 0;
		this.triage = " has not been triaged ";
		this.triageNum = 0;
		this.wasFirstforAsses = false;
		this.wasFirstInQr = false;
		this.hasReachedtarget = false;
		this.wasFirstInQueueXRay = false;
		this.wasFirstInQueueTest = false;
		this.typeArrival = typeArrival;
		this.timeOfArrival = time;
		this.timeInSystem = 0;
		this.queuingTimeQr = 0;
		this.queuingTimeQt = 0;
		this.queuingTimeQa = 0;
		this.currentQueue = null;
		this.timeEnteredCurrentQ = 0;
		this.timeOutCurrentQueue = 0;
		this.myResource = null;
		this.isInSystem = true;
		this.myBedReassessment = null;
		this.myDoctor = null;
		this.nextProc = 0;
		this.backInBed = false;
		this.isEnteredSystem = true;
		this.isFromOtherDoctor = false;
		this.wasInTest = false;
		this.wasInXray = false;
		this.numWasFstForAssess = 0;
		this.testCountX = 0;
		this.testCountT = 0;
		this.totalProcesses = 0;
		this.timeEndCurrentService = 0;
		this.myNurse = null;
		this.waitInCublicle = true;
		this.goToResusRoom = false;
		this.goToTreatRoom = false;
		this.isWaitingBedReassessment = 0;
		this.needsTests = false;
		this.tRegist=0;
		this.tTriage= 0;
		this.tFirstAssessment= 0;
		this.tReasssesment=0;
		this.tXray=0;
		this.tTest=0;
	}
	
	public void addToQ(String name) {

		QueueSim queue = null;
		GridPoint locQueue = this.getQueueLocation(name, grid);
		queue = (QueueSim) grid.getObjectAt(locQueue.getX(), locQueue.getY());
		queue.addPatientToQueue(this);
		this.setCurrentQueue(queue);
		this.setTimeEnteredCurrentQ(getTime());
		grid.moveTo(this, locQueue.getX(), locQueue.getY());
		// queue.elementsInQueue();
System.out.println(" *****************  " + this.getId()
				+ " has joined " + queue.getId() + " current loc "
				+ this.getLoc().toString() + " time: " + getTime());
		if(!name.equals("qTrolley")){
			queue.elementsInQueue();
		}
		
		// patient will seat in the patch in front of the queue if he is the
		// head of the queue
		Patient patientTocheck = queue.firstInQueue();
		if (patientTocheck == this) {
			this.moveToHeadOfQ(queue);
		}
	}
	
	@ScheduledMethod(start=10, interval=10, shuffle= false)
	public void getMyTimeInSys(){
				this.getTimeInSystem();
	}
	
	public void moveToHeadOfQ(QueueSim queue) {

		int newX = queue.getLoc().getX();
		int newY = queue.getLoc().getY() + 1;

System.out.println("when " + this.getId()+ " has reached the head of the queue, the objects in " + queue.getId());
		queue.elementsInQueue();
		

		Object whoInFirst = grid.getObjectAt(newX, newY);
		
		if (whoInFirst instanceof Patient) {
			Patient patientAtHead = (Patient) whoInFirst;
System.out.println("alreade there is at the head of "+ queue.getId() +": " +  patientAtHead.getId());
		} else {
			grid.moveTo(this, newX, newY);
System.out.println(this.getId() + " has moved to the head of: " + queue.getId() + " loc: "+ this.getLoc().toString() + " time: " + getTime());

			if (this.getCurrentQueue().getId() == "queueR ") {
				this.numWasFstForRegist=1;
				this.setWasFirstInQr(true);
			}
			else if (this.getCurrentQueue().getId() == "queueTriage ") {
				this.numWasFstForTriage=1;
				this.setWasFirstInQueueTriage(true);
			}
			else if (this.getCurrentQueue().getId() == "qBlue ") {
				this.numWasFstForAssess=1;
				this.setWasFirstforAsses(true);
			}
			else if (this.getCurrentQueue().getId() == "qGreen ") {
				this.numWasFstForAssess=1;
				this.setWasFirstforAsses(true);
			}
			else if (this.getCurrentQueue().getId() == "qYellow ") {
				this.numWasFstForAssess=1;
				this.setWasFirstforAsses(true);
			}
			else if (this.getCurrentQueue().getId() == "qOrange ") {
				this.numWasFstForAssess=1;
				this.setWasFirstforAsses(true);
			}
			else if (this.getCurrentQueue().getId() == "qRed ") {
				this.numWasFstForAssess=1;
				this.setWasFirstforAsses(true);
			}
			else if (this.getCurrentQueue().getId() == "qTest ") {
				this.numWasFstForTest=1;
				this.setWasFirstInQueueTest(true);
			}
			else if (this.getCurrentQueue().getId() == "qXRay ") {
				this.numWasFstForXRay=1;
				this.setWasFirstInQueueXRay(true);
			}

		}
	}
	
	public void addToQR() {		
		QueueSim queueR = null;
		GridPoint locQueueR = this.getQueueLocation("queueR ", grid);
		queueR = (QueueSim) grid.getObjectAt(locQueueR.getX(), locQueueR.getY());

		GridPoint currentLoc = this.getLoc();

		int x = currentLoc.getX();
		int y = currentLoc.getY();

		if (x == 1 && y == 0) {
			queueR.addPatientToQueue(this);
			this.setCurrentQueue(queueR);
			this.setTimeEnteredCurrentQ(getTime());
			grid.moveTo(this, locQueueR.getX(), locQueueR.getY());		
		}
		
		queueR.elementsInQueue();
//TODO Esto parece malo, mirar coordenadas
		if (queueR.firstInQueue() == this) {
			this.moveToHeadOfQ(queueR);
//			Object whoInFirst = grid.getObjectAt(1, 2);
//			if (whoInFirst instanceof Patient) {
//
//			} else {
//				grid.moveTo(this, 1, 2);
//				this.setWasFirstInQr(true);
//			}
//			
//
		}

		
	}
	
	public void decideWhereToGo() {		
		if (this.waitInCublicle == false) {
			Doctor myDoctor = this.getMyDoctor();
			Resource bed = myDoctor.findBed(this.getTriageNum());
			if (bed != null) {
				this.isWaitingBedReassessment = 0;
				this.setMyBedReassessment(bed);
				if (this.getMyBedReassessment()!=null){
System.out.println(this.getMyBedReassessment().getId()+ " is available " + this.getMyBedReassessment().isAvailable() );
					this.getMyBedReassessment().setAvailable(false);
				} else {
System.err.println("CualquiercosaYA ya ya!");
				}				
				this.moveBackToBed(bed);
			} else {
				this.isWaitingBedReassessment = 1;
				//may need an array here to add pati wait for b reass
				this.addToQ("queueBReassess ");
System.out.println(this.getId() + " has joined qBReassess.");
				patientsWaitingForCubicle.add(this);
			}
		} else {
			Resource myBed = this.getMyBedReassessment();
			this.moveBackToBed(myBed);	
System.out.println(this.getId() + " is back to his bed reassessment "+ myBed.getId());
		}
		if (this.getMyNurse() !=null){
			this.getMyNurse().moveTo(grid, this.getLoc());
		}
		
	}

	public void moveBackToBed(Resource bed) {
		this.getTimeInSystem();
		this.setInSystem(true);		
		Doctor doctor = this.getMyDoctor();
		if (doctor == null) {
System.err.println("\n ERROR: there is no doctor with patient");
		} else {
System.out.println(this.getId()
					+ " has in mind the " + doctor.getId());
			this.moveTo(grid, bed.getLoc());
	
System.out.println(this.getId() + " has moved to bed reassessment "
					+ bed.getId());
			doctor.setMyPatientCalling(this);
			if(this.needsTests){
				doctor.myPatientsInTests.remove(this);
System.out.println(doctor.getId()
						+ " has removed from his patients in test " + this.getId());
				if (!this.waitInCublicle){
					doctor.myPatientsInBed.add(this);					
System.out.println(doctor.getId()
							+ " has added to his patients in bed " + this.getId());
				}				
			}						
			doctor.getMyPatientsBackInBed().add(this);
			//FIXME patientsForReassessment
//			Doctor.patientsForReassessment.add(this);
			this.setMyResource(bed);
System.out.println(" AFTER TESTS (or starting ReAssess inmediately) " + this.getId()
					+ " has in mind the doctor " + doctor.getId());						
			String nameD = doctor.getId();
			printElementsQueue(doctor.getMyPatientsInBed(), nameD +
					" my patients in bed");
			printElementsArray(doctor.getMyPatientsInTests(), nameD
					+ " my patients in test ");
			this.setBackInBed(true);

		}
	
	}
	
	
	public void getAllServicesTimes(){
		this.gettRegist();
		this.gettTriage();
		this.gettFirstAssessment();
		this.gettReasssesment();
		this.gettTest();
		this.gettXray();
System.out.println(this.getId() + " tregistration, tTriage, tFirstAssessment, Treassessment, TTest, TxRay" + " [ " 
				+ this.gettRegist() +", " + this.gettTriage() +", "+ this.gettFirstAssessment() +", "+this.gettReasssesment()+", "
				+ this.gettTest() +", "+ this.gettXray() +"]");
		
	}

	public static void initSaticVars() {
		setCount(0);		
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Patient.count = count;
	}

	public int getTestCountX() {
		return testCountX;
	}

	public void setTestCountX(int testCountX) {
		this.testCountX = testCountX;
	}

	public int getTestCountT() {
		return testCountT;
	}

	public void setTestCountT(int testCountT) {
		this.testCountT = testCountT;
	}

	public int getTotalProcesses() {
		return totalProcesses;
	}

	public void setTotalProcesses(int totalProcesses) {
		this.totalProcesses = totalProcesses;
	}

	public int getWeekOutSystem() {
		return weekOutSystem;
	}

	public void setWeekOutSystem(int weekOutSystem) {
		this.weekOutSystem = weekOutSystem;
	}

	public int getDayOutSystem() {
		return dayOutSystem;
	}

	public void setDayOutSystem(int dayOutSystem) {
		this.dayOutSystem = dayOutSystem;
	}

	public int getHourOutSystem() {
		return hourOutSystem;
	}

	public void setHourOutSystem(int hourOutSystem) {
		this.hourOutSystem = hourOutSystem;
	}

	public Doctor getMyDoctor() {
		return myDoctor;
	}

	public void setMyDoctor(Doctor myDoctor) {
		this.myDoctor = myDoctor;
	}

	public Nurse getMyNurse() {
		return myNurse;
	}

	public void setMyNurse(Nurse myNurse) {
		this.myNurse = myNurse;
	}

	public int getWeekInSystem() {
		return weekInSystem;
	}

	public void setWeekInSystem(int weekInSystem) {
		this.weekInSystem = weekInSystem;
	}

	public int getDayInSystem() {
		return dayInSystem;
	}

	public void setDayInSystem(int dayInSystem) {
		this.dayInSystem = dayInSystem;
	}

	public int getHourInSystem() {
		return hourInSystem;
	}

	public void setHourInSystem(int hourInSystem) {
		this.hourInSystem = hourInSystem;
	}

	public int getTotalNumTest() {
		return totalNumTest;
	}

	public void setTotalNumTest(int totalNumTest) {
		this.totalNumTest = totalNumTest;
	}

	public boolean isHasReachedtarget() {
		return hasReachedtarget;
	}

	public void setHasReachedtarget(boolean hasReachedtarget) {
		this.hasReachedtarget = hasReachedtarget;
	}

	public boolean isInSystem() {
		return isInSystem;
	}

	public void setInSystem(boolean isInSystem) {
		this.isInSystem = isInSystem;
	}

	public boolean isEnteredSystem() {
		return isEnteredSystem;
	}

	public void setEnteredSystem(boolean isEnteredSystem) {
		this.isEnteredSystem = isEnteredSystem;
	}

	public boolean isWasInTest() {
		return wasInTest;
	}

	public void setWasInTest(boolean wasInTest) {
		this.wasInTest = wasInTest;
	}

	public boolean isWasInXray() {
		return wasInXray;
	}

	public void setWasInXray(boolean wasInXray) {
		this.wasInXray = wasInXray;
	}

	public boolean isWaitInCublicle() {
		return waitInCublicle;
	}

	public void setWaitInCublicle(boolean waitInCublicle) {
		this.waitInCublicle = waitInCublicle;
	}

	public boolean isBackInBed() {
		return backInBed;
	}

	public void setBackInBed(boolean backInBed) {
		this.backInBed = backInBed;
	}

	public String getTriage() {
		return triage;
	}

	public void setTriage(String triage) {
		this.triage = triage;
	}

	public int getTriageNum() {
		return triageNum;
	}

	public void setTriageNum(int triageNum) {
		this.triageNum = triageNum;
	}

	public double getQueuingTimeQr() {
		return queuingTimeQr;
	}

	public void setQueuingTimeQr(double queuingTimeQr) {
		this.queuingTimeQr = queuingTimeQr;
	}

	public double getQueuingTimeQt() {
		return queuingTimeQt;
	}

	public void setQueuingTimeQt(double queuingTimeQt) {
		this.queuingTimeQt = queuingTimeQt;
	}

	public double getQueuingTimeQa() {
		return queuingTimeQa;
	}

	public void setQueuingTimeQa(double queuingTimeQa) {
		this.queuingTimeQa = queuingTimeQa;
	}

	public double getTimeOfArrival() {
		return timeOfArrival;
	}

	public void setTimeOfArrival(double timeOfArrival) {
		this.timeOfArrival = timeOfArrival;
	}

	public double getTimeInSystem() {
		if (this.isInSystem){
			this.timeInSystem = getTime() - getTimeOfArrival();
		
			if (this.timeInSystem> 240){
				this.hasReachedtarget=true;
System.out.println(this.getId() + " has reached the target ");
//				printTime();
//				if(this.timeInSystem>600)
System.err.println("WARNING: REACHED TARGET "+ this.getId() + " is at : " +this.getLoc().toString()+ " time in system: "+ this.timeInSystem/60  + " hours" );
			}
		}
		
		return this.timeInSystem;
	}


	
	public void setTimeInSystem(double timeInSystem) {
		this.timeInSystem = timeInSystem;
	}

	public QueueSim getCurrentQueue() {
		return currentQueue;
	}

	public void setCurrentQueue(QueueSim currentQueue) {
		this.currentQueue = currentQueue;
	}

	public double getTimeEnteredCurrentQ() {
		return timeEnteredCurrentQ;
	}

	public void setTimeEnteredCurrentQ(double timeEnteredCurrentQ) {
		this.timeEnteredCurrentQ = timeEnteredCurrentQ;
	}

	public double getTimeOutCurrentQueue() {
		return timeOutCurrentQueue;
	}

	public void setTimeOutCurrentQueue(double timeOutCurrentQueue) {
		this.timeOutCurrentQueue = timeOutCurrentQueue;
	}

	public int getNextProc() {
		return nextProc;
	}

	public void setNextProc(int nextProc) {
		this.nextProc = nextProc;
	}

	public double getTimeEndCurrentService() {
		return timeEndCurrentService;
	}

	public void setTimeEndCurrentService(double timeEndCurrentService) {
		this.timeEndCurrentService = timeEndCurrentService;
	}

	public String getTypeArrival() {
		return typeArrival;
	}

	public void setTypeArrival(String typeArrival) {
		this.typeArrival = typeArrival;
	}

	public boolean isWasFirstInQr() {
		return wasFirstInQr;
	}

	public void setWasFirstInQr(boolean wasFirstInQr) {
		this.wasFirstInQr = wasFirstInQr;
	}

	public double getTestRatio() {
		return testRatio;
	}

	public void setTestRatio(double testRatio) {
		this.testRatio = testRatio;
	}

	public boolean isWasFirstInQueueTriage() {
		return wasFirstInQueueTriage;
	}

	public void setWasFirstInQueueTriage(boolean wasFirstInQueueTriage) {
		this.wasFirstInQueueTriage = wasFirstInQueueTriage;
	}

	public boolean isWasFirstInQueueXRay() {
		return wasFirstInQueueXRay;
	}

	public void setWasFirstInQueueXRay(boolean wasFirstInQueueXRay) {
		this.wasFirstInQueueXRay = wasFirstInQueueXRay;
	}

	public boolean isWasFirstInQueueTest() {
		return wasFirstInQueueTest;
	}

	public void setWasFirstInQueueTest(boolean wasFirstInQueueTest) {
		this.wasFirstInQueueTest = wasFirstInQueueTest;
	}

	public boolean isWasFirstforAsses() {
		return wasFirstforAsses;
	}

	public void setWasFirstforAsses(boolean wasFirstforAsses) {
		this.wasFirstforAsses = wasFirstforAsses;
	}

	public int getNumWasFstForRegist() {
		return numWasFstForRegist;
	}

	public void setNumWasFstForRegist(int numWasFstForRegist) {
		this.numWasFstForRegist = numWasFstForRegist;
	}

	public int getNumWasFstForTriage() {
		return numWasFstForTriage;
	}

	public void setNumWasFstForTriage(int numWasFstForTriage) {
		this.numWasFstForTriage = numWasFstForTriage;
	}

	public int getNumWasFstForTest() {
		return numWasFstForTest;
	}

	public void setNumWasFstForTest(int numWasFstForTest) {
		this.numWasFstForTest = numWasFstForTest;
	}

	public int getNumWasFstForXRay() {
		return numWasFstForXRay;
	}

	public void setNumWasFstForXRay(int numWasFstForXRay) {
		this.numWasFstForXRay = numWasFstForXRay;
	}

	public int getIsWaitingBedReassessment() {
		return isWaitingBedReassessment;
	}

	public void setIsWaitingBedReassessment(int isWaitingBedReassessment) {
		this.isWaitingBedReassessment = isWaitingBedReassessment;
	}

	public Resource getMyBedReassessment() {
		return myBedReassessment;
	}

	public void setMyBedReassessment(Resource myBedReassessment) {
		this.myBedReassessment = myBedReassessment;
	}

	public int getNumWasFstForAssess() {
		return numWasFstForAssess;
	}

	public void setNumWasFstForAssess(int numWasFstForAssess) {
		this.numWasFstForAssess = numWasFstForAssess;
	}

	public boolean isGoToResusRoom() {
		return goToResusRoom;
	}

	public void setGoToResusRoom(boolean goToResusRoom) {
		this.goToResusRoom = goToResusRoom;
	}

	public boolean isGoToTreatRoom() {
		return goToTreatRoom;
	}

	public void setGoToTreatRoom(boolean goToTreatRoom) {
		this.goToTreatRoom = goToTreatRoom;
	}

	public boolean isComingFromTest() {
		return needsTests;
	}
	
	public boolean getNeedsTests(){
		return this.needsTests;
	}

	public void setNeedsTests(boolean needsTests) {
		this.needsTests = needsTests;
	}

	public void increaseTestCounterXray() {
		this.testCountX++;
	}
	
	public void increaseTestCounterTest() {
		this.testCountT++;
	}

	public int getReassessmentDone() {
		return reassessmentDone;
	}

	public void setReassessmentDone(int reassessmentDone) {
		this.reassessmentDone = reassessmentDone;
	}

	public double gettRegist() {
		return tRegist;
	}

	public void settRegist(double tRegist) {
		this.tRegist = tRegist;
	}

	public double gettTriage() {
		return tTriage;
	}

	public void settTriage(double tTriage) {
		this.tTriage = tTriage;
	}

	public double gettFirstAssessment() {
		return tFirstAssessment;
	}

	public void settFirstAssessment(double tFirstAssessment) {
		this.tFirstAssessment = tFirstAssessment;
	}

	public double gettReasssesment() {
		return tReasssesment;
	}

	public void settReasssesment(double tReasssesment) {
		this.tReasssesment = tReasssesment;
	}

	public double gettXray() {
		return tXray;
	}

	public void settXray(double serviceTime) {
		this.tXray = serviceTime;
	}

	public double gettTest() {
		return tTest;
	}

	public void settTest(double serviceTime) {
		this.tTest = serviceTime;
	}

	public boolean isFromOtherDoctor() {
		return isFromOtherDoctor;
	}

	public void setFromOtherDoctor(boolean isFromOtherDoctor) {
		this.isFromOtherDoctor = isFromOtherDoctor;
	}

}
