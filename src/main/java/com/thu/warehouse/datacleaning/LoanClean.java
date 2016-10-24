package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.thu.warehouse.datacleaning.util.DataCleanException;

public class LoanClean {

	private static final String[] schema = {"loan_id","account_id","date","amount","duration","payments","status","payduration"};
	
	private static final String origin = "";
	private static final String clean = "";
	private static final String error = "";
	private static final String account = "";
	
	
	public static void clean(String input,String clean,String error) throws DataCleanException, IOException{
		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		File accountFile = new File(account);
		
		if (!inputFile.exists()||!accountFile.exists()) {
			System.out.println("The input file and account file is not exist");
			throw new DataCleanException("The input file and account file is not exist");
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		
		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader accountFileReader = new CSVReader(new FileReader(accountFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile));
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile));
		
		String[] values;
		
	}
	
	public static void main(String[] args) {
		
	}
}
