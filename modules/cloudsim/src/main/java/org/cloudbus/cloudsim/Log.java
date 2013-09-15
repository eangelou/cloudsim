/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.File;

/**
 * The Log class used for performing loggin of the simulation process. It provides the ability to
 * substitute the output stream by any OutputStream subclass.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class Log {

	/** The Constant LINE_SEPARATOR. */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** The output. */
	private static OutputStream output;
	private static OutputStream vmUtilizationOutput;

	/** The disable output flag. */
	private static boolean disabled;
	
	private static String vmUtilHeader = "Time, Host Id, Vm Id, Cloudlet Id, Remaining Iops, Iops Util, Remaining Mips, Mips Util";

	/**
	 * Prints the message.
	 * 
	 * @param message the message
	 */
	public static void print(String message) {
		if (!isDisabled()) {
			try {
				getOutput().write(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints the message passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void print(Object message) {
		if (!isDisabled()) {
			print(String.valueOf(message));
		}
	}

	/**
	 * Prints the line.
	 * 
	 * @param message the message
	 */
	public static void printLine(String message) {
		if (!isDisabled()) {
			print(message + LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the empty line.
	 */
	public static void printLine() {
		if (!isDisabled()) {
			print(LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the line passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void printLine(Object message) {
		if (!isDisabled()) {
			printLine(String.valueOf(message));
		}
	}

	/**
	 * Prints a string formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void format(String format, Object... args) {
		if (!isDisabled()) {
			print(String.format(format, args));
		}
	}

	/**
	 * Prints a line formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void formatLine(String format, Object... args) {
		if (!isDisabled()) {
			printLine(String.format(format, args));
		}
	}

	/**
	 * Sets the output.
	 * 
	 * @param _output the new output
	 */
	public static void setOutput(OutputStream _output) {
		output = _output;
	}

	/**
	 * Gets the output.
	 * 
	 * @return the output
	 */
	public static OutputStream getOutput() {
		if (output == null) {
			setOutput(System.out);
		}
		return output;
	}

	/**
	 * Sets the disable output flag.
	 * 
	 * @param _disabled the new disabled
	 */
	public static void setDisabled(boolean _disabled) {
		disabled = _disabled;
	}

	/**
	 * Checks if the output is disabled.
	 * 
	 * @return true, if is disable
	 */
	public static boolean isDisabled() {
		return disabled;
	}

	/**
	 * Disables the output.
	 */
	public static void disable() {
		setDisabled(true);
	}

	/**
	 * Enables the output.
	 */
	public static void enable() {
		setDisabled(false);
	}


	////////////////////////////////////////////////////////////////////////////////

	/**
	 * Prints the message.
	 * 
	 * @param message the message
	 */
	public static void vmUtilPrint(String message) {
		if (!isDisabled()) {
			try {
				getVmUtilOutput().write(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints the message passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void vmUtilPrint(Object message) {
		if (!isDisabled()) {
			vmUtilPrint(String.valueOf(message));
		}
	}

	/**
	 * Prints the line.
	 * 
	 * @param message the message
	 */
	public static void vmUtilPrintLine(String message) {
		if (!isDisabled()) {
			vmUtilPrint(message + LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the empty line.
	 */
	public static void vmUtilPrintLine() {
		if (!isDisabled()) {
			vmUtilPrint(LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the line passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void vmUtilPrintLine(Object message) {
		if (!isDisabled()) {
			vmUtilPrintLine(String.valueOf(message));
		}
	}

	/**
	 * Prints a string formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void vmUtilFormat(String format, Object... args) {
		if (!isDisabled()) {
			vmUtilPrint(String.format(format, args));
		}
	}

	/**
	 * Prints a line formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void vmUtilFormatLine(String format, Object... args) {
		if (!isDisabled()) {
			vmUtilPrintLine(String.format(format, args));
		}
	}

	/**
	 * Sets the vmUtilizationOutput.
	 * 
	 * @param _output the new output
	 */
	public static void setVmUtilOutput(OutputStream _output) {
		vmUtilizationOutput = _output;
		vmUtilPrintLine(vmUtilHeader);
	}
	
	
	/**
	 * Sets the vmUtilizationOutput.
	 * If the file can not be created terminate.
	 * 
	 * @param output the new output
	 */
	public static void setVmUtilOutput(String vmUtilizationOutput) {
		FileOutputStream out;
		PrintStream ps;
		File file = null;
		try {
			file = new File(vmUtilizationOutput);
			if (!file.exists()){
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			ps = new PrintStream(out);
			setVmUtilOutput(ps);
		} catch (Exception e){
			System.err.println("Failed to initialize vmUtilizationLog.");
			System.exit(0);
		}
	}
	
	/**
	 * Gets the output.
	 * 
	 * @return the output
	 */
	public static OutputStream getVmUtilOutput() {
		if (vmUtilizationOutput == null) {
			FileOutputStream out;
			PrintStream ps;
			File file = null;
			try {
				file = new File("vmUtilization");
				if (!file.exists()){
					file.createNewFile();
				}
				out = new FileOutputStream(file);
				ps = new PrintStream(out); 
				setVmUtilOutput(ps);
			} catch (Exception e){
				System.err.println("Failed to initialize vmUtilizationLog.");
				System.exit(0);
			}
			System.err.println("VM utilization LOG: " + file.getAbsolutePath());
		}
		return vmUtilizationOutput;
	}


}
