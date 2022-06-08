/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It is used to cleanup *.txt file type generated data files by sql-workbench tool that contains only column names and no data.
 *  
 * @author dstulgis
 */
public class DeleteEmptyFiles {

	private static final String VALID_SUFFIX = ".txt";
	private static final Logger log = Logger.getLogger(DeleteEmptyFiles.class.getName());
	
	private static List<String> removedFileNames = new ArrayList<String>();
	
	public static void main(String[] args) {
		if (args.length != 1) {
			log.log(Level.SEVERE, "Expected 1 argument: directory.");
			System.exit(-1);
		}
		File baseDir = new File(args[0]);
		if (baseDir.isFile()) {
			log.log(Level.WARNING, "Given argument is not a directory and only one file will be scanned for removal: {}", args[0]);
		}
		
		recursiveDelete(baseDir);
		log.log(Level.INFO, "Removed empty data files: {0}", removedFileNames);
	}

	private static void recursiveDelete(File currentFile) {
		if (currentFile.isDirectory()) {
			for (File file : currentFile.listFiles()) {
				recursiveDelete(file);
			}
			return;
		}
		
		if (!currentFile.getName().endsWith(VALID_SUFFIX)) {
			return;
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(currentFile));
			String line;
			long linesCount = 0;
			while (((line = reader.readLine()) != null) && linesCount < 2) {
				if (line.isEmpty()) {
					continue;
				}
				linesCount++;
			}
			reader.close();
			// Delete file if it contains only one line - table headers
			if (linesCount == 1) {
				removedFileNames.add(currentFile.getName());
				currentFile.delete();
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed checking file if it is empty.", e);
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e2) {
					log.log(Level.SEVERE, "Failed closing file.", e2);
				}
			}
		}
	}

}
