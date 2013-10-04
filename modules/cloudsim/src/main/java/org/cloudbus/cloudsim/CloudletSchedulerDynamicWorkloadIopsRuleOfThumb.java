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
public class CloudletSchedulerDynamicWorkloadIopsRuleOfThumb extends CloudletSchedulerDynamicWorkloadIops {

	
	/**
	 * Instantiates a new vM scheduler time shared.
	 * 
	 * @param mips the mips
	 * @param numberOfPes the pes number
	 */
	public CloudletSchedulerDynamicWorkloadIopsRuleOfThumb(double iops, double mips, int numberOfPes) {
		super(iops, mips, numberOfPes);
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
				e.printStackTrace();
			}
		*/
			
			if (!Log.isDisabled()) {
				
				Log.formatLine("vmUtil","%.2f, " + this.getHostId() + ", " + rcl.getCloudlet().getVmId() + ", " + rcl.getCloudlet().getCloudletId() +
						", %.3f, %d, %.3f, %d, %.3f", 
						getPreviousTime(),
						getCurrentIopsShare(),
						rcl.getRemainingIopsCloudletLength(),
						rcl.getCloudlet().getUtilizationOfIo(getPreviousTime()),
						rcl.getRemainingCloudletLength(),
						rcl.getCloudlet().getUtilizationOfCpu(getPreviousTime())
						);
				/*
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
				*/
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
				UtilizationIopsSimple iopsUtilizationModel = ((UtilizationIopsSimple) rcl.getCloudlet().getUtilizationModelIo());
				double mipsUtilization = rcl.getCloudlet().getUtilizationModelCpu().getUtilization(currentTime);
				iopsUtilizationModel.setMipsUtilization(mipsUtilization);
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
		UtilizationIopsSimple iopsUtilizationModel = ((UtilizationIopsSimple) rcl.getCloudlet().getUtilizationModelIo());
		iopsUtilizationModel.setMipsAvailable(getTotalMips());
		iopsUtilizationModel.setIopsAvailable(getIops());
		iopsUtilizationModel.setMipsUtilization(mipsUtilization);
		return getEstimatedFinishTime(rcl, getPreviousTime());
	}

}
