package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws FileNotFoundException {
		String filename = "account.csv";
		File file = new File(filename);
		FileInputStream fileInputStream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(fileInputStream);
		System.out.println(reader.getEncoding());
	}
}
