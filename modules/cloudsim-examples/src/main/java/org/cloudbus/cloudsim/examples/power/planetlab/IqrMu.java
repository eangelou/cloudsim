package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

import org.cloudbus.cloudsim.Log;

/**
 * A simulation of a heterogeneous power aware data center that applies the Inter Quartile Range
 * (IQR) VM allocation policy and Minimum Utilization (MU) VM selection policy.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
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
 * @since Jan 5, 2012
 */
public class IqrMu {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		boolean outputToFile = false;
		String outputFolder = "output";
		String workload = args[0];
		String vmAllocationPolicy = "iqr"; // Inter Quartile Range (IQR) VM allocation policy
		String vmSelectionPolicy = "mu"; // Minimum Utilization (MU) VM selection policy
		String parameter = "1.5"; // the safety parameter of the IQR policy
		String inputFolder = IqrMu.class.getClassLoader().getResource("workload/planetlab").getPath();
		String vmUtilHeader = "Time, Host Id, Vm Id, Cloudlet Id, iopsShare, Remaining Iops, Iops Util, Remaining Mips, Mips Util";
		Log.createOutput("vmUtil", vmAllocationPolicy + vmSelectionPolicy + "_" + workload + "vmUtil.log", vmUtilHeader);
		String hostUtilHeader = "Time, Host Id, Host Io Utilization, Host Iops unutilized, Host Cpu Utilization";
		Log.createOutput("hostUtil", vmAllocationPolicy + vmSelectionPolicy + "_" + workload + "hostUtil.log", hostUtilHeader);
		String migrationsHeader = "Time, Vm Id, Old Host Id, New Host Id, Time to completion";
		Log.createOutput("migrations", vmAllocationPolicy + vmSelectionPolicy + "_" + workload + "migration.log", migrationsHeader);

		new PlanetLabRunner(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
	}

}
