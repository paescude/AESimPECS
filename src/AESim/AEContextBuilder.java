package AESim;




import java.io.IOException;

import Datos.Reader;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class AEContextBuilder implements ContextBuilder<Object> {


	private static int currentRun;
	public static final int TRIAGE_CUBLICLES = 5;
	//public static final int TROLLEYS = 10; NO SIRVE?
	public static final int NUM_MINOR_CUBICLES = 5;
	public static final int NUM_MAJOR_CUBLICLES = 5;
	public static final int NUM_RESUS_CUBICLES = 3;
	public static final int NUM_X_RAY_ROOMS = 3;
	public static final int NUM_TEST_ROOMS = 5;
	public static final int NUM_SHO = 9;
	public static final int NUM_CONSULTANT = 1;
	public static final int NUM_PATIENTS = 2;
	public static final int NUM_NURSES = 5;
	public static final int NUM_CLERK = 2;
//	XXX este es el real: public static final int NURSE_MULTITASK = 4;
	public static final int NURSE_MULTITASK = 6;
	public static final int SHO_MULTITASK = 4;
	public static final int CONSULTANT_MULTITASK = 6;
	public static final int CLERK_MULTITASK = 1;
	// int clerkInitial = (Integer) params.getValue("clerk_count");

	
	@Override
	public Context<Object> build(Context<Object> context) {		

//		System.out.println("context");
		context.setId("Modelo");
		currentRun++;
		
		// currentRun = RunEnvironment.getInstance().getParameters().
		System.out.println("current run is: " + currentRun);
		// context.removeAll(context);
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);

		//
		// ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
		// "space", context, new RandomCartesianAdder<Object>(),
		// new repast.simphony.space.continuous.WrapAroundBorders(), 50,
		// 50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 20, 20));

		Parameters params = RunEnvironment.getInstance().getParameters();
		SimObject.initSaticVar();
		Clerk.initSaticVars();
		Sho.initSaticVar();
		Consultant.initSaticVars();
		Nurse.initSaticVars();
		Resource.initSaticVars();
		Patient.initSaticVars();
		
		
		
		Administrator admin= Administrator.getAdmin();
		context.add(admin);


		for (int i = 1; i <= TRIAGE_CUBLICLES; i++) {
			Resource cubicleTriage = new Resource("triage cubicle ",
					"triage cublicle " + i, grid);
			context.add(cubicleTriage);
			grid.moveTo(cubicleTriage, i + 3, 3);
		}

//		for (int i = 1; i <= trolleys; i++) {
//			Resource trolley = new Resource("trolley ", "trolley " + i, grid);
//			context.add(trolley);
//			grid.moveTo(trolley, 16, i + 1);
//		}

		for (int i = 1; i <= NUM_MINOR_CUBICLES; i++) {
			Resource minorCubicle = new Resource("minor cubicle ",
					"minor cublicle " + i, grid);
			context.add(minorCubicle);
			grid.moveTo(minorCubicle, 5, 9 + i);
		}

		for (int i = 1; i <= NUM_MAJOR_CUBLICLES; i++) {
			Resource majorCubicle = new Resource("major cubicle ",
					"major cublicle " + i, grid);
			context.add(majorCubicle);
			grid.moveTo(majorCubicle, 3, 9 + i);

		}

		for (int i = 1; i <= NUM_RESUS_CUBICLES; i++) {
			Resource resusCubicle = new Resource("resus cubicle ",
					"resus cublicle " + i, grid);
			context.add(resusCubicle);
			grid.moveTo(resusCubicle, 1, i + 9);
		}

		for (int i = 1; i <= NUM_X_RAY_ROOMS; i++) {
			Resource xRayRooms = new Resource("xRayRoom ", "xRayRoom " + i,
					grid);
			context.add(xRayRooms);
			grid.moveTo(xRayRooms, i + 10, 15);
		}

		for (int i = 1; i <= NUM_TEST_ROOMS; i++) {
			Resource testRooms = new Resource("testRoom ", "testRoom " + i,
					grid);
			context.add(testRooms);
			grid.moveTo(testRooms, i + 10, 11);
		}
		
		for (int i = 1; i <= NUM_CLERK; i++) {
			Clerk clerk = new Clerk(grid, i, i, 3, CLERK_MULTITASK);
			context.add(clerk);
			grid.moveTo(clerk, 17, 4+i);
		}
		// Doctors
	
		for (int i = 1; i <= NUM_SHO; i++) {			
			Doctor doctor = new Sho(grid, 6 + i, 5, i, SHO_MULTITASK);
			context.add(doctor);
			grid.moveTo(doctor, 19, 4 + i);
		}

		for (int i = 1; i <= NUM_CONSULTANT; i++) {
			
			Doctor doctor = new Consultant(grid, 6 + i, 0, i, CONSULTANT_MULTITASK);
			context.add(doctor);
			grid.moveTo(doctor, i + 6, 0);
		
		}
		Exit exit;
		try {
			exit = new Exit(grid, currentRun);
			context.add(exit);
			grid.moveTo(exit, 14, 5);
		} catch (IOException e) {
			e.printStackTrace();
		}
		WalkInDoor walkIn = new WalkInDoor("Door ", "WalkIn Door", grid);
		context.add(walkIn);
		grid.moveTo(walkIn, 0, 0);

		AmbulanceIn ambulanceIn = new AmbulanceIn("Door ", "Ambulance Door",
				grid);
		context.add(ambulanceIn);
		grid.moveTo(ambulanceIn, 0, 6);	

		// int nurseInitial = (Integer) params.getValue("nurse_count");
		for (int i = 1; i <= NUM_NURSES; i++) {
			Nurse nurse = new Nurse(grid, i + 7, 2, i, NURSE_MULTITASK);
			context.add(nurse);
			grid.moveTo(nurse, 18, 4+i);

		}

		

		// int patientInitial = (Integer) params.getValue("patient_count");
//		for (int i = 1; i <= NUM_PATIENTS; i++) {
//			Patient patient = new Patient(grid, "Context", 0);
//			context.add(patient);
//			// int x= (int) (50* Math.random());
//			// int y= (int) (50* Math.random());
//			// grid.moveTo(patient, x,y);
//			grid.moveTo(patient, 1, 0);
//		}

		SimObject simObject = new SimObject();
		context.add(simObject);
		QueueSim queueBReassess = new QueueSim("queueBReassess ", grid);
		context.add(queueBReassess);
		grid.moveTo(queueBReassess, 7, 9);
		QueueSim queueR = new QueueSim("queueR ", grid);
		context.add(queueR);
		grid.moveTo(queueR, 1, 1);
		// grid.moveTo(QueueSimR, 15, 0);
		QueueSim queueTriage = new QueueSim("queueTriage ", grid);
		context.add(queueTriage);
		grid.moveTo(queueTriage, 5, 1);
		// grid.moveTo(QueueSimTriage, 16, 0);
		QueueSim qBlue = new QueueSim("qBlue ", grid);
		context.add(qBlue);
		grid.moveTo(qBlue, 5, 7);
		QueueSim qGreen = new QueueSim("qGreen ", grid);
		context.add(qGreen);
		grid.moveTo(qGreen, 4, 7);
		QueueSim qYellow = new QueueSim("qYellow ", grid);
		context.add(qYellow);
		grid.moveTo(qYellow, 3, 7);
		QueueSim qOrange = new QueueSim("qOrange ", grid);
		context.add(qOrange);
		grid.moveTo(qOrange, 2, 7);
		QueueSim qRed = new QueueSim("qRed ", grid);
		context.add(qRed);
		grid.moveTo(qRed, 1, 7);
		QueueSim qTest = new QueueSim("qTest ", grid);
		context.add(qTest);
		grid.moveTo(qTest, 11, 9);
		QueueSim qXRay = new QueueSim("qXRay ", grid);
		context.add(qXRay);
		grid.moveTo(qXRay, 11, 13);
		QueueSim qTrolley = new QueueSim("qTrolley ", grid);
		context.add(qTrolley);
		grid.moveTo(qTrolley, 15, 7);
		
		try {
			Reader.readAllData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (RunEnvironment.getInstance().isBatch()) {
			double weeksEnd=8;
			double endSim= weeksEnd*7*24*60;
			RunEnvironment.getInstance().endAt(endSim);
//			RunEnvironment.getInstance().endAt(524160);// End the simulation
														// after
														// 52 weeks
														// RunEnvironment.getInstance().endAt(10080);
			double simTime = RunEnvironment.getInstance().getSparklineLength();
		
			
//			System.out.println("Simulation will end at: " + simTime);
		}

		
		return context;
	}

}
