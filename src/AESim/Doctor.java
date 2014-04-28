package AESim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import cern.jet.random.Uniform;
import Datos.Reader;
import Funciones.MathFunctions;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;

public abstract class Doctor extends Staff {
	
	protected static final LinkedList<Patient> patientsForReassessment = new LinkedList<Patient>();
	protected PriorityQueue<Patient> myPatientsInBed;
	protected LinkedList<Patient> myPatientsBackInBed;
	protected ArrayList<Patient> myPatientsInTests;
	protected ArrayList<Patient> allMyPatients;
	protected boolean isAtDoctorArea;
	protected double timeEnterSimulation;
	protected Patient myPatientCalling;
	protected Doctor doctorToHandOver;
	protected int sizeAllMyPatients;
	
	// PECS information and variables
	// INPUT variables
	protected double x1MyNumPatientsSeen;
	protected double x2MyTimeWorkedInShift;
	protected double x3TriageMaxAmongMyPatients;
	protected double x4MyPatientsAverageTimeInSys;
	protected double x5RatioTestMaxTestMyPatients;
	protected double x6MyTotalTimeWorkedInDpmnt; // TotalTimeWorkedInDpmnt is
												// measured in weeks
	protected double x7MyPatientsMaxTimeInSys;

	// PARAMETERS
	protected double c1MyMaxPatientHour;
	protected double c2MyDurationShift; // this is set when doctor moves to
										// doctors' area
	protected double c3LogisticCalmC;
	protected double c4LogisticKnowledgeC;// TODO this depends on other variables.
										// I'll calculate it later. c4= - c5 z4+
										// 2c5
	protected double c5LogisticExperienceC;
	protected double c6LogisticReputationC;
	
	protected double cpLogisticCalmC=3;		// cp represents the maximum time in system over all the 
										// patients NOT SEEN BY the doc at wich he losses the 50 % of his calm
	
	protected double cpLogisticReputationC=3;
	
	
	protected double alpha3Experience;
	protected double alpha1Calmness;
	protected double alpha2Knowledge;

	// STATE (PECS) Variables
	protected double z1Energy;
	protected double z2Calmness;
	protected double z3Knowledge;
	protected double[] z3KnowledgeMatrixPatient; // this is a matrix 1 row i cols
												// (number of "allMyPatients")
	// if I know the position i in the vector z3, i know the position i in the
	// array allmypatients
	// position [i]= knowledge about the patient i.

	protected double z4Experience;
	protected double z5Reputation;

	// Intensity state variables

	protected double alphaZ1W1;
	protected double alphaZ2W2;
	protected double alphaZ3W3;
	protected double alphaZ5W4;
	
	protected double beta1 = 0.7;
	protected double beta2 = 0.3;

	protected double cZ1W1;
	protected double cZ2W2;
	protected double cZ3W3;
	// I don't use the intensity of the experience to make decisions.
	protected double cZ5W4;

	protected double w1Fatigue;
	protected double w2Stress;
	protected double w3WillOfKnowledge;
	protected double[] w3WillOfKnowledgeMatrix;
	protected boolean startShift;

	protected double w3WillOfKnowledge1;
	protected double w3WillOfKnowledge2;
	protected double w3WillOfKnowledge3;
	protected double w3WillOfKnowledge4;
	protected double w3WillOfKnowledge5;
	protected double w3WillOfKnowledge6;
	protected double w4SocialDesire;
	protected double averageKnowledge;
	
	@Override
	public void requiredAtWork() {
		if (this.isInShift() == false) {
			this.startShift();
		} else {
			
		}
	}
	
	public void startShift(){
		this.setTimeInitShift(getTime());
		this.initializeDoctorShiftParams();
		this.doctorToHandOver= null;
		System.out.println(this.getId() + " will move to doctors area"
				+ " method: schedule work"
				+ " this method is being called by " + this.getId());
		this.numAvailable=this.multiTaskingFactor;
		this.setAvailable(true);
		if(this.isAtDoctorArea==false)
			this.moveToDoctorsArea();
		this.decideWhatToDoNext();
	}
	private void initializeDoctorShiftParams() {
		this.numAvailable = this.multiTaskingFactor;
		//Inicializar variables PECS
		System.out.println(this.getId()			+ " is initializing PECS at the beginning of his shift: ");
		printTime();
		this.setX1MyNumPatientsSeen(0);
		this.setX2MyTimeWorkedInShift(0);// this time
											// is in
											// hours
		this.setX3TriageMaxAmongMyPatients(1);
		this.setX4MyPatientsAverageTimeInSys(0);
		this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
													// calculated individually
													// to obtain a knowledge of
													// each patient
		this.setX6MyTotalTimeWorkedInDpmnt(0);
		this.setX7MyPatientsMaxTimeInSys(0);
		this.setZ1Energy(1);

		double alpha1 = this.getX3TriageMaxAmongMyPatients()
				* (1 - this.getZ1Energy());
		this.setZ2Calmness(MathFunctions.calcFLogisticPositive(0, alpha1,
				this.getC3LogisticCalmC()));
		this.setZ3Knowledge(MathFunctions.calcFLogisticNegative(0, 1,
				this.getC4LogisticKnowledgeC()));
		this.setZ4Experience(MathFunctions.calcFLogisticNegative(0,
				this.getAlpha3Experience(), this.getC5LogisticExperienceC()));
		double alpha4 = 1 + this.getZ4Experience();
		this.setZ5Reputation(MathFunctions.calcFLogisticPositive(0, alpha4,
				this.getC6LogisticReputationC()));

		this.setAlphaZ1W1(10);
		this.setcZ1W1(0.5);
		this.setAlphaZ2W2(10);
		this.setcZ2W2(0.5);
		this.setAlphaZ3W3(10);
		this.setcZ3W3(0.7);
		this.setAlphaZ5W4(5);
		this.setcZ5W4(0.5);
		
		
	}
	
	
	@ScheduledMethod(start = 5, interval = 10, priority = 60, shuffle = false, pick = 1)
	public void calcPECSvariables() {
		
if (this.isInShift()){

		// state variables calculation . I start with the Z's calculation
		// because they depend on the previous values of X's and Z's
	if (this.getId().equals("Consultant doctor 1")){
		System.out.println(this.getId());
	if ( this.getX1MyNumPatientsSeen()==0){
		System.out.println(this.getId() + " num patients seen " + this.getX1MyNumPatientsSeen() + " tick:" + getTime());
	}}
		this.setZ1Energy(MathFunctions.calcFCOverCplusX(this.getC1MyMaxPatientHour(),
				this.getC2MyDurationShift(), this.getX1MyNumPatientsSeen(),
				this.getX2MyTimeWorkedInShift()));
		
		System.out.println(" a ********** ******** "+" nuevo " +this.getC1MyMaxPatientHour());
		System.out.println(" b ********** ******** "+" nuevo "+this.getC2MyDurationShift());
		System.out.println(" c ********** ******** "+" nuevo "+this.getX1MyNumPatientsSeen());
		System.out.println(" d ********** ******** "+" nuevo "+this.getX2MyTimeWorkedInShift());
		
		double alpha1 = 0;
		
		double beta1 = 0.7;
		double beta2 = 0.3;
		
		if(this.getAllMyPatients().size()>0){
			alpha1 = this.getX3TriageMaxAmongMyPatients()
					* (1 - this.getZ1Energy());
			
		} else {
			alpha1 = (1 - this.getZ1Energy());
			beta1 = 0.5;
			beta2 = 0.5;
		}
							
		double avPatsNotSeenByThisDoc = (this.calculateAverageTimeAllPatients()[0]/60)*this.calculateAverageTimeAllPatients()[1]-this.getX4MyPatientsAverageTimeInSys()*this.getAllMyPatients().size();
		
		
		double x = beta1*this.getX4MyPatientsAverageTimeInSys()+beta2*avPatsNotSeenByThisDoc;
		double c = beta1*this.getC3LogisticCalmC()+beta2*this.getCpLogisticCalmC();
		
		this.setZ2Calmness(MathFunctions.calcFLogisticPositive(x, alpha1,c));
		
		double alpha2 = this.getX5RatioTestMaxTestMyPatients()
				+ this.getZ4Experience();

		this.setKnowledgeEachPatient();

		this.setZ3Knowledge(averageKnowledge);

		// this.setZ3Knowledge(calcFLogisticNegative(
		// this.getX4MyPatientsAverageTimeInSys(), alpha2,
		// this.getC4LogisticKnowledgeC()));

		this.setZ4Experience(MathFunctions.calcFLogisticNegative(
				this.getX6MyTotalTimeWorkedInDpmnt(),
				this.getAlpha3Experience(), this.getC5LogisticExperienceC()));
		double alpha4 = 1 + this.getZ4Experience();
		this.setZ5Reputation(MathFunctions.calcFLogisticPositive(
				this.getX7MyPatientsMaxTimeInSys(), alpha4,
				this.getC6LogisticReputationC()));
		double timeWorkedInWeeks = (getTime() - this.getTimeEnterSimulation())
				/ (60 * 24 * 7);

		this.setW1Fatigue(MathFunctions.calcFLogisticPositive(this.getZ1Energy(),this.getAlphaZ1W1()
				, this.getcZ1W1()));
		this.setW2Stress(MathFunctions.calcFLogisticPositive(this.getZ2Calmness(),
				this.getAlphaZ2W2(), this.getcZ2W2()));
		this.setW3WillOfKnowledge(MathFunctions.calcFLogisticPositive(this.getZ3Knowledge(),
				this.getAlphaZ3W3(), this.getcZ3W3()));
		this.setW4SocialDesire(MathFunctions.calcFLogisticPositive(this.getZ5Reputation(),
				this.getAlphaZ5W4(), this.getcZ5W4()));
		
//		if (this.isInShift()) {
			printTime();
			// this.setX1MyNumPatientsSeen(this.getAllMyPatients().size());
			this.setX2MyTimeWorkedInShift(this.calculateWorkedTimeHours());// this
																			// time
																			// is
																			// in
																			// hours
			this.setX3TriageMaxAmongMyPatients(this.getMaxTriage());
			this.setX4MyPatientsAverageTimeInSys(this
					.getAveTSysAllMyPatientsHours());
			this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
														// calculated
														// individually
														// to obtain a knowledge
														// of
														// each patient
			this.setX6MyTotalTimeWorkedInDpmnt(timeWorkedInWeeks);
			this.setX7MyPatientsMaxTimeInSys(this
					.getMaxTSysAllMyPatientsHours());

			System.out.println("\n	" + this.getId() + " is in shift?: "
					+ this.isInShift() + "\n		has worked in this shift "
					+ this.getX2MyTimeWorkedInShift() + " minutes "

					+ "\n		max triage among all patients "
					+ this.getMaxTriage()
					+ "\n		my patients average time in system "
					+ this.getAveTSysAllMyPatientsHours()
					+ "\n		my patients max time in system "
					+ this.getX7MyPatientsMaxTimeInSys()
					+ "\n		time worked in the department "
					+ this.getX6MyTotalTimeWorkedInDpmnt() + " weeks \n");
		}

		System.out.println("The fatigue of "+this.getId()+" is: "+this.getW1Fatigue());
		System.out.println("The energy of "+this.getId()+" is: "+this.getZ1Energy()+" The alphaZ1W1of "+this.getId()+" is: "+this.getAlphaZ1W1()+" The cZ1W1 of "+this.getId()+" is: "+this.getcZ1W1());
		this.setSizeAllMyPatients(this.getAllMyPatients().size());
//}
	}
	
public String marginalZ2 (){
		
		double alpha1 = 0;
		
		double beta1 = 0.7;
		double beta2 = 0.3;
		
		String winner = "";
		
		if(this.getAllMyPatients().size()>0){
			alpha1 = this.getX3TriageMaxAmongMyPatients()
					* (1 - this.getZ1Energy());
			
		} else {
			alpha1 = (1 - this.getZ1Energy());
			beta1 = 0.5;
			beta2 = 0.5;
		}
							
		double avPatsNotSeenByThisDoc = (this.calculateAverageTimeAllPatients()[0]/60)*this.calculateAverageTimeAllPatients()[1]-this.getX4MyPatientsAverageTimeInSys()*this.getAllMyPatients().size();
		
		
		double x = beta1*this.getX4MyPatientsAverageTimeInSys()+beta2*avPatsNotSeenByThisDoc;
		double c = beta1*this.getC3LogisticCalmC()+beta2*this.getCpLogisticCalmC();
		
		double z21 = MathFunctions.calcFLogisticPositive(beta1*this.getX4MyPatientsAverageTimeInSys(), alpha1,beta1*this.getC3LogisticCalmC());
		double z22 = MathFunctions.calcFLogisticPositive(beta2*avPatsNotSeenByThisDoc, alpha1,beta2*this.getCpLogisticCalmC());
		
		double z21Intensity = MathFunctions.calcFLogisticPositive(z21, this.getAlphaZ2W2(), this.getcZ2W2());
		double z22Intensity = MathFunctions.calcFLogisticPositive(z22, this.getAlphaZ2W2(), this.getcZ2W2());
		
		if(z21Intensity<z22Intensity){
			
			double z22Triage = MathFunctions.calcFLogisticPositive(2, alpha1,1);
			double z22TriageIntensity = MathFunctions.calcFLogisticPositive(z22Triage, this.getAlphaZ2W2(), this.getcZ2W2());
			
			double z22Time = MathFunctions.calcFLogisticPositive(beta2*avPatsNotSeenByThisDoc,1,beta2*this.getCpLogisticCalmC());
			double z22TimeIntensity = MathFunctions.calcFLogisticPositive(z22Time, this.getAlphaZ2W2(), this.getcZ2W2());
			
			if(z22TriageIntensity<z22TimeIntensity){
				winner = "Patients not seen by the doc, Time";
			} else {
				winner = "Patients not seen by the doc, Triage";
			}
			
		} else {
			
			double z21Triage = MathFunctions.calcFLogisticPositive(2, alpha1,1);
			double z21TriageIntensity = MathFunctions.calcFLogisticPositive(z21Triage, this.getAlphaZ2W2(), this.getcZ2W2());
			
			double z21Time = MathFunctions.calcFLogisticPositive(beta1*this.getX4MyPatientsAverageTimeInSys(),1,beta1*this.getC3LogisticCalmC());
			double z21TimeIntensity = MathFunctions.calcFLogisticPositive(z21Time, this.getAlphaZ2W2(), this.getcZ2W2());
			
			if(z21TriageIntensity<z21TimeIntensity){
				winner = "Patients seen by the doc, Time";
			} else {
				winner = "Patients seen by the doc, Triage";
			}
			
		}
		
		return winner;
		
	}
	
	public String marginalZ5 (){
		
		double alpha4 = this.getZ4Experience()+1;
		
		double beta1 = 0.7;
		double beta2 = 0.3;
		
		String winner = "";
		
							
		double avPatsNotSeenByThisDoc = (this.calculateAverageTimeAllPatients()[0]/60)*this.calculateAverageTimeAllPatients()[1]-this.getX4MyPatientsAverageTimeInSys()*this.getAllMyPatients().size();
		
		
		double x = beta1*this.getX4MyPatientsAverageTimeInSys()+beta2*avPatsNotSeenByThisDoc;
		double c = beta1*this.getC6LogisticReputationC()+beta2*this.getCpLogisticCalmC();
		
		
		double z21 = MathFunctions.calcFLogisticPositive(beta1*this.getX7MyPatientsMaxTimeInSys(), alpha4,beta1*this.getC6LogisticReputationC());
		double z22 = MathFunctions.calcFLogisticPositive(beta2*avPatsNotSeenByThisDoc, alpha4,beta2*this.getC6LogisticReputationC());
		
		double z21Intensity = MathFunctions.calcFLogisticPositive(z21, this.getAlphaZ5W4(), this.getcZ5W4());
		double z22Intensity = MathFunctions.calcFLogisticPositive(z22, this.getAlphaZ5W4(), this.getcZ5W4());
		
		if(z21Intensity<z22Intensity){
			winner = "Patients seen by the doc, Time";
		} else {
			winner = "Patients not seen by the doc, Time";
		}
		
		return winner;
		
	}

	
	
	public int getMaxTriage() {
		int maxTriage = 0;
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				if (patient.getTriageNum() > maxTriage) {
					maxTriage = patient.getTriageNum();
				}
			}
		}
		return maxTriage;
	}
	
	public double getAveTSysAllMyPatientsHours() {
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		int numberOfPatients = allMyPatientsV.size();
		double totalTimeInSys = 0;
		double aveTimeInSys = 0;
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				totalTimeInSys += (patient.getTimeInSystem()) / 60;

			}
			aveTimeInSys = totalTimeInSys / numberOfPatients;

		}
		return aveTimeInSys;
	}
	
	
	
	@Override
	protected void startBreak() {
		printTime();
		System.out.println(this.getId() + " is starting a break. Num available= " + this.getNumAvailable());
		this.scheduleEndBreak(this.durationOfRest);
		this.moveToDoctorsArea();
		
		
	}
	
	@Override
	protected void endBreak() {
		printTime();
		System.out.println(this.getId() + " is ending the break");
		this.setScheduledToStop(false);
		this.setNumAvailable(this.getMultiTaskingFactor());
		this.setAvailable(true);
		this.setX2MyTimeWorkedInShift(this.x2MyTimeWorkedInShift/2);
		this.decideWhatToDoNext();
	}

	public double getMaxTSysAllMyPatientsHours() {
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		int numberOfPatients = allMyPatientsV.size();
		double maxTime = 0;
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				if (patient.getTimeInSystem() > maxTime) {
					maxTime = (patient.getTimeInSystem()) / 60;
				}

			}
		}

		return maxTime;
	}

	
	public int calcMaxWPECS() {
		this.calcPECSvariables();
		double maxWPECS = this.getW1Fatigue();
		int decision = 1;
		if (maxWPECS < this.getW2Stress()) {
			maxWPECS = this.getW2Stress();
			decision = 2;
		}  
			
		if (maxWPECS < this.getW3WillOfKnowledge()) {
			maxWPECS = this.getW3WillOfKnowledge();
			decision = 3;
		}

		if (maxWPECS < this.getW4SocialDesire()) {
			maxWPECS = this.getW4SocialDesire();
			decision = 4;
		}
System.out.println(" decision is " + decision);
		return decision;
	}
	
	
	
	
	public double getRatioTestEachPatient(Patient patient) {

		int maxTest = 4;
		int totalTest = 0;
		totalTest = patient.getTotalNumTest();
		double ratio = totalTest / maxTest;
		patient.setTestRatio(ratio);

		return ratio;
	}

	public void getRatioTestAllMyPatients() {
		double ratio = 0;
		int maxTest = 4;
		int totalTest = 0;
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				totalTest = patient.getTotalNumTest();
				ratio = totalTest / maxTest;
				patient.setTestRatio(ratio);

			}
		}

	}
	
	public void setKnowledgeEachPatient() {
		double totalKnowlegde = 0;
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		int myTotalPatients = allMyPatientsV.size();
		if (myTotalPatients > 0) {
			z3KnowledgeMatrixPatient = new double[myTotalPatients];
			w3WillOfKnowledgeMatrix = new double[myTotalPatients];
			for (int i = 0; i < myTotalPatients; i++) {
				Patient patient = allMyPatientsV.get(i);
				this.z3KnowledgeMatrixPatient[i] = getKnowledgePatient(patient);
				totalKnowlegde += getKnowledgePatient(patient);
				double intensityKnow = (MathFunctions.calcFLogisticPositive(
						this.getZ3KnowledgeMatrixPatient()[i],
						this.getAlphaZ3W3(), this.getcZ3W3()));
				this.w3WillOfKnowledgeMatrix[i] = intensityKnow;
				switch (i) {
				case 0:
					this.setW3WillOfKnowledge1(w3WillOfKnowledgeMatrix[i]);
					break;
				case 1:
					this.setW3WillOfKnowledge2(w3WillOfKnowledgeMatrix[i]);
					break;
				case 2:
					this.setW3WillOfKnowledge3(w3WillOfKnowledgeMatrix[i]);
					break;
				case 3:
					this.setW3WillOfKnowledge4(w3WillOfKnowledgeMatrix[i]);
					break;
				case 4:
					this.setW3WillOfKnowledge5(w3WillOfKnowledgeMatrix[i]);
					break;

				case 5:
					this.setW3WillOfKnowledge6(w3WillOfKnowledgeMatrix[i]);
					break;
				default:
					break;
				}
			}

			this.averageKnowledge = totalKnowlegde / myTotalPatients;
		}

	}
	
	public double getKnowledgePatient(Patient patient) {
		double alpha2 = this.getRatioTestEachPatient(patient)
				+ this.getZ4Experience();
		double knowledge = (MathFunctions.calcFLogisticNegative(patient.getTimeInSystem(),
				alpha2, this.getC4LogisticKnowledgeC()));

		return knowledge;
	}

	@Override
	public void decideWhatToDoNext() {
//XXX BORRÉ UN MÉTODO
		//cada vez que un doctor puede busca una cama para lso pacienets que esperan por bed reassessment 
		movePatientBedReassessment(this);
		
		
		this.requiredAtWork = (int) this.getMyShiftMatrix()[getHour()][getDay()];
		if (this.requiredAtWork == 0) {
			boolean any = false;
			if (this.patientsInMultitask.size() > 0){
				this.scheduleEndShift(this.calculateMaxEndingTime()+5);
				any = true;
			}
			if (this.myPatientsInTests.size()>0){
				Doctor doctor = this.doctorToTakeOver();
				if (doctor == null){
					throw new NullPointerException();
				}
				any = true;
			}
			if (!any){
				this.moveOut();
			}
		}
		else {
			if (this.patientsInMultitask.size() < this.multiTaskingFactor){
				int decision= this.calcMaxWPECS();
				//XXX PARAR POR FATIGA
				
				if (decision==1){
					this.rest();
				
				}
				else {
				
//				if (!this.checkIfStartReassessment()){
//					if (!this.checkIfStartInitAssessment()) {
//						this.moveToDoctorsArea();
						this.decideWhatToDo();
//					}
//				}
			}
			}
		}
	}
	
	
	
	
	public void rest() {
		double maxEndingTime= this.calculateMaxEndingTime(); 
		if (maxEndingTime<getTime()){
			maxEndingTime= getTime();
		}
		double timeEndingShift=  this.timeInitShift+ (double) this.durationOfShift[getWeek()][getHour()];
		
		if (timeEndingShift> (maxEndingTime+durationOfRest)){
			scheduleStartBreak(maxEndingTime);
			
		}
		
		else {
			this.decideWhatToDo();
		}
		
	}

	protected boolean checkIfStartInitAssessment() {
		boolean checked = false;
		Patient patient = null;
		Boolean flag = false;
		// The head of the queue is at (x,y-1)
		// Object o = grid.getObjectAt(locX, locY);
		int i = 1;
		while (i <= 5 && !flag) {
			// checking from left to right, which patient is at the head of
			// assessment (by each triage color) queue
			for (Object o : grid.getObjectsAt(i, 8)) {
				if (o instanceof Patient) {
					patient = (Patient) o;
					flag = true;
					break;
				}

			}
			i++;
			// checks if there is anyone to start the first assessment
		}

		if (patient != null) {
			System.out.println(this.getId()
					+ " decide to start first assessment with:"
					+ patient.getId());			
			this.checkConditionsForFirstAssessment(patient);
			checked = true;

		} else {
//			System.out.println(this.getId() + "");
//			if (this.allMyPatients != null){
//				printElementsArray(allMyPatients, " All my patients, in check if start init assessment patients:");
//			}			
			if (!this.isAtDoctorArea){
				System.out.println(this.getId() + " is available and...decides what to do, moving to docs area when start Init assessment");
				this.moveToDoctorsArea();}
		}
		return checked;
	}
	
	protected boolean checkIfStartReassessment() {
		boolean check = false;
		if (this.patientsInMultitask.size() < this.multiTaskingFactor) {
			int doctorPatientsNum = this.getMyPatientsBackInBed().size();
			if (doctorPatientsNum > 0) {
				Patient patient = this.getMyPatientsBackInBed().peek();
				this.checkConditionsForReassessment(patient);
				check = true;
			} 
			
			else if (this.getClass() == Sho.class){
				System.out.println("Searhing other patients for Reassessment ...");
				Patient patient = null;
				boolean flag = false;
				for (int i = 1; i <= 5 && !flag; i+=2){
					for (int j = 10; j <= 14 && !flag; j++){
						for (Object o : grid.getObjectsAt(i, j)) {
							if (o instanceof Patient) {
								patient = (Patient) o;
								if(patient.isBackInBed()){
									if (patient.getTimeEndCurrentService()<getTime()){
									flag = true;
									break;
								}
								}
							}
						}
					}
				}
				if (flag && patient != null){
					System.out.println(this.getId() + " is going to reassess "  + patient.getId() + " Hay que avisarle al otro medico..........");
					this.notifyDoctor(patient);
					this.checkConditionsForReassessment(patient);
					check = true;
				}				
			}
		}
		return check;
	}



	protected void notifyDoctor(Patient patient) {
		patient.setFromOtherDoctor(true);
		Doctor otherDoctor = patient.getMyDoctor();
		//aquí remuevo patients in bed despue´s de back in bed
		otherDoctor.myPatientsBackInBed.remove(patient);
		otherDoctor.releaseFromPatient(patient);
		otherDoctor.myPatientsInBed.remove(patient);
		System.out.println(otherDoctor.getId() + " has removed from bed: " + patient.getId());

		if (!this.getMyPatientsBackInBed().contains(patient)){
			this.myPatientsBackInBed.add(patient);}
						
		patient.setMyDoctor(this);
		if (!this.getMyPatientsInBed().contains(patient)){
			this.myPatientsInBed.add(patient);}
		
		// XXX fecha 25 marzo 2014 (agregué engage with patient aquí)
	//	this.engageWithPatient(patient);
		System.out.println(this.getId() + " notified " + otherDoctor.getId() + " that he get the patient " + patient.getId());
		System.out.println("and added the patient to his PatientsBackInBed");
	}

	public abstract void decideWhatToDo();
	
	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstforAsses", triggerCondition = "$watchee.getNumWasFstForAssess()>0 && $watcher.getNumAvailable()>0", scheduleTriggerPriority = 90, whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void checkConditionsForFirstAssessment(Patient patient){
		patient.setNumWasFstForAssess(0);
		printTime();
		System.out.println("Cheking conditions for first assessment ");
		Resource bed = findBed(patient.getTriageNum());
		Nurse nurse = findNurse();
		
		boolean isRedPatient = false;
		boolean isAConsultant = false;
		if (Doctor.patientsRedTriage.contains(patient)){
			isRedPatient = true;
System.out.println(patient.getId() + " is a RED patient");
			if (this.getClass() == Consultant.class){
				isAConsultant = true;
System.out.println("The consultant is in charge of RED patient.");
			} else {
System.out.println("I´m not a consultant and I can´t start FstAssessmente with " + patient.getId());
			}
		}
		
		boolean conditionsOk = true;
		if (bed == null){
System.out.println("There is no bed");
			conditionsOk = false;
		} else if (nurse == null) {
System.out.println("There is no nurse");
			//FIXME aqui las enferemeras se usan y se daña todo
			conditionsOk = true;
		}
		if (conditionsOk && !isRedPatient){
System.out.println("Conditions are ok to start FirstAssessment");
			if (this instanceof Consultant) {
				Doctor sho = ((Consultant) this).checkForAnyAvailableDoctor();
				if (sho != null){
					sho.startFirstAssessment(patient, nurse, bed);
				}
				else {
					this.startFirstAssessment(patient, nurse, bed);
				}
			}
			
			else {
				this.startFirstAssessment(patient, nurse, bed);
			}
		} 
		else if (isRedPatient && isAConsultant && conditionsOk){
System.out.println("Conditions are ok to start FirstAssement with a RED PATIENT");
			Doctor.patientsRedTriage.remove(patient);
			this.startFirstAssessment(patient, nurse, bed);
		}
	}
	
	protected void startFirstAssessment(Patient patient, Nurse nurse, Resource bed) {
		printTime();
		System.out.println("\n\nSTART FIRST ASSESSMENT");
		System.out.println("START INIT ASSESSMENT " + this.getId() + " " + patient.getId() );
		printTime();
		GridPoint loc = bed.getLoc();
		int locX = loc.getX();
		int locY = loc.getY();
		grid.moveTo(this, locX, locY);
		if (nurse != null){
			grid.moveTo(nurse, locX, locY);
			patient.setMyNurse(nurse);
			nurse.engageWithPatient(patient);
		}
		
		grid.moveTo(patient, locX, locY);
		System.out.println(this.getId() + " & "
				+ patient.getId() + " have moved to "
				+ this.getLoc().toString() + " to "
				+ bed.getId());
		patient.setMyResource(bed);
		bed.setAvailable(false);
		
		patient.setMyDoctor(this);
		QueueSim queue = patient.getCurrentQueue();
		queue.removeFromQueue(patient);
		queue.elementsInQueue();
		if (!this.getMyPatientsInBed().contains(patient)){
			this.myPatientsInBed.add(patient);}
				
		this.engageWithPatient(patient);
		
		//XXX 28/02/2014: Copiamos X1MyNumPatients del archivo viejo
		double x = this.getX1MyNumPatientsSeen();
		x++;
		this.setX1MyNumPatientsSeen(x);
		
		printElementsQueue(this.myPatientsInBed,
				" my patients in bed time");
		printElementsArray(this.myPatientsInTests, "My patients in test");
		this.scheduleEndFirstAssessment(patient);
		printTime();
		if (this.available){
			this.decideWhatToDoNext();
		}
		System.out
				.println(this.getId()
						+ " schedules the end of first assessment and checks if there are more patients waiting at "
						+ queue.getId());

		if (queue.getSize() > 0) {
System.out
					.println("there are patients waiting for first assessment at:"
							+ queue.getId());
			Patient patientToMove = queue.firstInQueue();
System.out.println(patientToMove.getId()
					+ " will move to the head of " + queue.getId()
					+ " at:" + getTime());
			patientToMove.moveToHeadOfQ(queue);
		}
	}

	private void scheduleEndFirstAssessment(Patient patient) {
		System.out.println(this.getId()
				+ " is schedulling the end of first assessment of "
				+ patient.getId() );
		double parameters[] = this.firstAssessmentParameters(patient.getTriageNum());
		double serviceTime = 0;
		if ((patient.getTriage() == "Blue ")
				|| (patient.getTriage() == "Green ")) {
			serviceTime = MathFunctions.distTriangular(parameters[0], parameters[1],
					parameters[2]);
			// this.iniAssessmentTSampleTriang.add(serviceTime);
			// System.out.println(" init assessment sample triang: " +
			// this.iniAssessmentTSampleTriang);
		} else {
			serviceTime = MathFunctions.distLognormal(parameters[0], parameters[1],
					parameters[2]);
			// this.iniAssessmentTSampleLogn.add(serviceTime);
			// System.out.println(" init assessment sample logn: " +
			// this.iniAssessmentTSampleLogn);
		}

		// System.out.println("triangularOBS :   " + serviceTime);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		// double timeEndService = schedule.getTickCount()
		// + serviceTime;
		patient.settFirstAssessment(serviceTime);
		double timeEndService = schedule.getTickCount() + serviceTime;
		this.setNextEndingTime(timeEndService);
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndFirstAssessment action2 = new EndFirstAssessment(this, patient);
		patient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

		System.out.println("\n\tFirst assessment of " + patient.getId()
				+ " is schedulled to end at: " + timeEndService);

		
		System.out.println(this.getId() + " is available = "
				+ this.isAvailable() + " time " + getTime());

	}
	
	private static class EndFirstAssessment implements IAction {
		private Doctor doctor;
		private Patient patient;

		private EndFirstAssessment(Doctor doctor, Patient patient) {
			this.doctor = doctor;
			this.patient = patient;

		}

		@Override
		public void execute() {
			doctor.endFirstAssessment(this.patient);

		}

	}
	
	public void endFirstAssessment(Patient patient) {
		System.out.println("\n \t\t END FIRST ASSESSMENT");
		Doctor doctor = null;
		printTime();
//		doctor = chooseDocFirstAssess(patient, doctor);
		doctor=this;
		if (doctor != null) {
			int totalProcess = patient.getTotalProcesses();
			patient.setTotalProcesses(totalProcess + 1);
			// doctor decides what to do
			int route[] = decideTests(patient.getTriageNum());
			/*
			 * Choose a route. A patient could go to test or not. 30% of
			 * patients are removed from department.
			 */
			
			if (patient.isGoToTreatRoom()) {
				chooseRoute(patient, doctor, route);
			} else {
System.out.println(patient.getId() + " isn't going to TreatRoom and is leaving the department.");
				this.removePatientFromDepartment(patient);
			}
		} 
		else {
System.err.println(" ERROR: something is wrong here, no doctor to end fst assessment with "
							+ patient.getId());
//			 this.doEndFstAssessment(this, patient);

		}
		System.out.println(doctor.getId() + " is available = "
				+ doctor.isAvailable());
		// Para mover paciente de la lista de espera.
		//movePatientBedReassessment(doctor);
		System.out.println(doctor.getId()
				+ " is moving to doctors area at end first assessment");
		
			doctor.releaseFromPatient(patient);
			if (patient.getNeedsTest()){
				patient.setBackInBed(true);
		}		
		
//		if (!patient.isWaitInCublicle()){
			doctor.myPatientsInBed.remove(patient);
			System.out.println(doctor.getId() + " has removed from bed: " + patient.getId());
//		}
		

	}
	
	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "backInBed",triggerCondition = "$watchee.getReassessmentDone()<1 && $watcher.getNumAvailable()>0" /*&& $watcher.getStartedReassessment()<1"*/, whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, scheduleTriggerPriority = 60/*, pick = 1*/)
	public void checkConditionsForReassessment(Patient patient) {
		if (this.isAvailable()) {
			if (this.myPatientsBackInBed.contains(patient)) {
				System.out.println(this.getId() + " contains " + patient.getId() + " then: ");
				patient.setReassessmentDone(1);	
				System.out.println(patient.getId()+ " is set reassessment done in true (or 1) AND " + this.getId() + " will start reassessment with this patient" );
				this.startReassessment(patient);
				
			} else {
				this.decideWhatToDoNext();
			}	
		}
	
	}
	
	
	private void startReassessment(Patient patient) {
		printTime();
		this.myPatientsBackInBed.remove(patient);
		patient.setBackInBed(false);
		//FIXME Aqui peude estar lo de doble in bed! Mirar moveback to bed en patient
		if(!patient.isWaitInCublicle()){
			if (!this.getMyPatientsInBed().contains(patient)){
				this.myPatientsInBed.add(patient);}
			
			// XXX marzo 25 2014 agruegué engage with patient
			this.engageWithPatient(patient);
			
		}
		System.out.println(this.getId() + " is DOING reassessment to "
				+ patient.getId());
		printTime();
		System.out.println("       \n \nSTART RE-ASSESSMENT  " + this.getId()
				+ " and " + patient.getId());
		if (patient.getMyBedReassessment() == null || patient.getId() == null ){
			System.out.println("Que paso con mi camitaaa :(");
		}
		
		System.out.println(patient.getId() + " is at "
				+ patient.getMyBedReassessment().getId() + " loc "
				+ patient.getLoc());
	
		Resource bedPatient = patient.getMyBedReassessment();
		
		System.out.println(this.getId() + " will go to " + bedPatient.getId());
		GridPoint loc = patient.getLoc();
		this.moveTo(grid, loc);
		System.out.println(this.getId() + " moves to " + bedPatient.getId());
		this.setMyResource(bedPatient);
		patient.setMyResource(bedPatient);
		System.out.println(this.getId() + " and " + patient.getId()
				+ " have as resource:" + bedPatient.getId());
		System.out.println(this.getId() + " is setting " + bedPatient.getId()
				+ " available= false");
		bedPatient.setAvailable(false);
		if(patient.getNeedsTest() || patient.isFromOtherDoctor()){
			this.engageWithPatient(patient);	
		}
		System.out.println(this.getId() + " will schedule end reassessment");
		this.scheduleEndReassessment(this, patient);
		if (this.available)
			this.decideWhatToDoNext();				
	}

	/**
	 * @param doctor
	 * @param patient
	 */
	private void scheduleEndReassessment(Doctor doctor, Patient patient) {
		printTime();
		System.out.println(this.getId()
				+ " is scheduling the end of reassessment between: "
				+ doctor.getId() + " and " + patient.getId());

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();
		double serviceTime = getReassessmentTime(patient);
		patient.settReasssesment(serviceTime);
		double timeEndService = schedule.getTickCount()
				+ serviceTime;
		doctor.setNextEndingTime(timeEndService);
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		patient.setTimeEndCurrentService(timeEndService);
		EndReassessment action2 = new EndReassessment(doctor, patient);

		schedule.schedule(scheduleParams, action2);
		System.out.println(this.getId()
				+ " has started re-assessment and  has added "
				+ patient.getId() + " to his multitasking.  ");
		System.out.println("My multitasking factor is "
				+ this.multiTaskingFactor);
		printElementsArray(this.patientsInMultitask,
				" patients in multitasking");
		printElementsQueue(this.myPatientsInBed, "My patients in bed");
		System.out.println(this.getId() + " has available = "
				+ this.isAvailable());
		System.out.println(patient.getId()
				+ " expected to end reassessment at " + timeEndService);
		
	}
	
	private static class EndReassessment implements IAction {
		private Doctor doctor;
		private Patient patient;

		private EndReassessment(Doctor doctor, Patient patient) {
			this.doctor = doctor;
			this.patient = patient;

		}

		@Override
		public void execute() {
			doctor.endReassessment(this.patient);

		}

	}

	private double getReassessmentTime(Patient patient) {
		double time = 0;
		double parameters[] = this.reassessmentParameters(patient
				.getTriageNum());
		double min = parameters[0];
		double mean = parameters[1];
		double max = parameters[2];
		if (patient.getWasInTest() || patient.isWasInXray()) {
			// time=distExponential(min, mean, max);
			time = MathFunctions.exponential(mean).nextDouble();
			// this.reAssessmentTSampleExp.add(time);
			// System.out.println(" reassessment sample exponential: " +
			// reAssessmentTSampleExp);
		} else {
			time = MathFunctions.distLognormal(min, mean, max);
			// this.reAssessmentTSampleLogn.add(time);
			// System.out.println(" reassessment sample lognormal: " +
			// reAssessmentTSampleLogn);
		}

		return time;
	}

	public void endReassessment(Patient patient) {
		printTime();
//		TODO voy a mover el siguiente bloque al final
		
//		Doctor doctor = null;
//		if (this.isInShift() == false) {
//			doctor = this.doctorToTakeOver();
//		} else {
//			doctor = this;
//		}
		Doctor doctor = null;
		if (this.myPatientsInBed.contains(patient)){
			 doctor= this;
		}
		else if  (patient.getMyDoctor()!=null){
			 doctor= patient.getMyDoctor();
		}
		
		//está en guthub
		if (doctor!=null){
		if (doctor!= patient.getMyDoctor()){
System.out.println(doctor.getId() + " is ending reassessment but the doctor who has the patient is: " +patient.getMyDoctor() );
		}
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess + 1);		
		if (patient.getMyBedReassessment()==null){
System.out.println("Que pasoooo! Donde esta mi camita :(");
		}
		System.out
				.println("*****                               \n \n END RE-ASSESSMENT "
						+ doctor.getId()
						+ " and "
						+ patient.getId()

						+ " are at "
						+ patient.getMyBedReassessment().getId()
						+ " loc "
						+ patient.getLoc()
						+ "                  \n  ");

		Resource resourceToRelease = patient.getMyBedReassessment();
		System.out.println(" the bed to release is: "
				+ resourceToRelease.getId());
		doctor.setMyPatientCalling(null);

		int myPatientsInBed = doctor.getMyPatientsInBed().size();

		System.out.println(" method end reassessment ");
		if (doctor.myPatientsInBed.contains(patient)){
			doctor.myPatientsInBed.remove(patient);
		} else {
			Doctor doctorP= patient.getMyDoctor();
			if (doctorP.myPatientsInBed.contains(patient)){
				doctorP.myPatientsInBed.remove(patient);
			}
		}
		this.removePatientFromDepartment(patient);
//		movePatientBedReassessment(doctor);
		System.out.println(doctor.getId()
				+ " is moving to doctors area at end re-assessment");
		//		doctor.setStartedReassessment(0);
		
		
		}
		else {
			
					
			System.err.println(this.getId() + " por qué no tengo al paciente en my patients back in bed???" + patient.getId());
		}
		
//		Doctor doctor = null;
//		if (this.isInShift() == false) {
//			doctor = this.doctorToTakeOver();
//		} else {
//			doctor = this;
//		}
	}

	private void movePatientBedReassessment(Doctor doctor) {
		if (patientsWaitingForCubicle.size() > 0) {
			Patient patientWaiting = patientsWaitingForCubicle.get(0);
			Resource bed = doctor.findBed(patientWaiting.getTriageNum());
			if (bed != null) {
				patientsWaitingForCubicle.remove(patientWaiting);
				patientWaiting.setMyBedReassessment(bed);
				patientWaiting.getMyBedReassessment().setAvailable(false);
				patientWaiting.moveBackToBed(bed);
			}
		}
	}

	protected abstract double[] reassessmentParameters(int triageNum);

	protected void removePatientFromDepartment(Patient patient) {
		if (patient.getMyDoctor()!=null){
			if (patient.getMyDoctor().myPatientsInBed.contains(patient))
			patient.getMyDoctor().myPatientsInBed.remove(patient);
			System.out.println(patient.getMyDoctor().getId() + " has removed from bed: " + patient.getId());

			
			if (patient.getMyDoctor().myPatientsInTests.contains(patient))
			patient.getMyDoctor().myPatientsInTests.remove(patient);
			
			if (patient.getMyDoctor().allMyPatients.contains(patient))
			patient.getMyDoctor().allMyPatients.remove(patient);
			}
			
			if (patient.getMyDoctor().getMyPatientsBackInBed().contains(patient)){
				this.myPatientsBackInBed.remove(patient);
				}
			
			printTime();
		
System.out.println(patient.getId() + " IS LEAVING THE DEPARTMENT");
System.out.println("method remove patient from dep " + this.getId()
					+ " is setting " + patient.getMyResource().getId()
					+ " available= true");
System.out
					.println(patient.getId()
							+ " His time in system is:  "
							+ patient.getTimeInSystem());
			patient.getMyResource().setAvailable(true);
			patient.setMyResource(null);
			patient.setMyBedReassessment(null);
			if (patient.getMyNurse() != null){
				patient.getMyNurse().releaseFromPatient(patient);
			}			
			this.releaseFromPatient(patient);
			this.myPatientsInBed.remove(patient);
			patient.setMyDoctor(null);
			printTime();
System.out.println(patient.getId() + " goes to qTrolley");
			this.setMyResource(null);
			this.myPatientsInBed.remove(patient);
			printElementsQueue(myPatientsInBed,
					" my patients in bed ");
		
			patient.setInSystem(false);
			patient.getTimeInSystem();
			patient.addToQ("qTrolley ");
			try {
				Exit.addToFile(patient);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
System.out.println(patient.getId() + " has moved to trolley");
System.out.println(patient.getId() + " has left  the department");
System.out.println(patient.getId() + " is in system = " + patient.isInSystem());
			patient.getAllServicesTimes();
		
	}

	private void chooseRoute(Patient patient, Doctor doctor, int[] route) {
		if (route[0] == 0 && route[1] == 0) {
			patient.setMyBedReassessment(patient.getMyResource());
			System.out.println(patient.getId() + " starts reassessment immediately");
			printTime();
			patient.setNeedsTests(false);
			patient.setWaitInCublicle(true);
			System.out.println(doctor.getId() + " can do reassessment after first assessment? " + patient.getId() + " doctor patients in mt" + doctor.getPatientsInMultitask().size() + " patient will move back to bed");
			patient.moveBackToBed(patient.getMyResource());			
		}
		
		else {
			patientNeedsTests(patient, doctor, route);
		}
	}

	public void patientNeedsTests(Patient patient, Doctor doctor, int[] route) {
		System.out.println(patient.getId() + " needs tests ");
		patient.setNeedsTests(true);
		System.out.println(" method end First assessment"
				+ doctor.getId() + " will add to his patients in test "
				+ patient.getId()); 
		if (!doctor.getMyPatientsInTests().contains(patient)){
			doctor.myPatientsInTests.add(patient);}
		
		doctor.myPatientsInBed.remove(patient);
		printElementsArray(doctor.getMyPatientsInTests(),
				" my patients in test ");
		
				
		//10% de los pacientes no bloquean el cubiculo. Los rojos siempre la bloquean.
		//		TODO este es el valor real	Math.random() < 0.1
		if (Math.random() < 0.9 && !patient.getMyResource().getResourceType().substring(0, 4).equals("resus")) {
			Resource resourceToRelease = patient.getMyResource();	
			System.out.println(patient.getId() + " does not wait for tests in bed and is releasing: " + resourceToRelease.getId());

			System.out.println("method end fst assessment " + this.getId()
					+ " is setting " + resourceToRelease.getId()
					+ " available= true, because " + patient.getId()
					+ " does not wait for test in bed");
			resourceToRelease.setAvailable(true);
			patient.setWaitInCublicle(false);
			patient.setIsWaitingBedReassessment(1);
		} 
		else {
			System.out.println(patient.getId() + " waits in bed");
			Resource resourceToGo = patient.getMyResource();
			patient.setMyBedReassessment(resourceToGo);
			patient.setWaitInCublicle(true);
			patient.getMyBedReassessment().setAvailable(false);
			System.out.println(patient.getId() + " is keeping blocked as bed reassessment " + patient.getMyBedReassessment().getId());
		}

		printTime();
		System.out.println(patient.getId()
				+ " keeps in mind that his assigned doctor is  "
				+ patient.getMyDoctor().getId());

		doctor.setMyResource(null);

		System.out.println(doctor.getId() +
				 " method: endFstAssessment"
				+ " this method is being called by " + this.getId());
		
		
		dedideWhichTest(patient, doctor, route);
	}

	/**
	 * @param patient
	 * @param doctor
	 * @param route
	 */
	public void dedideWhichTest(Patient patient, Doctor doctor, int[] route) {
		System.out.println(doctor.getId() + " decides for patient's test");
		if (route[0] == 1) {
			printTime();

			System.out.println(patient.getId() + " needs Xray ");
			System.out.println(patient.getId() + " goes to qXray");
			patient.addToQ("qXRay ");
			// patient.increaseTestCounterXray();
			patient.setTotalNumTest(patient.getTotalNumTest() + 1);
			System.out.println(doctor.getId()
					+ " adds to list of patients in test "
					+ patient.getId());
			
			if (route[1] == 1) {
				printTime();
				patient.setNextProc(1);
				System.out.println(patient.getId()
						+ " will need test after Xray");
				// patient goes to test after xray
			} else {
				printTime();
				patient.setNextProc(2);
				System.out.println(patient.getId()
						+ " will go back to bed after Xray");
				// patient goes back to bed after xray
			}
		} else if (route[0] == 0 && route[1] == 1) {
			printTime();

			System.out.println(patient.getId()
					+ " needs test and didn't need xRay ");
			System.out.println(doctor.getId()
					+ " adds to list of patients in test "
					+ patient.getId());
			if (!doctor.getMyPatientsInTests().contains(patient)){
				doctor.myPatientsInTests.add(patient);}
			
			patient.addToQ("qTest ");
			// patient.increaseTestCounterTest();
			patient.setNextProc(2);
			patient.setTotalNumTest(patient.getTotalNumTest() + 1);
			System.out.println(" method end First assessment"
					+ doctor.getId()
					+ " has added to his patients in test "
					+ patient.getId());
			printElementsArray(doctor.getMyPatientsInTests(),
					" my patients in test ");

		}
	}

	private int[] decideTests(int triageNum) {
		Uniform unif = RandomHelper.createUniform();
		double rndXRay = unif.nextDouble();
		double rndTest = unif.nextDouble();
		int testRoute[] = { 0, 0 };
		if (rndXRay <= Reader.getMatrixPropTest()[triageNum - 1][0]) {
			testRoute[0] = 1;

		}
		if (rndTest <= Reader.getMatrixPropTest()[triageNum - 1][1]) {
			testRoute[1] = 1;

		}
		return testRoute;
	}

	private Doctor chooseDocFirstAssess(Patient patient, Doctor doctor) {
		if (patient == null) {
			System.err
			.println("\n ERROR: Shouldn't be happening, patient is null at end of fst assessment");
		} else {
			if (patient.getMyDoctor() == this && (this.isInShift())) {
				System.out
				.println(" the method end fst assessment is being called by "
						+ this.getId()
						+ " that is the same doctor the patien had in mind ");
				System.out
				.println(this.getId()
						+ " is in shift, then it is possible to start end fst assessment");
				doctor = this;
			} else {
				if (patient.getMyDoctor() != null)
					if (patient.getMyDoctor().isInShift()) {
						System.out
						.println(patient.getMyDoctor().getId()
								+ " is not in shift but is ending the fst assessment with "
								+ patient.getId());
						doctor = patient.getMyDoctor();
					} else {
						doctor = patient.getMyDoctor();
					}
			}
		}
		return doctor;
	}

	public abstract double[] firstAssessmentParameters(int triageNum);
	

	protected void canMoveOut(){
		
		
//		if(this.getAllMyPatients().size()>0){ voy a cambiar esto para que solo se vaya si tiene pacientes en test
		if ((this.getMyPatientsInBed().size())>0){
			System.err.println(this.getId() + " is trying to leave but still has patients in bed, why?");
			printElementsQueue(this.getMyPatientsInBed(), " Patients in bed when trying to leave: ");
		}
		if (this.getMyPatientsInTests().size()>0){
System.out.println(this.getId() + " STILL HAS PATIENTS TO SEE, CHECKING IF THERE ARE DOCTOR TO TAKE OVER");
			if (this.doctorToTakeOver() != null) {
System.out.println(this.getId() + " has found a doctor to hand over his patients ");
System.out.println(this.getId() + " IS MOVING OUT ");
				this.moveOut();
			}		
			else {
				System.err.println("no hay nadie que reciba mis pacientes");
			}
		} 
		
		else {
System.out.println(this.getId() + " has no patients left to see ");
System.out.println(this.getId() + " IS MOVING OUT ");
			this.moveOut();
		}
		
		
	}
	
	protected double calculateMaxEndingTime() {
		double maxEndingTime= 0; 
		for (Iterator<Patient> iterator = this.patientsInMultitask.iterator(); iterator
				.hasNext();) {
			Patient patient = (Patient) iterator.next();
			if(patient.getTimeEndCurrentService()>maxEndingTime){
				maxEndingTime= patient.getTimeEndCurrentService();
			}
		}
		return maxEndingTime;
	}
	
	@Override
	protected void canNotMoveOut(){
		double maxEndingTime= this.calculateMaxEndingTime(); 
		if (getTime() < maxEndingTime) {
			printTime();
System.out
					.println(this.getId()
							+ " has finished his shift but needs to wait to leave because he still has work to do");
			double timeEnding = maxEndingTime;
			this.scheduleEndShift(timeEnding+5);
			this.setAvailable(false);
		
			//this.setPatientsInMultitask(allMyPatients);
		}
	}
	
	@Override
	public void resetVariables() {
		if (this.allMyPatients.size() > 0) {
			printTime();
//			XXX aqui hay que mirar que el doc no se vaya con pacienets en la cama 
System.out
					.println(this.getId()
							+ " has finished his shift and still has patients, needs to hand over his patients  ");
			this.doctorToTakeOver();
	
		}			
		//TODO Resetear PECS
		this.setInShift(false);
		this.setAvailable(false);
		int i = this.getIdNum();
		int x = 19;
		int y = i + 4;
	
		grid.moveTo(this, x, y);
		this.isAtDoctorArea=false;
		//XXX 28/02/2014 verificar allMyPatients
		this.allMyPatients.clear();
		this.setX1MyNumPatientsSeen(0);
		this.setX2MyTimeWorkedInShift(0);// this time
											// is in
											// hours
		this.setX3TriageMaxAmongMyPatients(1);
		this.setX4MyPatientsAverageTimeInSys(0);
		this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
													// calculated individually
													// to obtain a knowledge of
													// each patient
		this.setX6MyTotalTimeWorkedInDpmnt(0);
		this.setX7MyPatientsMaxTimeInSys(0);
		
		//XXX 05/03/2014 las siguientes lineas las acabo de pegar porq no se estan reiniciando 
		this.setZ1Energy(1);
		this.setZ2Calmness(1);
		this.setZ3Knowledge(0);
		this.setZ4Experience(this.getZ4Experience());
		this.setZ5Reputation(this.getZ5Reputation());
		this.z3KnowledgeMatrixPatient = new double[0];
		this.w3WillOfKnowledgeMatrix = new double[0];
		
		this.setW1Fatigue(0);
		this.setW2Stress(0);
		this.setW3WillOfKnowledge(0);
		this.setW4SocialDesire(0);
		
		

		
	}
	
	private Doctor doctorToTakeOver() {
		Doctor doctor = null;
		context = getContext();
		boolean enterIf = false;
		for (Object d : context.getObjects(Doctor.class)) {
			if (d != null) {
				doctor = (Doctor) d;
				int hour = getHour();
				int day = getDay();
				// Circular list for hour
				int nextHour = hour + 1;
				int nextDay = day;
				if (nextHour > 23) {
					nextHour = 0;
					nextDay = day + 1;
					if (nextDay > 6) {
						nextDay = 0;
					}
				}
				if (doctor.getClass() != Consultant.class) {
					if ((doctor.getMyShiftMatrix()[hour][day] == 1)
							&& (doctor.getMyShiftMatrix()[nextHour][nextDay] == 1)) {
						enterIf = true;
						printTime();

//						TODO hay que verifcar que el dcotor no se vaya sin atender los pacientes en la cama 
						System.out.println("there is a doctor to take over: "
								+ doctor.getId());
						//XXX MARZO 30 2014 AQUÍ DEBE haber algo malo porque los pacientes no empiezan a atenderse rápido 
						this.handOver(doctor);
						doctor.decideWhatToDoNext();
						break;
					}

				} else
					doctor = null;
			}

		}

		if (doctor == null) {
System.err.println("there is no doctor to take over: "
					+ this.getId() + " is leaving");
System.out.println(this.getId()
					+ " search for somebody to stay at least when he leaves");
			for (Object d : context.getObjects(Doctor.class)) {
				if (d != null) {
					doctor = (Doctor) d;
					int hour = getHour();
					int day = getDay();
					// Circular list for hour
					int nextHour = hour + 1;
					int nextDay = day;
					if (nextHour > 23) {
						nextHour = 0;
						nextDay = day + 1;
						if (nextDay > 6) {
							nextDay = 0;
						}
					}
					if (doctor.getClass() != Consultant.class) {
						if ((doctor.getMyShiftMatrix()[hour][day] == 1)) {
							enterIf = true;
							printTime();
System.out
									.println("there is a doctor to take over: "
											+ doctor.getId());
							this.handOver(doctor);
							doctor.decideWhatToDoNext();
							break;
						}
					}
				}

			}

		}

		return doctor;
	}
	private void handOver(Doctor doctor) {
		//TODO marzo 30 yo creo que aquí debería devolver el doctor para preguntar si != nuill que pasó 
		printTime();
		System.out.println(this.getId() + " has started method hand over "
				+ doctor.getId());
		PriorityQueue<Patient> newMyPatientsInBed = this
				.getMyPatientsInBed();
		ArrayList<Patient> newMyPatientsInTests = this.getMyPatientsInTests();
		
		LinkedList<Patient> newMyPatientsBackInBed = this.getMyPatientsBackInBed();

		System.out.println(doctor.getId()
				+ " before take over the patients had in all his list: \n");
		doctor.printElementsQueue(doctor.myPatientsInBed,
				" patients in bed \n");
		doctor.printElementsArray(doctor.myPatientsInTests,
				" patients in test \n");
		try{
			
			if (this.myPatientsInTests.size() > 0){
				doctor.myPatientsInTests.addAll(newMyPatientsInTests);}
			
			
			if (this.myPatientsBackInBed.size() > 0) {
				doctor.myPatientsBackInBed.addAll(newMyPatientsBackInBed);
		}
			
			if (this.myPatientsInBed.size() > 0){
				doctor.myPatientsInBed.addAll(newMyPatientsInBed);
			}
			
			
		}
		
		catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}

		for (int i = 0; i < this.getAllMyPatients().size(); i++) {
			Patient patient = this.getAllMyPatients().get(i);
			patient.setMyDoctor(doctor);
			printTime();
System.out.println("/n " + patient.getId() + " has a new doctor: "
					+ patient.getMyDoctor().getId());
		}		
		
		double x = doctor.getX1MyNumPatientsSeen();
		x = x + (double) this.getAllMyPatients().size();
		doctor.setX1MyNumPatientsSeen(x);
		
		
		this.myPatientsInBed.clear();
		this.myPatientsBackInBed.clear();
		this.myPatientsInTests.clear();
		this.allMyPatients.clear();
		this.doctorToHandOver = doctor;
		System.out.println(doctor.getId()
				+ " receiving after take over at time:  " + getTime());
		System.out.println(doctor.getId() + " has received in his list: \n");
		doctor.printElementsQueue(doctor.myPatientsInBed,
				" patients in bed \n");
		doctor.printElementsArray(doctor.myPatientsInTests,
				" patients in test \n");
		System.out.println(this.getId()
				+ " is leaving, after hand over at time:  " + getTime());
		System.out.println(this.getId() + " should have an empty list: \n");
		doctor.printElementsQueue(this.myPatientsInBed,
				" patients in bed \n");
		doctor.printElementsArray(this.myPatientsInTests,
				" patients in test \n");
		System.out.println(this.getId() + " handled to " + doctor.getId()
				+ " time " + getTime());
	
	}
	
	public void moveToDoctorsArea() {
	
		if (this.getClass() == Sho.class) {
			boolean flag = false;
			int y = 5;
			int x;
			for (int j = 0; j < 2; j++) {
				for (int i = 1; i < 6; i++) {
					Object o = grid.getObjectAt(i + 6, y + j);
					if (o == null) {
						x = i + 6;
						grid.moveTo(this, x, 5);
						this.isAtDoctorArea = true;
						System.out.println(this.getId()
								+ " has moved to doctors area "
								+ this.getLoc().toString() + " at time "
								+ getTime());
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
	
		} else if (this.getClass() == Consultant.class) {
	
			grid.moveTo(this, this.getInitPosX(), this.initPosY);
	
System.out.println(this.getId() + " has moved to consultant area "
					+ this.getLoc().toString());
		}
	if (!this.isScheduledToStop){
		this.setInShift(true);
		this.setAvailable(true);
		System.out.println(this.getId() + " is in shift and is available at "+ getTime());
	}
	}
	
	// implements comparator
	class PriorityQueueComparatorTriage implements Comparator<Patient> {
	
		@Override
		public int compare(Patient p1, Patient p2) {
	
			if (p1.getTriageNum() > p2.getTriageNum()) {
				return -1;
			}
			if (p1.getTriageNum() < p2.getTriageNum()) {
				return 1;
			} else {
				if (p1.getTimeInSystem() > p2.getTimeInSystem()) {
					return -1;
				}
				if (p1.getTimeInSystem() < p2.getTimeInSystem()) {
					return 1;
				} else
					return 0;
			}
	
		}
	}
	
	class PriorityQueueComparatorTime implements Comparator<Patient> {
	
		@Override
		public int compare(Patient p1, Patient p2) {
			if (p1.getTimeInSystem() > p2.getTimeInSystem()) {
				return -1;
			}
			if (p1.getTimeInSystem() < p2.getTimeInSystem()) {
				return 1;
			}
			return 0;
		}
	
	}
	
	public PriorityQueue<Patient> getMyPatientsInBed() {
		return myPatientsInBed;
	}
	
	public void setMyPatientsInBed(PriorityQueue<Patient> myPatientsInBed) {
		this.myPatientsInBed = myPatientsInBed;
	}
	
	public LinkedList<Patient> getMyPatientsBackInBed() {
		return myPatientsBackInBed;
	}
	
	public void setMyPatientsBackInBed(LinkedList<Patient> myPatientsBackInBed) {
		this.myPatientsBackInBed = myPatientsBackInBed;
	}
	
	public ArrayList<Patient> getMyPatientsInTests() {
		return myPatientsInTests;
	}
	
	public void setMyPatientsInTests(ArrayList<Patient> myPatientsInTests) {
		this.myPatientsInTests = myPatientsInTests;
	}
	
	public ArrayList<Patient> getAllMyPatients() {
		PriorityQueue<Patient> myPatientsInBed = this.getMyPatientsInBed();
		ArrayList<Patient> myPatientsInTest = this.getMyPatientsInTests();

		this.allMyPatients = new ArrayList<Patient>();
		if (myPatientsInTest.size()>2 || myPatientsInBed.size() > 2){
			if (!(this instanceof Consultant)){
				
				
				this.getSizeAllMyPatients();
			}
		}
		this.allMyPatients.addAll(myPatientsInTest);
		this.allMyPatients.addAll(myPatientsInBed);
		return allMyPatients;
	}
	
	public void setAllMyPatients(ArrayList<Patient> allMyPatients) {
		this.allMyPatients = allMyPatients;
	}
	
	public boolean isAtDoctorArea() {
		return isAtDoctorArea;
	}
	
	public void setAtDoctorArea(boolean isAtDoctorArea) {
		this.isAtDoctorArea = isAtDoctorArea;
	}
	
	public double getTimeEnterSimulation() {
		return timeEnterSimulation;
	}
	
	public void setTimeEnterSimulation(double timeEnterSimulation) {
		this.timeEnterSimulation = timeEnterSimulation;
	}
	
	public Patient getMyPatientCalling() {
		return myPatientCalling;
	}
	
	public void setMyPatientCalling(Patient myPatientCalling) {
		this.myPatientCalling = myPatientCalling;
	}
	
	public Doctor getDoctorToHandOver() {
		return doctorToHandOver;
	}
	
	public void setDoctorToHandOver(Doctor doctorToHandOver) {
		this.doctorToHandOver = doctorToHandOver;
	}

	public double getX1MyNumPatientsSeen() {
		return x1MyNumPatientsSeen;
	}

	public void setX1MyNumPatientsSeen(double x1MyNumPatientsSeen) {
		this.x1MyNumPatientsSeen = x1MyNumPatientsSeen;
	}

	public double getX2MyTimeWorkedInShift() {
		return x2MyTimeWorkedInShift;
	}

	public void setX2MyTimeWorkedInShift(double x2MyTimeWorkedInShift) {
		this.x2MyTimeWorkedInShift = x2MyTimeWorkedInShift;
	}

	public double getX3TriageMaxAmongMyPatients() {
		return x3TriageMaxAmongMyPatients;
	}

	public void setX3TriageMaxAmongMyPatients(double x3TriageMaxAmongMyPatients) {
		this.x3TriageMaxAmongMyPatients = x3TriageMaxAmongMyPatients;
	}

	public double getX4MyPatientsAverageTimeInSys() {
		return x4MyPatientsAverageTimeInSys;
	}

	public void setX4MyPatientsAverageTimeInSys(double x4MyPatientsAverageTimeInSys) {
		this.x4MyPatientsAverageTimeInSys = x4MyPatientsAverageTimeInSys;
	}

	public double getX5RatioTestMaxTestMyPatients() {
		return x5RatioTestMaxTestMyPatients;
	}

	public void setX5RatioTestMaxTestMyPatients(double x5RatioTestMaxTestMyPatients) {
		this.x5RatioTestMaxTestMyPatients = x5RatioTestMaxTestMyPatients;
	}

	public double getX6MyTotalTimeWorkedInDpmnt() {
		return x6MyTotalTimeWorkedInDpmnt;
	}

	public void setX6MyTotalTimeWorkedInDpmnt(double x6MyTotalTimeWorkedInDpmnt) {
		this.x6MyTotalTimeWorkedInDpmnt = x6MyTotalTimeWorkedInDpmnt;
	}

	public double getX7MyPatientsMaxTimeInSys() {
		return x7MyPatientsMaxTimeInSys;
	}

	public void setX7MyPatientsMaxTimeInSys(double x7MyPatientsMaxTimeInSys) {
		this.x7MyPatientsMaxTimeInSys = x7MyPatientsMaxTimeInSys;
	}

	public double getC1MyMaxPatientHour() {
		return this.c1MyMaxPatientHour;
	}

	public void setC1MyMaxPatientHour(double c1MyMaxPatientHour) {
		this.c1MyMaxPatientHour = c1MyMaxPatientHour;
	}

	public double getC2MyDurationShift() {
		this.c2MyDurationShift = this.getDurationOfShift(getDay(),getHour());
		return c2MyDurationShift;
	}

	public void setC2MyDurationShift(double c2MyDurationShift) {
		this.c2MyDurationShift = c2MyDurationShift;
	}

	public double getC3LogisticCalmC() {
		return c3LogisticCalmC;
	}

	public void setC3LogisticCalmC(double c3LogisticCalmC) {
		this.c3LogisticCalmC = c3LogisticCalmC;
	}

	public double getC4LogisticKnowledgeC() {
		return c4LogisticKnowledgeC;
	}

	public void setC4LogisticKnowledgeC(double c4LogisticKnowledgeC) {
		this.c4LogisticKnowledgeC = c4LogisticKnowledgeC;
	}

	public double getC5LogisticExperienceC() {
		return c5LogisticExperienceC;
	}

	public void setC5LogisticExperienceC(double c5LogisticExperienceC) {
		this.c5LogisticExperienceC = c5LogisticExperienceC;
	}

	public double getC6LogisticReputationC() {
		return c6LogisticReputationC;
	}

	public void setC6LogisticReputationC(double c6LogisticReputationC) {
		this.c6LogisticReputationC = c6LogisticReputationC;
	}

	public double getAlpha3Experience() {
		return alpha3Experience;
	}

	public void setAlpha3Experience(double alpha3Experience) {
		this.alpha3Experience = alpha3Experience;
	}

	public double getAlpha1Calmness() {
		return alpha1Calmness;
	}

	public void setAlpha1Calmness(double alpha1Calmness) {
		this.alpha1Calmness = alpha1Calmness;
	}

	public double getAlpha2Knowledge() {
		return alpha2Knowledge;
	}

	public void setAlpha2Knowledge(double alpha2Knowledge) {
		this.alpha2Knowledge = alpha2Knowledge;
	}

	public double getZ1Energy() {
		return z1Energy;
	}

	public void setZ1Energy(double z1Energy) {
		this.z1Energy = z1Energy;
	}

	public double getZ2Calmness() {
		return z2Calmness;
	}

	public void setZ2Calmness(double z2Calmness) {
		this.z2Calmness = z2Calmness;
	}

	public double getZ3Knowledge() {
		return z3Knowledge;
	}

	public void setZ3Knowledge(double z3Knowledge) {
		this.z3Knowledge = z3Knowledge;
	}

	public double[] getZ3KnowledgeMatrixPatient() {
		return z3KnowledgeMatrixPatient;
	}

	public void setZ3KnowledgeMatrixPatient(double[] z3KnowledgeMatrixPatient) {
		this.z3KnowledgeMatrixPatient = z3KnowledgeMatrixPatient;
	}

	public double getZ4Experience() {
		return z4Experience;
	}

	public void setZ4Experience(double z4Experience) {
		this.z4Experience = z4Experience;
	}

	public double getZ5Reputation() {
		return z5Reputation;
	}

	public void setZ5Reputation(double z5Reputation) {
		this.z5Reputation = z5Reputation;
	}

	public double getAlphaZ1W1() {
		return alphaZ1W1;
	}

	public void setAlphaZ1W1(double alphaZ1W1) {
		this.alphaZ1W1 = alphaZ1W1;
	}

	public double getAlphaZ2W2() {
		return alphaZ2W2;
	}

	public void setAlphaZ2W2(double alphaZ2W2) {
		this.alphaZ2W2 = alphaZ2W2;
	}

	public double getAlphaZ3W3() {
		return alphaZ3W3;
	}

	public void setAlphaZ3W3(double alphaZ3W3) {
		this.alphaZ3W3 = alphaZ3W3;
	}

	public double getAlphaZ5W4() {
		return alphaZ5W4;
	}

	public void setAlphaZ5W4(double alphaZ5W4) {
		this.alphaZ5W4 = alphaZ5W4;
	}

	public double getcZ1W1() {
		return cZ1W1;
	}

	public void setcZ1W1(double cZ1W1) {
		this.cZ1W1 = cZ1W1;
	}

	public double getcZ2W2() {
		return cZ2W2;
	}

	public void setcZ2W2(double cZ2W2) {
		this.cZ2W2 = cZ2W2;
	}

	public double getcZ3W3() {
		return cZ3W3;
	}

	public void setcZ3W3(double cZ3W3) {
		this.cZ3W3 = cZ3W3;
	}

	public double getcZ5W4() {
		return cZ5W4;
	}

	public void setcZ5W4(double cZ5W4) {
		this.cZ5W4 = cZ5W4;
	}

	public double getW1Fatigue() {
		return w1Fatigue;
	}

	public void setW1Fatigue(double w1Fatigue) {
		this.w1Fatigue = w1Fatigue;
	}

	public double getW2Stress() {
		return w2Stress;
	}

	public void setW2Stress(double w2Stress) {
		this.w2Stress = w2Stress;
	}

	public double getW3WillOfKnowledge() {
		return w3WillOfKnowledge;
	}

	public void setW3WillOfKnowledge(double w3WillOfKnowledge) {
		this.w3WillOfKnowledge = w3WillOfKnowledge;
	}

	public double[] getW3WillOfKnowledgeMatrix() {
		return w3WillOfKnowledgeMatrix;
	}

	public void setW3WillOfKnowledgeMatrix(double[] w3WillOfKnowledgeMatrix) {
		this.w3WillOfKnowledgeMatrix = w3WillOfKnowledgeMatrix;
	}

	public boolean isStartShift() {
		return startShift;
	}

	public void setStartShift(boolean startShift) {
		this.startShift = startShift;
	}

	public double getW3WillOfKnowledge1() {
		return w3WillOfKnowledge1;
	}

	public void setW3WillOfKnowledge1(double w3WillOfKnowledge1) {
		this.w3WillOfKnowledge1 = w3WillOfKnowledge1;
	}

	public double getW3WillOfKnowledge2() {
		return w3WillOfKnowledge2;
	}

	public void setW3WillOfKnowledge2(double w3WillOfKnowledge2) {
		this.w3WillOfKnowledge2 = w3WillOfKnowledge2;
	}

	public double getW3WillOfKnowledge3() {
		return w3WillOfKnowledge3;
	}

	public void setW3WillOfKnowledge3(double w3WillOfKnowledge3) {
		this.w3WillOfKnowledge3 = w3WillOfKnowledge3;
	}

	public double getW3WillOfKnowledge4() {
		return w3WillOfKnowledge4;
	}

	public void setW3WillOfKnowledge4(double w3WillOfKnowledge4) {
		this.w3WillOfKnowledge4 = w3WillOfKnowledge4;
	}

	public double getW3WillOfKnowledge5() {
		return w3WillOfKnowledge5;
	}

	public void setW3WillOfKnowledge5(double w3WillOfKnowledge5) {
		this.w3WillOfKnowledge5 = w3WillOfKnowledge5;
	}

	public double getW3WillOfKnowledge6() {
		return w3WillOfKnowledge6;
	}

	public void setW3WillOfKnowledge6(double w3WillOfKnowledge6) {
		this.w3WillOfKnowledge6 = w3WillOfKnowledge6;
	}

	public double getW4SocialDesire() {
		return w4SocialDesire;
	}

	public void setW4SocialDesire(double w4SocialDesire) {
		this.w4SocialDesire = w4SocialDesire;
	}

	public static LinkedList<Patient> getPatientsforreassessment() {
		return patientsForReassessment;
	}

	public double getCpLogisticCalmC() {
		return cpLogisticCalmC;
	}

	public void setCpLogisticCalmC(double cpLogisticCalmC) {
		this.cpLogisticCalmC = cpLogisticCalmC;
	}

	public double getCpLogisticReputationC() {
		return cpLogisticReputationC;
	}

	public void setCpLogisticReputationC(double cpLogisticReputationC) {
		this.cpLogisticReputationC = cpLogisticReputationC;
	}

	public double getBeta1() {
		return beta1;
	}

	public void setBeta1(double beta1) {
		this.beta1 = beta1;
	}

	public double getBeta2() {
		return beta2;
	}

	public void setBeta2(double beta2) {
		this.beta2 = beta2;
	}

	public int getSizeAllMyPatients() {
		return sizeAllMyPatients;
	}

	public void setSizeAllMyPatients(int sizeAllMyPatients) {
		this.sizeAllMyPatients = sizeAllMyPatients;
	}
		
}
