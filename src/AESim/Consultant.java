package AESim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import Datos.Reader;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Consultant extends Doctor {
	private static int count;

	public Consultant(Grid<Object> grid, int initPosX, int initPosY, int idNum, int multiTasking) {
		this.idNum = idNum;
		this.grid = grid;
		this.numAvailable = 0;
		this.initPosX = initPosX;
		this.initPosY = initPosY;
		this.setId("Consultant doctor " + idNum);
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
	
	public double[] firstAssessmentParameters(int triage) {
		double parameters[] = { 0, 0, 0 };
		switch (triage) {
		case 1:
			parameters[0] = 5;
			parameters[1] = 15;
			parameters[2] = 45;
			break;
		case 2:
			parameters[0] = 5;
			parameters[1] = 15;
			parameters[2] = 45;
			break;
		case 3:
			parameters[0] = 1;
			parameters[1] = 30;
			parameters[2] = 24;
			break;
		case 4:
			parameters[0] = 1;
			parameters[1] = 30;
			parameters[2] = 24;
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
		// TODO crear una lista con pacientes rojos y que el consultan mire la lista antes de decidir que hacer 
//		System.out.println(this.getId()+ " is consultant and is checking any other sho is available");
		Patient patient = null;
		Boolean flag = false;
		if(this.isAvailable()){
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
			if (patient!=null ){
				if(patient.getTriageNum()==5){
					Resource bed = findBed(patient.getTriageNum());
					Nurse nurse = findNurse();
					if(bed!=null){
						this.startFirstAssessment(patient, nurse, bed);
					}
					
				} else {
					Doctor shoAvailable = checkForAnyAvailableDoctor();
					if (shoAvailable == null) {		
						System.out.println(this.getId()+ " is Consultant and is checking if start init assessment");			
						if (!this.checkIfStartInitAssessment()) {
//							if (!this.isAtDoctorArea) {
//								this.moveToDoctorsArea();
//							}
						}
					
					}
					else shoAvailable.checkIfStartInitAssessment();
					
				}
			}
		}
		
		
//		Doctor shoAvailable = checkForAnyAvailableDoctor();
//		if (shoAvailable == null) {		
////			System.out.println(this.getId()+ " is SHO and is checking if start init assessment");			
//			if (!this.checkIfStartInitAssessment()) { 
////				if (!this.isAtDoctorArea) {
////					this.moveToDoctorsArea();
////				}
//			}
//		
//		}
//		else shoAvailable.checkIfStartInitAssessment();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see AESim.Doctor#reassessmentParameters(int)
	 * •	TaskDataCollectionEnabled: True 
•  QueueDataCollectionEnabled: True 
•  BeginEffect:
Entity.time.StartResus=Clock;
Entity.time.resStartDay=day;
Entity.time.resStartHour=hour;
Entity.AssignedRoom=0;
// Assign a doctor type
if (Entity.TriageColor==cRed) {
  Entity.doctorType=1;//All red patients are treated by a consultant
} else {
  if (RSHO[day,hour]>=1) Entity.doctorType=2;
  else if (RConsultant[day,hour]>=1) Entity.doctorType=1;
}


//Model.PrintOutput("Doctor type at Resus "+Entity.doctorType);
// Decrement the number of staff
if (Entity.TriageColor==cRed){
	RConsultant[day,hour]--;
	RSHO[day,hour]--;
	RNurse[day,hour]-=(1*mtfNurse);//One nurse always stays with the patient
} else { //Orange	
	RNurse[day,hour]-=(1*mtfNurse);
	if (Entity.doctorType==1) RConsultant[day,hour]--;
	else RSHO[day,hour]--;
}

// Decrement the number of cubicals
capacity[0]--;

	 */
	
	
	@Override
	public double[] reassessmentParameters(int Triage) {
		double parameters[] = { 0, 0, 0 };
		switch (Triage) {
		case 1:
			parameters[0] = 1;
			parameters[1] = 8;
			parameters[2] = 5;
			break;
		case 2:
			parameters[0] = 1;
			parameters[1] = 8;
			parameters[2] = 5;
			break;
		case 3:
			parameters[0] = 1;
			parameters[1] = 22;
			parameters[2] = 19;
			break;
		case 4:
			parameters[0] = 1;
			parameters[1] = 22;
			parameters[2] = 19;
			break;
		case 5:
			parameters[0] = 1;
			parameters[1] = 27;
			parameters[2] = 15;
			break;
		}
		return parameters;
	}

	private Doctor checkForAnyAvailableDoctor() {
		Doctor shoAvailable = null;
		System.out
				.println(" checks if there is any sho available to start init assessment ");
		for (Object sho : getContext().getObjects(Sho.class)) {
			Doctor shoToCheck = (Doctor) sho;
			if (shoToCheck.isAvailable()) {
				shoAvailable = shoToCheck;
				System.out.println(shoAvailable.getId() + " is available ");
				break;
			}
		}
		return shoAvailable;
	}
	
	
	@ScheduledMethod(start = 0, interval=480,priority = 99, shuffle = false, pick = 1)
	public void resetConsultantShiftParams() {
		this.setC1MyMaxPatientHour(10);
		this.setC2MyDurationShift(8.0);// hay un solo consultant que se reinica cada 8 horas
		this.setC3LogisticCalmC(3);
		this.setC4LogisticKnowledgeC(0.15);
		this.setC5LogisticExperienceC(-50);
		this.setC6LogisticReputationC(2.5);
		this.setAlpha1Calmness(0.0002);
		this.setAlpha2Knowledge(1);
		this.setAlpha3Experience(0.0175);
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
	}
	@ScheduledMethod(start = 0,priority = 99, shuffle = false, pick = 1)
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

//		this.setX1MyNumPatientsSeen(0);
//		this.setX2MyTimeWorkedInShift(0);// this time
//											// is in
//											// hours
//		this.setX3TriageMaxAmongMyPatients(1);
//		this.setX4MyPatientsAverageTimeInSys(0);
//		this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
//													// calculated individually
//													// to obtain a knowledge of
//													// each patient
//		this.setX6MyTotalTimeWorkedInDpmnt(0);
//		this.setX7MyPatientsMaxTimeInSys(0);

		int id = this.getIdNum();
		
		switch (id) {

		case 1:
			this.setMyShiftMatrix(Reader.getMatrixSHOD(0));
			// this doctor is a consultant, minimum experience is 8 years
			/*for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 24; j++) {
					sum = sum + this.getMyShiftMatrix()[j][i];
				}
				this.setDurationOfShift(i,sum);
			}*/
//			this.setC1MyMaxPatientHour(10);
//			this.setC2MyDurationShift(8.0);// hay un solo consultant que se reinica cada 8 horas
//			this.setC3LogisticCalmC(3);
//			this.setC4LogisticKnowledgeC(0.15);
//			this.setC5LogisticExperienceC(-50);
//			this.setC6LogisticReputationC(2.5);
//			this.setAlpha1Calmness(0.0002);
//			this.setAlpha2Knowledge(1);
//			this.setAlpha3Experience(0.0175);
			break;

		}
		System.out.println(this.getId() + " shift's duration ["
				+ this.durationOfShift[0] + " ," + this.durationOfShift[1]
				+ "," + this.durationOfShift[2] + " ,"
				+ this.durationOfShift[3] + " ," + this.durationOfShift[4]
				+ ", " + this.durationOfShift[5] + ", "
				+ this.durationOfShift[6] + "]");
	}

	@Override
	public double getC2MyDurationShift(){
		return this.c2MyDurationShift;
	}
	
	public static void initSaticVars() {
		setCount(0);		
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Consultant.count = count;
	}

}
