package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException {
		String filename = "account.csv";
		File file = new File(filename);
		CSVReader reader = new CSVReader(new FileReader(file));
		reader = new CSVReader(new FileReader(file), 142, reader.getParser());
		System.out.println(reader.getLinesRead());
		System.out.println(reader.getRecordsRead());
		System.out.println(reader.getSkipLines());

		for (String string : reader.readNext()) {
			System.out.print(string + " ");
		}
		System.out.println("");
		System.out.println(reader.getLinesRead());
		System.out.println(reader.getRecordsRead());
		System.out.println(reader.getSkipLines());
	}
}
