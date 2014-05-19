package AESim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import Datos.Reader;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Sho extends Doctor {
	private static int count;

	public Sho(Grid<Object> grid, int initPosX, int initPosY,
			int idNum, int multiTasking) {
		this.idNum = idNum;
		this.grid = grid;
		this.numAvailable = 0;
		this.initPosX = initPosX;
		this.initPosY = initPosY;
		this.setId("SHO doctor " + idNum);
		this.myPatientCalling = null;
		this.available = false;
		this.nextEndingTime = 0;
		this.timeEnterSimulation = getTime();
		this.myPatientsInBed = new PriorityQueue<Patient>(5,
				new PriorityQueueComparatorTime());
		this.myPatientsInTests = new ArrayList<Patient>();
		this.myPatientsBackInBed = new LinkedList<Patient>();
		this.doctorToHandOver = null;
		this.multiTaskingFactor = multiTasking;
		this.patientsInMultitask = new ArrayList<Patient>();
		this.allMyPatients = new ArrayList<Patient>();
		this.isAtDoctorArea = false;
//		this.beta1 = 0.7;
//		this.beta2 = 0.3;
//		this.cpLogisticCalmC = 3;
//		this.cpLogisticReputationC = 3;
	}
	
	@Override
	public double[] firstAssessmentParameters(int triage) {
		double parameters[] = { 0, 0, 0 };
		switch (triage) {
		case 1:
			parameters[0] = 5;
			parameters[1] = 20;
			parameters[2] = 55;
			break;
		case 2:
			parameters[0] = 5;
			parameters[1] = 20;
			parameters[2] = 55;
			break;
		case 3:
			parameters[0] = 1;
			parameters[1] = 38;
			parameters[2] = 30;
			break;
		case 4:
			parameters[0] = 1;
			parameters[1] = 38;
			parameters[2] = 30;
			break;
		case 5:
			parameters[0] = 1;
			parameters[1] = 27;
			parameters[2] = 15;
			break;
		}
		return parameters;
	}
	
	@Override
	public void decideWhatToDo() {
		
		// TODO Auto-generated method stub
		System.out.println(" checks if there is any sho available to start init assessment ");
		if (!this.checkIfStartReassessment()){
			if (!this.checkIfStartInitAssessment()) {
				if (!this.isAtDoctorArea) {
					System.out.println(this.getId()+" is moving to docs area because when decide what to do has nothing to do ");
					this.moveToDoctorsArea();
				}
			}
		}
	}	
	
	
	@Override
	public void decideWhatToDoStress(){
		this.checKInitAssessmentStress();
		this.setHowToChoosePats(0);
			
	}
	
	
	
	
	
	
	
	@Override
	public double[] reassessmentParameters(int Triage) {
		double parameters[] = { 0, 0, 0 };
		switch (Triage) {
		case 1:
			parameters[0] = 1;
			parameters[1] = 10;
			parameters[2] = 8;
			break;
		case 2:
			parameters[0] = 1;
			parameters[1] = 10;
			parameters[2] = 8;
			break;
		case 3:
			parameters[0] = 1;
			parameters[1] = 28;
			parameters[2] = 22;
			break;
		case 4:
			parameters[0] = 1;
			parameters[1] = 28;
			parameters[2] = 22;
			break;
		case 5:
			parameters[0] = 1;
			parameters[1] = 27;
			parameters[2] = 15;
			break;
		}
		
		return parameters;
	}
	
	@ScheduledMethod(start = 0, priority = 99, shuffle = false, pick = 1)
	public void initNumDocs() {
		printTime();
		System.out.println("When simulation starts, the conditions are "
				+ this.getId());
		GridPoint currentLoc = grid.getLocation(this);
		int currentX = currentLoc.getX();
		int currentY = currentLoc.getY();

		if (currentX == 19) {
			this.setAvailable(false);
			this.setInShift(false);
			System.out.println(this.getId()
					+ " is not in shift and is not available, time: "
					+ getTime());

		} else if (currentY == 4) {
			this.setAvailable(true);
			this.setInShift(true);
			System.out.println(this.getId()
					+ " is in shift and is available, time: " + getTime());
		}

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

		int id = this.getIdNum();
		this.setMyShiftMatrix(Reader.getMatrixSHOD(id));
		// sho minim. exp is 2 years (middle grade 6)
		// doctor middle experience
		this.setShifts();
		
		switch(id){
		case 1:
			this.setC1MyMaxPatientHour(8);
			this.setC3LogisticCalmC(2);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(0);
			this.setC6LogisticReputationC(2.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0175);
			break;

		case 2:
			this.setC1MyMaxPatientHour(8);
			this.setC3LogisticCalmC(2);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(0);
			this.setC6LogisticReputationC(2.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0175);
			break;

		case 3:
			this.setC1MyMaxPatientHour(4);
			this.setC3LogisticCalmC(1);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(200);
			this.setC6LogisticReputationC(1.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.075);
			break;

		case 4:	
			this.setC1MyMaxPatientHour(8);
			this.setC3LogisticCalmC(2);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(0);
			this.setC6LogisticReputationC(2.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0175);
			break;

		case 5:
			this.setC1MyMaxPatientHour(4);
			this.setC3LogisticCalmC(1);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(200);
			this.setC6LogisticReputationC(1.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.075);
			break;

		case 6:
			this.setC1MyMaxPatientHour(4);
			this.setC3LogisticCalmC(1);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(200);
			this.setC6LogisticReputationC(1.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.075);
			break;

		case 7:
			this.setC1MyMaxPatientHour(6);
			this.setC3LogisticCalmC(1.33);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(50);
			this.setC6LogisticReputationC(1.5);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0125);
			break;

		case 8:
			this.setC1MyMaxPatientHour(6);
			this.setC3LogisticCalmC(1.33);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(50);
			this.setC6LogisticReputationC(1.5);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0125);
			break;

		case 9:
			this.setC1MyMaxPatientHour(6);
			this.setC3LogisticCalmC(1.33);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(50);
			this.setC6LogisticReputationC(1.5);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0125);
			break;
		}
		
		System.out.println(this.getId() +  " day " + getDay() + " hour "+ getHour() + " duracion ");
		

		System.out.println(this.getId() + " shift's duration ["
				+ this.durationOfShift[0] + " ," + this.durationOfShift[1]
				+ "," + this.durationOfShift[2] + " ,"
				+ this.durationOfShift[3] + " ," + this.durationOfShift[4]
				+ ", " + this.durationOfShift[5] + ", "
				+ this.durationOfShift[6] + "]");
	}

//XXX borré un método que estaba comentado 

	@ScheduledMethod(start=0,interval=60)
	public void verificar(){
		if(this.getIdNum()==8){
			System.out.println("Week: "+this.getWeek()+" Day: "+this.getDay()+" Hour: "+this.getHour());
			System.out.println("Doctor SHO "+this.getIdNum()+" is In Shift?: "+this.isInShift());
		}
		
	}
	
	public static void initSaticVars() {
		setCount(0);		
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Sho.count = count;
	}
}
