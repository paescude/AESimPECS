package Funciones;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import repast.simphony.random.RandomHelper;
import cern.jet.random.Exponential;
import cern.jet.random.Uniform;

public class MathFunctions {
	
	private double zLinear;
	private double zLogistic;
	private double zBipolar;
	private double zHyperTan;
	private double vPhysis;
	private double vEmotion;
	private double vCognition;
	private double vSocial;
	
	public Uniform uniform(double min, double max) {
		Uniform uniform = RandomHelper.createUniform(min, max);
		return uniform;
	}
	
	public static Exponential exponential(double mean) {
		double lambda = 1 / mean;
		Exponential exponential = RandomHelper.createExponential(lambda);
		return exponential;
	}
	
	
	// function c/(c+x)
	public static double calcFCOverCplusX(double c1, double c2, double x1, double x2) {
		double z = c1 * c2 / (c1 * c2 + x1 * x2);
		return z;

	}

	// function logsistic alpha positivive
	public static double calcFLogisticPositive(double x, double alpha, double c) {
		double logistic = 1 / (1 + Math.exp(alpha * (x - c)));
		return logistic;

	}

	// function logsistic alpha negative
	public static double calcFLogisticNegative(double x, double alpha, double c) {
		double logistic = 1 / (1 + Math.exp(-alpha * (x - c)));
		return logistic;

	}
	public static double distTriangular(double min, double mode, double max) {

		double T = 0;
		double obs = 0;
		double constant = (mode - min) / (max - min);
		double random = Math.random();
		double T1 = (Math.sqrt((constant * random)));
		double T2 = 1 - Math.sqrt((1 - constant) * (1 - random));
		if (random < (constant))
			T = T1;
		else
			T = T2;
		obs = min + (max - min) * T;
		return obs;
	}

	public static double distLognormal(double min, double mean, double max) {
		double average = mean;
		double stdDev = max;
		double mcuadrado = Math.pow(average, 2);
		double v = stdDev;
		double a = mcuadrado / (Math.sqrt(v + mcuadrado));
		double scale = Math.log(a);
		double shape = Math.sqrt(Math.log(1 + (v / mcuadrado)));
		LogNormalDistribution log = new LogNormalDistribution(scale, shape, 0.95);
		double obs = (log.sample());
		return obs;
	}
	
	public static float findMax(float matrix[][]) {
		float max = matrix[0][0];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] > max) {
					max = matrix[i][j];
				}
			}
		}
		return max;
	}
	
	public double linearTransfer(double u) {
		zLinear = u;
		return zLinear;
	}
	
	public double logisticTransfer(double a, double c, double z) {
		zLogistic = 1 / (1 + Math.exp((-a * (z - c))));

		return zLogistic;
	}

	public double bipolarTransfer(double u) {
		zBipolar = ((2 / (1 + Math.exp(-u))) - 1);

		return zBipolar;
	}

	public double hyperTan(double u) {
		zHyperTan = Math.tanh(u);

		return zHyperTan;
	}

	public double getzLinear() {
		return zLinear;
	}

	public void setzLinear(double zLinear) {
		this.zLinear = zLinear;
	}

	public double getzLogistic() {
		return zLogistic;
	}

	public void setzLogistic(double zLogistic) {
		this.zLogistic = zLogistic;
	}

	public double getzBipolar() {
		return zBipolar;
	}

	public void setzBipolar(double zBipolar) {
		this.zBipolar = zBipolar;
	}

	public double getzHyperTan() {
		return zHyperTan;
	}

	public void setzHyperTan(double zHyperTan) {
		this.zHyperTan = zHyperTan;
	}

	public double getvPhysis() {
		return vPhysis;
	}

	public void setvPhysis(double vPhysis) {
		this.vPhysis = vPhysis;
	}

	public double getvEmotion() {
		return vEmotion;
	}

	public void setvEmotion(double vEmotion) {
		this.vEmotion = vEmotion;
	}

	public double getvCognition() {
		return vCognition;
	}

	public void setvCognition(double vCognition) {
		this.vCognition = vCognition;
	}

	public double getvSocial() {
		return vSocial;
	}

	public void setvSocial(double vSocial) {
		this.vSocial = vSocial;
	}

}
