package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UtilizationMips implements UtilizationModel {
	private UtilizationModel model;
	private Double ioUtilizationCutOff;
	private Double decreaseUtilizationByFactor;
	
	/** The history. */
	private Map<Double, Double> history;

	/**
	 * Instantiates a new mips utilization model.
	 */
	public UtilizationMips(UtilizationModel model, double ioUtilizationCutOff, double decreaseUtilizationByFactor){
		setHistory(new HashMap<Double, Double>());
		this.ioUtilizationCutOff = ioUtilizationCutOff;
		this.decreaseUtilizationByFactor = decreaseUtilizationByFactor;
		this.model = model;
	}
	

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		if (getHistory().containsKey(time)) {
			return getHistory().get(time);
		}

		double utilization = model.getUtilization(time);
		getHistory().put(time, utilization);
		return utilization;
	}
	
	public double getUtilization(double time, double ioUtilization){
		if (getHistory().containsKey(time)) {
			return getHistory().get(time);
		}
		
		double utilization = model.getUtilization(time);
		/*
		 * If the disk utilization is greater than ioUtilizationCutOff
		 * then the disk is utilized and cpu utilization should drop 
		 */
		if (ioUtilization > ioUtilizationCutOff) {
			utilization *= this.decreaseUtilizationByFactor;
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

