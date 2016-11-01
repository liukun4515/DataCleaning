package com.thu.warehouse.datacleaning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;

public class Main {

	public static void main(String[] args) throws IOException {
		File file = new File("anti_2.txt");
		CSVReader csvReader = new CSVReader(new FileReader(file));
		String[] vales;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		int index = 1;
		while(bufferedReader.readLine() != null){
			System.out.println(index);
			index++;
		}

	}

}
