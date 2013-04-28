/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * IoProvisionerSimple is an extension of IoProvisioner which uses a best-effort policy to
 * allocate ioBw to a VM.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class IoProvisionerSimple extends IoProvisioner {

	/** The IoBw table. */
	private Map<String, Integer> IoBwTable;

	/**
	 * Instantiates a new Io provisioner simple.
	 * 
	 * @param availableIo the available IoBw
	 */
	public IoProvisionerSimple(int availableIoBw) {
		super(availableIoBw);
		setIoBwTable(new HashMap<String, Integer>());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#allocateIoBwForVm(cloudsim.Vm, int)
	 */
	@Override
	public boolean allocateIoBwForVm(Vm vm, int IoBw) {
		int maxIoBw = vm.getIoBw();

		if (IoBw >= maxIoBw) {
			IoBw = maxIoBw;
		}

		deallocateIoBwForVm(vm);

		if (getAvailableIoBw() >= IoBw) {
			setAvailableIoBw(getAvailableIoBw() - IoBw);
			getIoBwTable().put(vm.getUid(), IoBw);
			vm.setCurrentAllocatedIoBw(getAllocatedIoBwForVm(vm));
			return true;
		}

		vm.setCurrentAllocatedIoBw(getAllocatedIoBwForVm(vm));

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#getAllocatedIoBwForVm(cloudsim.Vm)
	 */
	@Override
	public int getAllocatedIoBwForVm(Vm vm) {
		if (getIoBwTable().containsKey(vm.getUid())) {
			return getIoBwTable().get(vm.getUid());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#deallocateIoBwForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocateIoBwForVm(Vm vm) {
		if (getIoBwTable().containsKey(vm.getUid())) {
			int amountFreed = getIoBwTable().remove(vm.getUid());
			setAvailableIoBw(getAvailableIoBw() + amountFreed);
			vm.setCurrentAllocatedIoBw(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#deallocateIoBwForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocateIoBwForAllVms() {
		super.deallocateIoBwForAllVms();
		getIoBwTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#isSuitableForVm(cloudsim.Vm, int)
	 */
	@Override
	public boolean isSuitableForVm(Vm vm, int ram) {
		int allocatedIoBw = getAllocatedIoBwForVm(vm);
		boolean result = allocateIoBwForVm(vm, ram);
		deallocateIoBwForVm(vm);
		if (allocatedIoBw > 0) {
			allocateIoBwForVm(vm, allocatedIoBw);
		}
		return result;
	}

	/**
	 * Gets the IoBw table.
	 * 
	 * @return the IoBw table
	 */
	protected Map<String, Integer> getIoBwTable() {
		return IoBwTable;
	}

	/**
	 * Sets the IoBw table.
	 * 
	 * @param IoBwTable the IoBw table
	 */
	protected void setIoBwTable(Map<String, Integer> IoBwTable) {
		this.IoBwTable = IoBwTable;
	}

}
