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
import java.util.HashMap;
import java.util.Map;

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
	private static final String LOG_FOLDER = "/local/gspilio/";
	
	/** The output. */
	private static Map<String,OutputStream> outputMap = new HashMap<String,OutputStream>();
	private static OutputStream vmUtilizationOutput;

	/** The disable output flag. */
	private static boolean disabled;
	
	
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
	 * Prints the message.
	 * 
	 * @param message the message
	 */
	public static void print(String outputName, String message) {
		if (!isDisabled()) {
			try {
				getOutput(outputName).write(message.getBytes());
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
	 * Prints the message passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void print(String outputName, Object message) {
		if (!isDisabled()) {
			print(outputName, String.valueOf(message));
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
	 * Prints the line.
	 * 
	 * @param message the message
	 */
	public static void printLine(String outputName, String message) {
		if (!isDisabled()) {
			print(outputName, message + LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the empty line.
	 */
	public static void printEmptyLine() {
		if (!isDisabled()) {
			print(LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the empty line.
	 */
	public static void printEmptyLine(String outputName) {
		if (!isDisabled()) {
			print(outputName, LINE_SEPARATOR);
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
	 * Prints the line passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void printLine(String outputName, Object message) {
		if (!isDisabled()) {
			printLine(outputName, String.valueOf(message));
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
	 * Prints a string formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void format(String outputName, String format, Object... args) {
		if (!isDisabled()) {
			print(outputName, String.format(format, args));
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
	 * Prints a line formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void formatLine(String outputName, String format, Object... args) {
		if (!isDisabled()) {
			printLine(outputName, String.format(format, args));
		}
	}

	/**
	 * Gets the output with key default.
	 * 
	 * @return the output
	 */
	public static OutputStream getOutput() {
		if (outputMap.get("default") == null) {
			createOutput("default",System.out);
		}
		return getOutput("default");
	}
	
	/**
	 * Gets from outputMap the output with key outputName
	 * 
	 * @param outputName the key for the output
	 * @return the output
	 */
	public static OutputStream getOutput(String outputName) {
		OutputStream output = outputMap.get(outputName);
		if (output == null){
			System.err.println("Unknown output '" + outputName + "' requested!\n Creating the requested output!");
			createOutput(outputName);
			return outputMap.get(outputName);
		}
		return output;
	}
	
	/**
	 * Adds output to outputMap with key outputName
	 * 
	 * @param outputName the key for the output
	 * @param output the output to be added
	 */
	public static void createOutput(OutputStream output){
		outputMap.put("default", output);
	}
	
	/**
	 * Adds output to outputMap with key outputName
	 * 
	 * @param outputName the key for the output
	 * @param output the output to be added
	 */
	public static void createOutput(String outputName, OutputStream output){
		outputMap.put(outputName, output);
	}
	
	/**
	 * Creates log file with name outputName to LOG_FOLDER. Creates new output to that file and
	 * add this output to outputMap with key outputName
	 * 
	 * @param outputName the key for the output (also the name of the file)
	 */
	public static void createOutput(String outputName){
		createOutput(outputName, outputName);
	}
	
	/**
	 * Creates log file with name outputName to LOG_FOLDER. Creates new output to that file and
	 * add this output to outputMap with key outputName
	 * 
	 * @param outputName the key for the output
	 * @param outputFileName the name of the log file.
	 */
	public static void createOutput(String outputName, String outputFileName){
		FileOutputStream out;
		PrintStream ps;
		File file = null;
		try {
			file = new File(LOG_FOLDER + outputFileName);
			if (!file.exists()){
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			ps = new PrintStream(out);
			outputMap.put(outputName, ps);
		} catch (Exception e){
			System.err.println("Failed to initialize " + LOG_FOLDER + outputFileName + " log file.\n\n" +
					e.getMessage() + "\n\n" + e.getStackTrace() );
			System.exit(0);
		}
	}
	
	/**
	 * Creates log file with name outputName to LOG_FOLDER. Creates new output to that file and
	 * add this output to outputMap with key outputName
	 * 
	 * @param outputName the key for the output
	 * @param outputFileName the name of the log file.
	 * @param header the header (first line) of the log file
	 */
	public static void createOutput(String outputName, String outputFileName, String header){
		createOutput(outputName, outputFileName);
		printLine(outputName, header);
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


}