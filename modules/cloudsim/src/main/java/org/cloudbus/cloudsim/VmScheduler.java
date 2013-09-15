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

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.IoProvisioner;

/**
 * VmScheduler is an abstract class that represents the policy used by a VMM to share processing
 * power among VMs running in a host.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmScheduler {

	/** The peList. */
	private List<? extends Pe> peList;

	/** The map of VMs to PEs. */
	private Map<String, List<Pe>> peMap;

	/** The MIPS that are currently allocated to the VMs. */
	private Map<String, List<Double>> mipsMap;

	/** The total available mips. */
	private double availableMips;
	
	/** Host's ioProvisioner */
	private IoProvisioner ioProvisioner;

	/** The IOPS that are currently allocated to the VMs. */
	private Map<String, Double> allocatedIopsMap;
	
	/** The IOPS that are currently requested by the VMs. */
	private Map<String, Double> requestedIopsMap;
	
	/** The VMs migrating in. */
	private List<String> vmsMigratingIn;

	/** The VMs migrating out. */
	private List<String> vmsMigratingOut;

	/**
	 * Creates a new HostAllocationPolicy.
	 * 
	 * @param pelist the pelist
	 * @pre peList != $null
	 * @post $none
	 */
	public VmScheduler(List<? extends Pe> pelist, IoProvisioner ioProvisioner) {
		setPeList(pelist);
		setPeMap(new HashMap<String, List<Pe>>());
		setMipsMap(new HashMap<String, List<Double>>());
		setIoProvisioner(ioProvisioner);
		setAllocatedIopsMap(new HashMap<String, Double>());
		setRequestedIopsMap(new HashMap<String, Double>());
		setAvailableMips(PeList.getTotalMips(getPeList()));
		setVmsMigratingIn(new ArrayList<String>());
		setVmsMigratingOut(new ArrayList<String>());
	}

	/**
	 * Allocates PEs for a VM.
	 * 
	 * @param vm the vm
	 * @param mipsShare the mips share
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocatePesForVm(Vm vm, List<Double> mipsShare);

	/**
	 * Releases PEs allocated to a VM.
	 * 
	 * @param vm the vm
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocatePesForVm(Vm vm);

	/**
	 * Releases PEs allocated to all the VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForAllVms() {
		getMipsMap().clear();
		setAvailableMips(PeList.getTotalMips(getPeList()));
		for (Pe pe : getPeList()) {
			pe.getPeProvisioner().deallocateMipsForAllVms();
		}
	}

	/**
	 * Gets the pes allocated for vm.
	 * 
	 * @param vm the vm
	 * @return the pes allocated for vm
	 */
	public List<Pe> getPesAllocatedForVM(Vm vm) {
		return getPeMap().get(vm.getUid());
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 * 
	 * @param vm the vm
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		return getMipsMap().get(vm.getUid());
	}
	
	/**
	 * Return the IOPS share that is allocated to a given VM.
	 * @param vm the vm
	 * @return the IOPS share that is available to the VM
	 */
	public Double getAllocatedIopsForVm(Vm vm){
		return getAllocatedIopsMap().get(vm.getUid());
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 * 
	 * @param vm the vm
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(Vm vm) {
		double allocated = 0;
		List<Double> mipsMap = getAllocatedMipsForVm(vm);
		if (mipsMap != null) {
			for (double mips : mipsMap) {
				allocated += mips;
			}
		}
		return allocated;
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		if (getPeList() == null) {
			Log.printLine("Pe list is empty");
			return 0;
		}

		double max = 0.0;
		for (Pe pe : getPeList()) {
			double tmp = pe.getPeProvisioner().getAvailableMips();
			if (tmp > max) {
				max = tmp;
			}
		}

		return max;
	}

	/**
	 * Returns PE capacity in MIPS.
	 * 
	 * @return mips
	 */
	public double getPeCapacity() {
		if (getPeList() == null) {
			Log.printLine("Pe list is empty");
			return 0;
		}
		return getPeList().get(0).getMips();
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pe> List<T> getPeList() {
		return (List<T>) peList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param peList the pe list
	 */
	protected <T extends Pe> void setPeList(List<T> peList) {
		this.peList = peList;
	}

	/**
	 * Gets the mips map.
	 * 
	 * @return the mips map
	 */
	protected Map<String, List<Double>> getMipsMap() {
		return mipsMap;
	}
	

	/**
	 * Gets the iops map.
	 * 
	 * @return the iops map
	 */
	protected Map<String, Double> getAllocatedIopsMap() {
		return allocatedIopsMap;
	}


	/**
	 * Sets the mips map.
	 * 
	 * @param mipsMap the mips map
	 */
	protected void setMipsMap(Map<String, List<Double>> mipsMap) {
		this.mipsMap = mipsMap;
	}

	/**
	 * Gets the available iops
	 */
	public int getIops(){
		return ioProvisioner.getIoBw();
	}
/*	
	/**
	 * Sets the available iops
	 * 
	 * @param iops the available iops
	 *
	protected void setIops(Double iops){
		this.iops = iops;
	}
*/	
	/**
	 * Gets the currently available iops
	 */
	public int getAvailableIops(){
		return ioProvisioner.getAvailableIoBw();
	}
/*	
	/**
	 * Sets the currently available iops
	 * 
	 * @param iops the currently available iops
	 *
	protected void setAvailableIops(Double iops){
		this.availableIops = iops;
	}
*/	

	/**
	 * Sets the ioProvisioner.
	 * 
	 * @param ioProvisioner the ioProvisioner
	 */
	protected void setIoProvisioner(IoProvisioner ioProvisioner) {
		this.ioProvisioner = ioProvisioner;
	}
	
	/**
	 * Sets the iops map.
	 * 
	 * @param iopsMap the iops map
	 */
	protected void setAllocatedIopsMap(Map<String, Double> iopsMap) {
		this.allocatedIopsMap = iopsMap;
	}
	
	/**
	 * Gets the free mips.
	 * 
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return availableMips;
	}

	/**
	 * Sets the free mips.
	 * 
	 * @param availableMips the new free mips
	 */
	protected void setAvailableMips(double availableMips) {
		this.availableMips = availableMips;
	}

	/**
	 * Gets the vms in migration.
	 * 
	 * @return the vms in migration
	 */
	public List<String> getVmsMigratingOut() {
		return vmsMigratingOut;
	}

	/**
	 * Sets the vms in migration.
	 * 
	 * @param vmsInMigration the new vms migrating out
	 */
	protected void setVmsMigratingOut(List<String> vmsInMigration) {
		vmsMigratingOut = vmsInMigration;
	}

	/**
	 * Gets the vms migrating in.
	 * 
	 * @return the vms migrating in
	 */
	public List<String> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Sets the vms migrating in.
	 * 
	 * @param vmsMigratingIn the new vms migrating in
	 */
	protected void setVmsMigratingIn(List<String> vmsMigratingIn) {
		this.vmsMigratingIn = vmsMigratingIn;
	}

	/**
	 * Gets the pe map.
	 * 
	 * @return the pe map
	 */
	public Map<String, List<Pe>> getPeMap() {
		return peMap;
	}

	/**
	 * Sets the pe map.
	 * 
	 * @param peMap the pe map
	 */
	protected void setPeMap(Map<String, List<Pe>> peMap) {
		this.peMap = peMap;
	}
	
	public boolean allocateIopsForVm(Vm vm, Double currentRequestedIops) {
		if(allocatedIopsMap.get(vm.getUid()) == null){
			allocatedIopsMap.put(vm.getUid(), 0.0);
			requestedIopsMap.put(vm.getUid(), currentRequestedIops);
		}
		
		double totalRequestedIops = 0.0;
		for (String vmId: requestedIopsMap.keySet()) {
			totalRequestedIops += requestedIopsMap.get(vmId);
		}
		Double iopsScaleFactor = (ioProvisioner.getIoBw() > totalRequestedIops) ? 1 : ioProvisioner.getIoBw() /(totalRequestedIops); 
		System.err.println("iopsScaleFactor: " + iopsScaleFactor );
		for (String vmId : allocatedIopsMap.keySet()) {
			System.err.println("Allocating for (" + vmId + ") " + iopsScaleFactor * requestedIopsMap.get(vmId));
			allocatedIopsMap.put(vmId, iopsScaleFactor * requestedIopsMap.get(vmId));
		}
		return true;
	}
	
	public void deallocateIopsForVm(Vm vm){
		getAllocatedIopsMap().remove(vm.getUid());
		getRequestedIopsMap().remove(vm.getUid());
	}
	
	public void deallocateIopsForAllVms(){
		getAllocatedIopsMap().clear();
		getRequestedIopsMap().clear();
	}

	public Map<String, Double> getRequestedIopsMap() {
		return requestedIopsMap;
	}

	public void setRequestedIopsMap(Map<String, Double> requestedIopsMap) {
		this.requestedIopsMap = requestedIopsMap;
	}

}
