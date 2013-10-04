package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UtilizationIopsSimple implements UtilizationModel {
	
	private Double iopsPerMips;
	/*
	 * mips available to vm
	 */
	private Double mipsAvailable;	/*
	 * iops available to vm
	 */
	private Double iopsAvailable;
	private Double mipsUtilization;
	
	/** The history. */
	private Map<Double, Double> history;

	/**
	 * Instantiates a new iops utilization model using a rule of thumb between iops and mips.
	 * See "Characteristics of I/O Traffic in Personal Computer and Server Workloads"
	 * by W. Hsu and A. Smith
	 */
	public UtilizationIopsSimple(double iopsPerMips){
		setHistory(new HashMap<Double, Double>());
		this.iopsPerMips = iopsPerMips;
	}
	

	public Double getMipsAvailable() {
		return mipsAvailable;
	}

	public void setMipsAvailable(Double mipsAvailable) {
		this.mipsAvailable = mipsAvailable;
	}

	public Double getIopsAvailable() {
		return iopsAvailable;
	}

	public void setIopsAvailable(Double iopsAvailable) {
		this.iopsAvailable = iopsAvailable;
	}

	public void setMipsUtilization(double mipsUtilization){
		this.mipsUtilization = mipsUtilization;
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
		utilization = (mipsUtilization * mipsAvailable * iopsPerMips) / iopsAvailable;
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

