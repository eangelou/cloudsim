/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.Vm;

/**
 * RamProvisioner is an abstract class that represents the provisioning policy of memory to virtual
 * machines inside a Host. When extending this class, care must be taken to guarantee that the field
 * availableMemory will always contain the amount of free memory available for future allocations.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class IoProvisioner {

	/** The ram. */
	private int ioBw;

	/** The available ram. */
	private int availableIoBw;

	/**
	 * Creates the new IoProvisioner.
	 * 
	 * @param ioBw the ioBw
	 * 
	 * @pre ioBw>=0
	 * @post $none
	 */
	public IoProvisioner(int ioBw) {
		setIoBw(ioBw);
		setAvailableIoBw(ioBw);
	}

	/**
	 * Allocates ioBw for a given VM.
	 * 
	 * @param vm virtual machine for which the ioBw are being allocated
	 * @param ioBW the ioBw
	 * 
	 * @return $true if the ioBw could be allocated; $false otherwise
	 * 
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateIoBwForVm(Vm vm, int ioBw);

	/**
	 * Gets the allocated ioBw for VM.
	 * 
	 * @param vm the VM
	 * 
	 * @return the allocated ioBw for vm
	 */
	public abstract int getAllocatedIoBwForVm(Vm vm);

	/**
	 * Releases ioBw used by a VM.
	 * 
	 * @param vm the vm
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateIoBwForVm(Vm vm);

	/**
	 * Releases ioBw used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateIoBwForAllVms() {
		setAvailableIoBw(getIoBw());
	}

	/**
	 * Checks if is suitable for vm.
	 * 
	 * @param vm the vm
	 * @param ioBw the ioBw
	 * 
	 * @return true, if is suitable for vm
	 */
	public abstract boolean isSuitableForVm(Vm vm, int ioBw);

	/**
	 * Gets the ioBw.
	 * 
	 * @return the ioBw
	 */
	public int getIoBw() {
		return ioBw;
	}

	/**
	 * Sets the ioBw.
	 * 
	 * @param ioBw the ioBw to set
	 */
	protected void setIoBw(int ioBw) {
		this.ioBw = ioBw;
	}

	/**
	 * Gets the amount of used ioBw in the host.
	 * 
	 * @return used ioBw
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getUsedIoBw() {
		return ioBw - availableIoBw;
	}

	/**
	 * Gets the available ioBw in the host.
	 * 
	 * @return available ioBw
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getAvailableIoBw() {
		return availableIoBw;
	}

	/**
	 * Sets the available ioBw.
	 * 
	 * @param availableIoBw the availableIoBw to set
	 */
	protected void setAvailableIoBw(int availableIoBw) {
		this.availableIoBw = availableIoBw;
	}

}
