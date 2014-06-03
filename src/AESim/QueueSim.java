package AESim;

import java.util.Iterator;
import java.util.LinkedList;

import repast.simphony.space.grid.Grid;

public class QueueSim extends SimObject{
	private int maxInQueue;
	private double maxWaitTime;

	// number in queue
	private double meanInQueue;

	// Simulation variables
	// time in queue
	private double meanWaitTime;
	String name;
	// agents in queue
	private Patient patientInQueue;

	private Patient patientOutQueue;
	// private GridPoint loc;
	private LinkedList<Patient> queue;

	private int totalInQueue;
	private double totalWaitTime;
	
	public QueueSim(String queueName, Grid<Object> grid) {
		queue = new LinkedList<Patient>();
		this.setName(queueName);
		this.setId(queueName);
		this.grid = grid;
		maxWaitTime = 0;
		maxInQueue = 0;
		meanInQueue = 0;
		meanWaitTime = 0;
		totalWaitTime = 0;
		totalInQueue = 0;
	//	setLoc(grid.getLocation(this));
	}

	public Boolean addPatientToQueue(Patient patient) {
		boolean b = queue.add(patient);
		patientInQueue = patient;
		

		totalInQueue = this.queue.size();

		return b;
	}

	public void removeFromQueue(Patient patient) {
		queue.remove(patient);
		String qName = patient.getCurrentQueue().getName();
		double qTime = getTime() - patient.getTimeEnteredCurrentQ();
		if (qName.equals("queueR ")) {
			patient.setQueuingTimeQr(qTime);
//			System.out.println("****------******---- "
//					+ patient.getId() + " time in " + qName + ": "
//					+ patient.getQueuingTimeQr());
		}
		if (qName.equals("queueTriage ")) {
			patient.setQueuingTimeQt(qTime);
//			System.out.println("****------******---- "
//					+ patient.getId() + " time in " + qName + ": "
//					+ patient.getQueuingTimeQt());
		}
		if (qName.equals("queueInitA ")) {
			patient.setQueuingTimeQa(qTime);
//			System.out.println("****------******---- "
//					+ patient.getId() + " time in " + qName + ": "
//					+ patient.getQueuingTimeQa());
		}
		if (qName.equals("qTest ")) {

		}
		totalInQueue = this.queue.size();
//		patient.setCurrentQueue(null);
	}

	public void elementsInQueue() {

		Patient patientQueuing = null;
		Iterator<Patient> iter = this.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2){
//			System.out.println("" + this.getId() + ": "
//					+ a.substring(0, a.length() - 2) + "]");
		}
	}
	
	public int getSize() {
		return totalInQueue;
	}
	
	public Iterator<Patient> iterator() {
		return queue.iterator();

	}

	public Patient firstInQueue() {
		return queue.peek();
	}
	
	public int getMaxInQueue() {
		return maxInQueue;
	}
	public void setMaxInQueue(int maxInQueue) {
		this.maxInQueue = maxInQueue;
	}
	public double getMaxWaitTime() {
		return maxWaitTime;
	}
	public void setMaxWaitTime(double maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
	public double getMeanInQueue() {
		return meanInQueue;
	}
	public void setMeanInQueue(double meanInQueue) {
		this.meanInQueue = meanInQueue;
	}
	public double getMeanWaitTime() {
		return meanWaitTime;
	}
	public void setMeanWaitTime(double meanWaitTime) {
		this.meanWaitTime = meanWaitTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Patient getPatientInQueue() {
		return patientInQueue;
	}
	public void setPatientInQueue(Patient patientInQueue) {
		this.patientInQueue = patientInQueue;
	}
	public Patient getPatientOutQueue() {
		return patientOutQueue;
	}
	public void setPatientOutQueue(Patient patientOutQueue) {
		this.patientOutQueue = patientOutQueue;
	}
	public LinkedList<Patient> getQueue() {
		return queue;
	}
	public void setQueue(LinkedList<Patient> queue) {
		this.queue = queue;
	}
	public int getTotalInQueue() {
		return totalInQueue;
	}
	public void setTotalInQueue(int totalInQueue) {
		this.totalInQueue = totalInQueue;
	}
	public double getTotalWaitTime() {
		return totalWaitTime;
	}
	public void setTotalWaitTime(double totalWaitTime) {
		this.totalWaitTime = totalWaitTime;
	}
}
