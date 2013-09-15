package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UtilizationIops implements UtilizationModel {
	private UtilizationModel model;
	private Double a;
	private Double b;
	/*
	 * mips until next iops
	 */
	private Double mipsLeft;
	private Double mipsUtilization;
	
	/** The history. */
	private Map<Double, Double> history;
	private double allocatedMips;

	/**
	 * Instantiates a new iops utilization model.
	 * See "Characteristics of I/O Traffic in Personal Computer and Server Workloads"
	 * by W. Hsu and A. Smith
	 */
	public UtilizationIops(UtilizationModel model, double a, double b){
		setHistory(new HashMap<Double, Double>());
		this.a = a;
		this.b = b;
		this.model = model;
		this.mipsLeft = 0.0;
	}
	
	public void updateMipsLeft(double mipsToRemove){
		this.mipsLeft -= mipsToRemove;
	}
	
	public void setMipsUtilization(double mipsUtilization){
		this.mipsUtilization = mipsUtilization;
	}
	
	public void setAllocatedMips(double allocatedMips){
		this.allocatedMips = allocatedMips;
	}
	
	/*
	 * Returns an estimation of how many mips must be completed until next iops
	 */
	public double getMipsUntilNextIopsIssue(){
		double timeToNextIopsIssue = 0;
		if (this.mipsUtilization > 0.01){
			timeToNextIopsIssue = ((1 - this.b * this.mipsUtilization) / (this.a * this.mipsUtilization));
		}
		double mipsUntilNextIopsIssue = timeToNextIopsIssue * this.allocatedMips * this.mipsUtilization;
		return mipsUntilNextIopsIssue;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time){
		if (getHistory().containsKey(time)) {
			return getHistory().get(time);
		}

		double utilization = 0;
		System.err.println(time + ": mipsLeft = " + this.mipsLeft);
		if (this.mipsLeft <= 0) {
			this.mipsLeft = this.getMipsUntilNextIopsIssue();
			System.err.println(time + ": new mipsLeft = " + this.mipsLeft);
			utilization = model.getUtilization(time);
		}
		getHistory().put(time, utilization);
		return utilization;
	}

	/**
	 * Gets the history.
	 * 
	 * @return the history
	 */
	protected Map<Double, Double> getHistory() {
		return history;
	}

	/**
	 * Sets the history.
	 * 
	 * @param history the history
	 */
	protected void setHistory(Map<Double, Double> history) {
		this.history = history;
	}

	/**
	 * Save history.
	 * 
	 * @param filename the filename
	 * @throws Exception the exception
	 */
	public void saveHistory(String filename) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(getHistory());
		oos.close();
	}

	/**
	 * Load history.
	 * 
	 * @param filename the filename
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public void loadHistory(String filename) throws Exception {
		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);
		setHistory((Map<Double, Double>) ois.readObject());
		ois.close();
	}


}

