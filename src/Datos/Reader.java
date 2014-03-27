package Datos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;


public class Reader {
	
	private static float[][][] matricesNurse;
	private static float[][][] matricesDoctor;
	private static float[][][] matricesClerk;
	private static float[][][] matricesSHOD;
	
	private static float[][] arrayDNW; 

		
	private static float[][] matrixArrivalWalkIn;
	private static float[][] matrixArrivalAmbulance;
	private static float[][] matrixTriagePropByArrival;
	private static float[][] matrixPropTest;

	
	public static final  String PATH = "C:\\RepastSimphony-2.1\\AESim\\src\\Datos\\";
	
	public static void readAllData() throws IOException {
		setMatrixArrivalWalkIn(readFileIn(PATH + "DatosIn.txt", 24, 7));
		setMatrixArrivalAmbulance(readFileIn(PATH + "DatosAmbulance.txt", 24, 7));
		
		matricesClerk = new float[3][][];
		for (int i = 1; i <= 2; ++i){
			setMatrixClerk(i,readFileIn(PATH + "datosClerk"+i+".txt", 24, 7));
		}
		
		matricesNurse = new float[6][][];
		for (int i = 1; i <= 5; ++i){
			setMatrixNurse(i,readFileIn(PATH + "datosNurse"+i+".txt", 24, 7));
		}
		
		setArrayDNW(readFileIn(PATH + "datosDNW.txt", 24, 1));
		
		matricesSHOD = new float [10][][];
		for (int i = 0; i <= 9; ++i){
			setMatrixSHOD(i,readFileIn(PATH + "datosSHO_D"+i+".txt", 24, 7));
			System.out.println(i);
		}
		
		setMatrixTriagePropByArrival(readFileIn(PATH + "datosTriageByArrival.txt", 5, 2));
		// En la MatrixTriagePropArrival la primera columna corresponde a WalkIn
		// y la segunda a Ambulance, son las distribuciones acumuladas
		setMatrixPropTest(readFileIn(PATH + "datosProportionsTests.txt", 5, 2));
		// En la MatrixPropTest la primera columna corresponde a XRay y la
		// segunda a Test
	}
	
	public static float[][] readFileIn(String fileName, int rows, int cols)
			throws IOException {
		String arrivalMatrix[][] = new String[rows][cols];
		float[][] arrivalMatrixfloat = new float[rows][cols];
		String line, token, delimiter = ",";

		StringTokenizer tokenizer;

		BufferedReader input = null;
		int i = 0;
		int j = 0;
		try {
			input = new BufferedReader(new FileReader(fileName));
			line = input.readLine(); // when printed gives first line in file
			// outer while (process lines)
			while (line != null) { // doesn't seem to start from first line
				tokenizer = new StringTokenizer(line, delimiter);

				while (tokenizer.hasMoreTokens()) {// process tokens in line
					token = tokenizer.nextToken();
					arrivalMatrix[i][j] = token;
					j++;
				}// close inner while
				j = 0;
				line = input.readLine(); // next line
				i++;
			}// close outer while

		} catch (FileNotFoundException e) {
//			System.out.println("Unable to open file " + fileName);
		} catch (IOException e) {
//			System.out.println("Unable to read from file " + fileName);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
//				System.out.println("Unable to close file " + fileName);
			}
		}

		for (int a = 0; a < rows; a++) {
			for (int b = 0; b < cols; b++) {

				arrivalMatrixfloat[a][b] = Float
						.parseFloat(arrivalMatrix[a][b]);

			}


		}

		return arrivalMatrixfloat;
	}
	
	public static void setMatrixNurse(int n, float [][] matrixNurse){
		Reader.matricesNurse[n] = matrixNurse;
	}
	
	public static float[][] getMatrixNurse(int n){
		return Reader.matricesNurse[n];
	}

	
	public static float[][] getArrayDNW() {
		return arrayDNW;
	}

	public static void setArrayDNW(float[][] arrayDNW) {
		Reader.arrayDNW = arrayDNW;
	}
	
	public static float [][] getMatrixClerk(int id){
		return matricesClerk[id];
	}
	
	public static void setMatrixClerk(int n, float[][] matrixClerk){
		Reader.matricesClerk[n]=matrixClerk;
	}


	public static float[][] getMatrixArrivalWalkIn() {
		return matrixArrivalWalkIn;
	}

	public static void setMatrixArrivalWalkIn(float[][] matrixArrivalWalkIn) {
		Reader.matrixArrivalWalkIn = matrixArrivalWalkIn;
	}

	public static float[][] getMatrixArrivalAmbulance() {
		return matrixArrivalAmbulance;
	}

	public static void setMatrixArrivalAmbulance(float[][] matrixArrivalAmbulance) {
		Reader.matrixArrivalAmbulance = matrixArrivalAmbulance;
	}

	public static float[][] getMatrixTriagePropByArrival() {
		return matrixTriagePropByArrival;
	}

	public static void setMatrixTriagePropByArrival(
			float[][] matrixTriagePropByArrival) {
		Reader.matrixTriagePropByArrival = matrixTriagePropByArrival;
	}

	public static float[][] getMatrixPropTest() {
		return matrixPropTest;
	}

	public static void setMatrixPropTest(float[][] matrixPropTest) {
		Reader.matrixPropTest = matrixPropTest;
	}

	public static float [][] getMatrixSHOD(int id){
		return matricesSHOD[id];
	}
	
	public static void setMatrixSHOD(int n, float[][] matrixClerk){
		Reader.matricesSHOD[n]=matrixClerk;
	}

}
