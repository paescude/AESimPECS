package AESim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import antlr.collections.List;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;

public class Exit extends SimObject {


	private static int numExit;
	private static  double [] [] patientTimes;
	private static int varI;
	private static int currentRun;
	private static int dataNum;
	
	private static FileWriter writer;
	
	public Exit(Grid<Object> grid, int currentRun) throws IOException {
		this.grid = grid;
		Exit.currentRun = currentRun;
		patientTimes = new double [1000][1000];
		varI = 0;
//		String fileName1 = "C:\\RepastSimphony-2.1\\AESim\\Outputs\\patientTimeInSystemRuns\\";	
		String fileName1 = "C:\\Users\\pescuder\\Dropbox\\Outputs\\patientTimeInSystemRuns\\";	
		
		String fileName2 = "PatientTimeInSystemRun" + String.valueOf(currentRun)+ ".csv";
		String filePath = fileName1 + fileName2;
		writer = new FileWriter(filePath);
		writer.append("\n Tick, RunNum,Week,Day,Hour,Min, MinTotalWeek,Id,TriageColor,TriageNum, TypeArrival,TimeInSys, isBackInBed, patientDoctor, CurrentQueuet, timeRegist, timeTriage, timeFstAss, timeReAss, timeTest, timeXRay, IsSystem\n");
		Exit.dataNum = 0;
		
	}
	
	
	// TODO for now I will use the exit to keep the results and monitoring
	// things. Change it later
	
	public static void addToFile(Patient patient)  throws IOException {
			writer.append(String.valueOf(getTime()));
			writer.append(","); 
			writer.append(String.valueOf(currentRun));
			writer.append(",");
			writer.append(String.valueOf(getWeek()));
			writer.append(",");
			writer.append(String.valueOf(getDay()));
			writer.append(",");
			writer.append(String.valueOf(getHour()));
			writer.append(",");
			writer.append(String.valueOf(getMinute()));
			writer.append(",");
			writer.append(String.valueOf(getMinTotalWeek()));
			writer.append(",");
			writer.append(patient.getId());
			writer.append(",");					
			writer.append(patient.getTriage());
			writer.append(",");
			writer.append(String.valueOf(patient.getTriageNum()));					
			writer.append(",");
			writer.append(patient.getTypeArrival());
			writer.append(",");
			writer.append(String.valueOf(patient
					.getTimeInSystem()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.isBackInBed()));
			writer.append(",");
			if (patient.getMyDoctor() == null){
				writer.append("Null");
			} else {
				writer.append(patient.getMyDoctor().getId());
			}
			
			writer.append(",");
			if (patient.getCurrentQueue() == null){
				writer.append("Null");
			} else {
				writer.append(patient.getCurrentQueue().getId());
			}
			writer.append(",");
			writer.append(String.valueOf(patient
					.gettRegist()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.gettTriage()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.gettFirstAssessment()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.gettReasssesment()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.gettTest()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.gettXray()));
			writer.append(",");
			writer.append(String.valueOf(patient
					.isInSystem()));
			writer.append("\n");		
	}
	
//	@ScheduledMethod(start = 10, priority = 5, pick = 1, interval = 30)
//	public void getResults() throws IOException {
//		
//		for (Object p: getContext().getObjects(Patient.class)){		
//			if (p!=null){	
//				Patient patient = (Patient)p;
//				if (!patient.isInSystem()){
//					writer.append(String.valueOf(getTime()));
//					writer.append(","); 
//					writer.append(String.valueOf(currentRun));
//					writer.append(",");
//					writer.append(String.valueOf(getWeek()));
//					writer.append(",");
//					writer.append(String.valueOf(getDay()));
//					writer.append(",");
//					writer.append(String.valueOf(getHour()));
//					writer.append(",");
//					writer.append(String.valueOf(getMinute()));
//					writer.append(",");
//					writer.append(String.valueOf(getMinTotalWeek()));
//					writer.append(",");
//					writer.append(patient.getId());
//					writer.append(",");					
//					writer.append(patient.getTriage());
//					writer.append(",");
//					writer.append(String.valueOf(patient.getTriageNum()));					
//					writer.append(",");
//					writer.append(patient.getTypeArrival());
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.getTimeInSystem()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.isBackInBed()));
//					writer.append(",");
//					if (patient.getMyDoctor() == null){
//						writer.append("Null");
//					} else {
//						writer.append(patient.getMyDoctor().getId());
//					}
//					
//					writer.append(",");
//					if (patient.getCurrentQueue() == null){
//						writer.append("Null");
//					} else {
//						writer.append(patient.getCurrentQueue().getId());
//					}
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.gettRegist()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.gettTriage()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.gettFirstAssessment()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.gettReasssesment()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.gettTest()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.gettXray()));
//					writer.append(",");
//					writer.append(String.valueOf(patient
//							.isInSystem()));
//					writer.append("\n");
//				}				 
//			}			
//		}
//
//	}

	@ScheduledMethod(start = 1440, priority = 4)
	public void printResults() {

	}


	public static double[][] getPatientTimes() {
		return patientTimes;
	}

}
