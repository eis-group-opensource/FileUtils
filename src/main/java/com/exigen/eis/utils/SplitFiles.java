/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to split huge sql file to smaller ones.</br>
 * Sql statements must be terminated with ');' in separate line.</br>
 * As program arguments following parameters needs to be passed: directory, file name to be split, maximum sql statements count in splitted files.</br>
 * Smaller files with given number of sql statmens will be generated in same directory, but with number and underscore added to splitted file name.</br>
 * For example:
 * </br>  Having program arguments: \test\dir lookupValue.sql 1000
 * </br>  Will generate files like: \test\dir\1_lookupValue.sql, \test\dir\2_lookupValue.sql and so on...
 *  
 * @author dstulgis
 */
public class SplitFiles {
	
	private static final Logger log = Logger.getLogger(SplitFiles.class.getName());

	public static void main(String[] args) {
		if (args.length != 3) {
			log.log(Level.SEVERE, "Expected 3 arguments: directory, file name and sql statements count in block.");
			System.exit(-1);
		}
		
		int blockSize = 0;
		try {
			blockSize = Integer.valueOf(args[2]);
			if (blockSize < 1) {
				log.log(Level.SEVERE, "Sql statements count in block must be greater than 0 and currently is {0}", new Object[]{blockSize});
				System.exit(-1);
			}
		} catch (NumberFormatException e) {
			log.log(Level.SEVERE, "Third argument is sql statements count and must be integer.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		BufferedReader reader = null;
		String path = args[0] + "/" + args[1];
		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "File is not available: {0}", new Object[]{path});
			e.printStackTrace();
			System.exit(-1);
		}
		
		BufferedWriter writer = null;
		
		try {
			long start = System.currentTimeMillis();
			String line;
			int fileNr = 1;
			int blockCount = 1;
			writer = new BufferedWriter(new FileWriter(args[0] + "/" + fileNr + "_" + args[1]));
			log.log(Level.INFO, "Appending sql statements to {0}_{1}", new Object[]{fileNr, args[1]});
			while ((line = reader.readLine()) != null) {
				writer.write(line + '\n');
				if (line.equals(");")) {
					if (blockCount == blockSize) {
						writer.write("COMMIT;\n");
						writer.flush();
						writer.close();
						fileNr++;
						writer = new BufferedWriter(new FileWriter(args[0] + "/" + fileNr + "_" + args[1]));
						log.log(Level.INFO, "Appending sql statements to {0}_{1}", new Object[]{fileNr, args[1]});
						blockCount = 1;
					}
					blockCount++;
				}
			}
			long timetaken = System.currentTimeMillis() - start;
			long minutes = timetaken / 60000;
			long seconds = (timetaken % 60000) / 1000;
			log.log(Level.INFO, "SplitFiles total time: " + (minutes != 0 ? minutes + " minutes and ": "") + seconds + " seconds");
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed splitting file: {0}", new Object[]{path});
			e.printStackTrace();
			System.exit(-1);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {}
			}
		}
	}

}
