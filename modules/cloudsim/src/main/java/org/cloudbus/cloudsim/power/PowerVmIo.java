/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.MathUtil;

import flanagan.analysis.Stat;

/**
 * The class of a VM that stores its CPU utilization history. The history is used by VM allocation
 * and selection policies.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerVmIo extends PowerVm {

	
	private final List<Double> ioUtilizationHistory = new LinkedList<Double>();

	/**
	 * Instantiates a new power vm.
	 * 
	 * @param id the id
	 * @param userId the user id
	 * @param mips the mips
	 * @param pesNumber the pes number
	 * @param ram the ram
	 * @param bw the bw
	 * @param size the size
	 * @param priority the priority
	 * @param vmm the vmm
	 * @param cloudletScheduler the cloudlet scheduler
	 * @param schedulingInterval the scheduling interval
	 */
	public PowerVmIo(
			int id,
			int userId,
			double mips,
			double iops,
			int pesNumber,
			int ram,
			long bw,
			long size,
			int priority,
			String vmm,
			CloudletScheduler cloudletScheduler,
			double schedulingInterval) {
		super(id, userId, mips, iops, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
	}

	/**
	 * Updates the processing of cloudlets running on this VM.
	 * 
	 * @param currentTime current simulation time
	 * @param mipsShare array with MIPS share of each Pe available to the scheduler
	 * 
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
	 *         no next events
	 * 
	 * @pre currentTime >= 0
	 * @post $none
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare, Double iopsShare) {
		double time = super.updateVmProcessing(currentTime, mipsShare, iopsShare);
		if (currentTime > getPreviousTime() && (currentTime - 0.1) % getSchedulingInterval() == 0) {
			double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
			//TODO: the two histories should be alligned. Does it matter if we include the util value at clock==0 even if it's zero
			if (CloudSim.clock() != 0 || utilization != 0) {
				addUtilizationHistoryValue(utilization);
			}
			double ioUtilization = this.getTotalUtilizationOfIo(getCloudletScheduler().getPreviousTime());
			if (CloudSim.clock() != 0 || ioUtilization != 0) {
				addIoUtilizationHistoryValue(ioUtilization);
			}
			setPreviousTime(currentTime);
		}
		return time;
	}

	/**
	 * Gets the IO utilization MAD in percents.
	 * 
	 * @return the ioUtilization mean in percents
	 */
	public double getIoUtilizationMad() {
		double mad = 0;
		if (!getIoUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getIoUtilizationHistory().size()) {
				n = getIoUtilizationHistory().size();
			}
			double median = MathUtil.median(getIoUtilizationHistory());
			double[] deviationSum = new double[n];
			for (int i = 0; i < n; i++) {
				deviationSum[i] = Math.abs(median - getIoUtilizationHistory().get(i));
			}
			mad = Stat.median(deviationSum);
		}
		//FIXME: should probably be mad * getIops(). A bug report has been generated @ cloudsim.
		return mad;
	}

	/**
	 * Gets the utilization mean in IOPS.
	 * 
	 * @return the utilization mean in IOPS
	 */
	public double getIoUtilizationMean() {
		double mean = 0;
		if (!getIoUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getIoUtilizationHistory().size()) {
				n = getIoUtilizationHistory().size();
			}
			for (int i = 0; i < n; i++) {
				mean += getIoUtilizationHistory().get(i);
			}
			mean /= n;
		}
		return mean * getIops();
	}

	/**
	 * Gets the utilization variance in MIPS.
	 * 
	 * @return the utilization variance in MIPS
	 */
	public double getIoUtilizationVariance() {
		double mean = getIoUtilizationMean();
		double variance = 0;
		if (!getIoUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getIoUtilizationHistory().size()) {
				n = getIoUtilizationHistory().size();
			}
			for (int i = 0; i < n; i++) {
				double tmp = getIoUtilizationHistory().get(i) * getIops() - mean;
				variance += tmp * tmp;
			}
			variance /= n;
		}
		return variance;
	}

	/**
	 * Adds the io utilization history value.
	 * 
	 * @param ioUtilization the utilization
	 */
	public void addIoUtilizationHistoryValue(double ioUtilization) {
		getIoUtilizationHistory().add(0, ioUtilization);
		if (getIoUtilizationHistory().size() > HISTORY_LENGTH) {
			getIoUtilizationHistory().remove(HISTORY_LENGTH);
		}
	}

	/**
	 * Gets the IO utilization history.
	 * 
	 * @return the ioUtilization history
	 */
	protected List<Double> getIoUtilizationHistory() {
		return ioUtilizationHistory;
	}

}
