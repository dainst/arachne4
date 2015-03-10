package de.uni_koeln.arachne.util.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions related to external processes.
 * 
 * @author Reimar Grabowski
 */
public class ExternalProcess {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProcess.class);

	/**
	 * Runs a command blocking. The commands StdIn and StdErr are logged.
	 * 
	 * @param command A string array containing the command and its arguments.
	 */
	public static void runBlocking(final String[] command) {
		runBlocking(command, null);
	}
	
	/**
	 * Runs a command blocking. The commands StdIn and StdErr are logged.
	 * 
	 * @param command A string array containing the command and its arguments.
	 * @param file The working directory name (absolute path).
	 */
	public static void runBlocking(final String[] command, final String directoryName) {
		final File directory = (directoryName != null) ? new File(directoryName) : null;
		BufferedReader input = null;
		BufferedReader error = null;

		try	{
			Process process;
			LOGGER.info("Start");
			if (directory != null)
				process = Runtime.getRuntime().exec(command, null, directory);
			else
				process = Runtime.getRuntime().exec(command);
			String line;
			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			LOGGER.info("Start input");
			while ((line = input.readLine()) != null) {
				LOGGER.info(line);
			}
			LOGGER.info("Start error");
			error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = error.readLine()) != null) {
				LOGGER.error(line);
			}

			final int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("Process '" + command + "finished.");
			} else {
				LOGGER.error("Process '" + command + "finished with Exit code: " + exitCode);
			}
		} catch (IOException e) {
			LOGGER.error("Process '" + command + "' has IO problems: ", e);
		} catch (InterruptedException e) {
			LOGGER.error("Process '" + command + "' got interrupted: ", e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close inpurt reader.");
				}
			if (error != null)
				try {
					error.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close error reader.");
				}
		}
	}

}
