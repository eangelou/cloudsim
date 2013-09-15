/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * CloudletSchedulerDynamicWorkload implements a policy of scheduling performed by a virtual machine
 * assuming that there is just one cloudlet which is working as an online service.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class CloudletSchedulerDynamicWorkloadIops extends CloudletSchedulerTimeShared {

	/** The mips. */
	private double mips;

	/** The number of PEs. */
	private int numberOfPes;
	
	/** For logging reasons*/
	private int HostId;

	public int getHostId() {
		return HostId;
	}

	public void setHostId(int vmId) {
		this.HostId = vmId;
	}

	/** The total mips. */
	private double totalMips;
	
	private double iops;

	/** The under allocated mips. */
	private Map<String, Double> underAllocatedMips;

	/** The cache previous time. */
	private double cachePreviousTime;

	/** The cache current requested mips. */
	private List<Double> cacheCurrentRequestedMips;
	
	/** The cache current requested iops. */
	private Double cacheCurrentRequestedIops;

	/**
	 * Instantiates a new vM scheduler time shared.
	 * 
	 * @param mips the mips
	 * @param numberOfPes the pes number
	 */
	public CloudletSchedulerDynamicWorkloadIops(double iops, double mips, int numberOfPes) {
		super();
		setIops(iops);
		setMips(mips);
		setNumberOfPes(numberOfPes);
		setTotalMips(getNumberOfPes() * getMips());
		setUnderAllocatedMips(new HashMap<String, Double>());
		setCachePreviousTime(-1);
	}

	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 * 
	 * @param currentTime current simulation time
	 * @param mipsShare array with MIPS share of each Pe available to the scheduler
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
	 *         no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare, Double iopsShare) {
		setCurrentMipsShare(mipsShare);
		setCurrentIopsShare(iopsShare);

		double timeSpan = currentTime - getPreviousTime();
		double nextEvent = Double.MAX_VALUE;
		List<ResCloudlet> cloudletsToFinish = new ArrayList<ResCloudlet>();
		
		for (ResCloudlet rcl : getCloudletExecList()) {
		/*	
						System.err.println(rcl.getCloudletId() + ") RemainingIops= " + rcl.getRemainingIopsCloudletLength());
			System.err.println(rcl.getCloudletId() + ") IopsFinishedSoFar= " + rcl.getCloudlet().getCloudletIopsFinishedSoFar());
			System.err.println(rcl.getCloudletId() + ") Iops To remove= " + ((long) (iopsCapacity * timeSpan)));
			System.err.println(rcl.getCloudletId() + ") RemainingMips= " + rcl.getRemainingCloudletLength());
			System.err.println(rcl.getCloudletId() + ") MipsFinishedSoFar= " + rcl.getCloudlet().getCloudletFinishedSoFar());
			System.err.println(rcl.getCloudletId() + ") Mips To remove= " + ((long) (getCapacity(mipsShare) * timeSpan * rcl.getNumberOfPes() * Consts.MILLION)));
			System.err.println(rcl.getCloudletId() + ") Timespan= " + timeSpan);
			/*try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/
			
			if (!Log.isDisabled()) {
				
				Log.vmUtilFormatLine("%.2f, " + this.getHostId() + ", " + rcl.getCloudlet().getVmId() + ", " + rcl.getCloudlet().getCloudletId() +
						", %d, %.3f, %d, %.3f", 
						getPreviousTime(),
						rcl.getRemainingIopsCloudletLength(),
						rcl.getCloudlet().getUtilizationOfIo(getPreviousTime()),
						rcl.getRemainingCloudletLength(),
						rcl.getCloudlet().getUtilizationOfCpu(getPreviousTime())
						);
				Log.formatLine(
						"%.2f: [Cloudlet #" + rcl.getCloudletId() + "] " 
								+ "RemainingIops: %d, IopsFinishedSoFar: %d, IopsToRemove: %.2f, "
								+ "RemainingMips: %d, MipsFinishedSoFar: %d, MipsToRemove: %.2f, Timespan: %.2f",
						CloudSim.clock(),
						rcl.getRemainingIopsCloudletLength(),
						rcl.getCloudlet().getCloudletIopsFinishedSoFar(),
						timeSpan * getCurrentAllocatedIopsForCloudlet(rcl, getPreviousTime()),
						rcl.getRemainingCloudletLength(),
						rcl.getCloudlet().getCloudletFinishedSoFar(),
						timeSpan * getTotalCurrentAllocatedMipsForCloudlet(rcl, getPreviousTime()),
						timeSpan);
			}
			
			rcl.updateCloudletIopsFinishedSoFar((long) (timeSpan * getCurrentAllocatedIopsForCloudlet(rcl, getPreviousTime())));
			double mipsToRemove = timeSpan * getTotalCurrentAllocatedMipsForCloudlet(rcl, getPreviousTime()) * Consts.MILLION;
			rcl.updateCloudletFinishedSoFar((long) mipsToRemove);
			if (rcl.getRemainingCloudletLength() == 0){
				rcl.getCloudlet().setUtilizationModelCpu(new UtilizationModelNull());
			}
			
			if (rcl.getRemainingIopsCloudletLength() == 0){
				rcl.getCloudlet().setUtilizationModelIo(new UtilizationModelNull());
			} else {
				UtilizationIops iopsUtilizationModel = ((UtilizationIops) rcl.getCloudlet().getUtilizationModelIo());
				iopsUtilizationModel.updateMipsLeft(mipsToRemove / Consts.MILLION);
				double mipsUtilization = rcl.getCloudlet().getUtilizationModelCpu().getUtilization(currentTime);
				iopsUtilizationModel.setMipsUtilization(mipsUtilization);
				iopsUtilizationModel.setAllocatedMips(getTotalCurrentAllocatedMipsForCloudlet(rcl, currentTime));
			}
			
			if (rcl.getRemainingCloudletLength() == 0 && rcl.getRemainingIopsCloudletLength() == 0) { // finished: remove from the list
				System.out.println(rcl.getCloudletId() + " cloudlet finished!");
				cloudletsToFinish.add(rcl);
				continue;
			} else { // not finish: estimate the finish time
				
				double estimatedFinishTime = getEstimatedFinishTime(rcl, currentTime);
				if (estimatedFinishTime - currentTime < 0.1) {
					estimatedFinishTime = currentTime + 0.1;
				}
				if (estimatedFinishTime < nextEvent) {
					nextEvent = estimatedFinishTime;
				}
			}
		}

		for (ResCloudlet rgl : cloudletsToFinish) {
			getCloudletExecList().remove(rgl);
			cloudletFinish(rgl);
		}

		setPreviousTime(currentTime);

		if (getCloudletExecList().isEmpty()) {
			return 0;
		}

		return nextEvent;
	}

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cl the cl
	 * @return predicted completion time
	 * @pre _gl != null
	 * @post $none
	 */
	@Override
	public double cloudletSubmit(Cloudlet cl) {
		return cloudletSubmit(cl, 0);
	}

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cl the cl
	 * @param fileTransferTime the file transfer time
	 * @return predicted completion time
	 * @pre _gl != null
	 * @post $none
	 */
	@Override
	public double cloudletSubmit(Cloudlet cl, double fileTransferTime) {
		ResCloudlet rcl = new ResCloudlet(cl);
		rcl.setCloudletStatus(Cloudlet.INEXEC);

		for (int i = 0; i < cl.getNumberOfPes(); i++) {
			rcl.setMachineAndPeId(0, i);
		}

		getCloudletExecList().add(rcl);
		double mipsUtilization = rcl.getCloudlet().getUtilizationOfCpu(getPreviousTime());
		((UtilizationIops) rcl.getCloudlet().getUtilizationModelIo()).setMipsUtilization(mipsUtilization);
		return getEstimatedFinishTime(rcl, getPreviousTime());
	}

	/**
	 * Processes a finished cloudlet.
	 * 
	 * @param rcl finished cloudlet
	 * @pre rgl != $null
	 * @post $none
	 */
	@Override
	public void cloudletFinish(ResCloudlet rcl) {
		rcl.setCloudletStatus(Cloudlet.SUCCESS);
		rcl.finalizeCloudlet();
		getCloudletFinishedList().add(rcl);
	}

	/**
	 * Get utilization created by all cloudlets.
	 * 
	 * @param time the time
	 * @return total utilization
	 */
	@Override
	public double getTotalUtilizationOfCpu(double time) {
		double totalUtilization = 0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			totalUtilization += rcl.getCloudlet().getUtilizationOfCpu(time);
		}
		return totalUtilization;
	}

	/**
	 * Gets the current mips.
	 * 
	 * @return the current mips
	 */
	@Override
	public List<Double> getCurrentRequestedMips() {
		if (getCachePreviousTime() == getPreviousTime()) {
			return getCacheCurrentRequestedMips();
		}
		List<Double> currentMips = new ArrayList<Double>();
		double totalMips = getTotalUtilizationOfCpu(getPreviousTime()) * getTotalMips();
		double mipsForPe = totalMips / getNumberOfPes();

		for (int i = 0; i < getNumberOfPes(); i++) {
			currentMips.add(mipsForPe);
		}

		Double requestedIops = getTotalUtilizationOfIo(getPreviousTime()) * getIops();
		
		setCachePreviousTime(getPreviousTime());
		setCacheCurrentRequestedMips(currentMips);
		setCacheCurrentRequestedIops(requestedIops);

		return currentMips;
	}

	public Double getCurrentRequestedIops(){
		if(getCachePreviousTime() == getPreviousTime()) {
			return getCacheCurrentRequestedIops();
		}
		
		Double requestedIops = getTotalUtilizationOfIo(getPreviousTime()) * getIops();
		
		List<Double> currentMips = new ArrayList<Double>();
		double totalMips = getTotalUtilizationOfCpu(getPreviousTime()) * getTotalMips();
		double mipsForPe = totalMips / getNumberOfPes();

		for (int i = 0; i < getNumberOfPes(); i++) {
			currentMips.add(mipsForPe);
		}

		
		setCachePreviousTime(getPreviousTime());
		setCacheCurrentRequestedIops(requestedIops);
		setCacheCurrentRequestedMips(currentMips);
		
		return requestedIops;
	}
	
	/**
	 * Gets the current mips.
	 * 
	 * @param rcl the rcl
	 * @param time the time
	 * @return the current mips
	 */
	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
		return rcl.getCloudlet().getUtilizationOfCpu(time) * getTotalMips();
	}

	/**
	 * Gets the total current mips for the clouddlet.
	 * 
	 * @param rcl the rcl
	 * @param mipsShare the mips share
	 * @return the total current mips
	 */
	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
		double totalCurrentMips = 0.0;
		if (mipsShare != null) {
			int neededPEs = rcl.getNumberOfPes();
			for (double mips : mipsShare) {
				totalCurrentMips += mips;
				neededPEs--;
				if (neededPEs <= 0) {
					break;
				}
			}
		}
		return totalCurrentMips;
	}

	/**
	 * Gets the current mips.
	 * 
	 * @param rcl the rcl
	 * @param time the time
	 * @return the current mips
	 */
	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
		double totalCurrentRequestedMips = getTotalCurrentRequestedMipsForCloudlet(rcl, time);
		double totalCurrentAvailableMips = getTotalCurrentAvailableMipsForCloudlet(rcl, getCurrentMipsShare());
		if (totalCurrentRequestedMips > totalCurrentAvailableMips) {
			return totalCurrentAvailableMips;
		}
		return totalCurrentRequestedMips;
	}

	/**
	 * Update under allocated mips for cloudlet.
	 * 
	 * @param rcl the rgl
	 * @param mips the mips
	 */
	public void updateUnderAllocatedMipsForCloudlet(ResCloudlet rcl, double mips) {
		if (getUnderAllocatedMips().containsKey(rcl.getUid())) {
			mips += getUnderAllocatedMips().get(rcl.getUid());
		}
		getUnderAllocatedMips().put(rcl.getUid(), mips);
	}

	/**
	 * Get estimated cloudlet completion time.
	 * 
	 * @param rcl the rcl
	 * @param time the time
	 * @return the estimated finish time
	 */
	public double getEstimatedFinishTime(ResCloudlet rcl, double time) {

		double remainingLength = rcl.getRemainingCloudletLength();
		double remainingIopsLength = rcl.getRemainingIopsCloudletLength();
		double estimatedMipsTime = remainingLength / getTotalCurrentAllocatedMipsForCloudlet(rcl, time);
		double estimatedIopsTime = remainingIopsLength;
		if (getCurrentAllocatedIopsForCloudlet(rcl,time) > 1){
			estimatedIopsTime = remainingIopsLength / getCurrentAllocatedIopsForCloudlet(rcl, time);
		}
			
		double estimatedFinishTime = time + ((estimatedIopsTime > estimatedMipsTime) ? estimatedIopsTime : estimatedMipsTime);  

		return estimatedFinishTime;
	}
	

	/**
	 * Gets the total current mips.
	 * 
	 * @return the total current mips
	 */
	public int getTotalCurrentMips() {
		int totalCurrentMips = 0;
		for (double mips : getCurrentMipsShare()) {
			totalCurrentMips += mips;
		}
		return totalCurrentMips;
	}

	/**
	 * Sets the total mips.
	 * 
	 * @param mips the new total mips
	 */
	public void setTotalMips(double mips) {
		totalMips = mips;
	}

	/**
	 * Gets the total mips.
	 * 
	 * @return the total mips
	 */
	public double getTotalMips() {
		return totalMips;
	}

	/**
	 * Sets the pes number.
	 * 
	 * @param pesNumber the new pes number
	 */
	public void setNumberOfPes(int pesNumber) {
		numberOfPes = pesNumber;
	}

	/**
	 * Gets the pes number.
	 * 
	 * @return the pes number
	 */
	public int getNumberOfPes() {
		return numberOfPes;
	}

	/**
	 * Sets the mips.
	 * 
	 * @param mips the new mips
	 */
	public void setMips(double mips) {
		this.mips = mips;
	}

	/**
	 * Gets the mips.
	 * 
	 * @return the mips
	 */
	public double getMips() {
		return mips;
	}

	/**
	 * Sets the under allocated mips.
	 * 
	 * @param underAllocatedMips the under allocated mips
	 */
	public void setUnderAllocatedMips(Map<String, Double> underAllocatedMips) {
		this.underAllocatedMips = underAllocatedMips;
	}

	/**
	 * Gets the under allocated mips.
	 * 
	 * @return the under allocated mips
	 */
	public Map<String, Double> getUnderAllocatedMips() {
		return underAllocatedMips;
	}

	/**
	 * Gets the cache previous time.
	 * 
	 * @return the cache previous time
	 */
	protected double getCachePreviousTime() {
		return cachePreviousTime;
	}

	/**
	 * Sets the cache previous time.
	 * 
	 * @param cachePreviousTime the new cache previous time
	 */
	protected void setCachePreviousTime(double cachePreviousTime) {
		this.cachePreviousTime = cachePreviousTime;
	}

	/**
	 * Gets the cache current requested mips.
	 * 
	 * @return the cache current requested mips
	 */
	protected List<Double> getCacheCurrentRequestedMips() {
		return cacheCurrentRequestedMips;
	}

	/**
	 * Sets the cache current requested mips.
	 * 
	 * @param cacheCurrentRequestedMips the new cache current requested mips
	 */
	protected void setCacheCurrentRequestedMips(List<Double> cacheCurrentRequestedMips) {
		this.cacheCurrentRequestedMips = cacheCurrentRequestedMips;
	}
	
	public double getCurrentRequestedIopsForCloudlet(ResCloudlet rcl, double time){
		return getIops() * rcl.getCloudlet().getUtilizationOfIo(time);
	}
	
	public double getCurrentAvailableIopsForCloudlet(ResCloudlet rcl, double time, double iopsShare){
		return iopsShare;
	}
	
	public double getCurrentAllocatedIopsForCloudlet(ResCloudlet rcl, double time){
		double availableIops = getCurrentAvailableIopsForCloudlet(rcl, time, getCurrentIopsShare());
		double requestedIops = getCurrentRequestedIopsForCloudlet(rcl, time);
		return (requestedIops < availableIops ? requestedIops : availableIops);
	}

	public double getTotalUtilizationOfIo(double time) {
		double totalUtilization = 0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			totalUtilization += rcl.getCloudlet().getUtilizationOfIo(time);
		}
		return totalUtilization;
	}

	public double getIops() {
		return iops;
	}

	public void setIops(double iops) {
		this.iops = iops;
	}

	public Double getCacheCurrentRequestedIops() {
		return cacheCurrentRequestedIops;
	}

	public void setCacheCurrentRequestedIops(Double cacheCurrentRequestedIops) {
		this.cacheCurrentRequestedIops = cacheCurrentRequestedIops;
	}
	
}
