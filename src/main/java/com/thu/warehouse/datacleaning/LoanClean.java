package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.thu.warehouse.datacleaning.util.DataCleanException;

public class LoanClean {

	enum ErrorType {
		LOAD_ID_ERROR, ACCOUNT_ID_ERROR, DATA_ERROR, AMOUNT_ERROR, DURATION_ERROR, PAYMENTS_ERROR, STATUS_ERROR, PAYDUARTION_ERROR, LENGTH_ERROR, LOAD_ID_UNIQUE_ERROR, ACCOUNT_ID_NOT_FOUND_ERROR
	}

	private static final String[] schema = { "loan_id", "account_id", "date", "amount", "duration", "payments",
			"status", "payduration" };

	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private static final String D = "D";

	private static final String origin = "loan.csv";
	private static final String clean = "loanClean.csv";
	private static final String error = "loanError.csv";
	private static final String account = "accountClean.csv";

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {
		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		File accountFile = new File(account);

		if (!inputFile.exists() || !accountFile.exists()) {
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
		Set<Integer> account_id_set = new HashSet<>();
		String[] errorSchema = new String[schema.length + 1];
		Set<Integer> load_id_set = new HashSet<>();
		
		accountFileReader.readNext();
		List<String[]> list = accountFileReader.readAll();
		for (String[] strings : list) {
			account_id_set.add(Integer.valueOf(strings[0]));
		}
		
		values = inputFileReader.readNext();
		if (values.length == schema.length) {
			for (int i = 0; i < schema.length; i++) {
				errorSchema[i] = schema[i];
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema is not mapping");
				}
				errorSchema[errorSchema.length - 1] = "error_info";
			}
			cleanFileWriter.writeNext(schema);
			errorFileWriter.writeNext(errorSchema);
			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				if (values.length == schema.length) {
					// check the load id
					String load_idString = values[0];
					if (load_idString != null && load_idString.matches("^[1-9]\\d*$")) {
						int load_idInt = Integer.valueOf(load_idString);
						if (load_id_set.contains(load_idInt)) {
							errorInfo = errorInfo + ErrorType.LOAD_ID_UNIQUE_ERROR + " ";
						} else {
							load_id_set.add(load_idInt);
						}
					} else {
						errorInfo = errorInfo + ErrorType.LOAD_ID_ERROR + " ";
					}
					// check account
					String account_idString = values[1];
					if (account_idString != null && account_idString.matches("^[1-9]\\d*$")) {
						int account_idInt = Integer.valueOf(account_idString);
						if (account_id_set.contains(account_idInt)) {

						} else {
							errorInfo = errorInfo + ErrorType.ACCOUNT_ID_NOT_FOUND_ERROR + " ";
						}
					} else {
						errorInfo = errorInfo + ErrorType.ACCOUNT_ID_ERROR + " ";
					}
					// check the data
					String dataString = values[2];
					if (dataString != null) {
						SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						try {
							format.parse(dataString);
						} catch (ParseException e) {
							errorInfo = errorInfo + ErrorType.DATA_ERROR + " ";
						}
					} else {
						errorInfo = errorInfo + ErrorType.DATA_ERROR + " ";
					}
					// check amount
					String amountString = values[3];
					int amountInt =0;
					if (amountString != null && amountString.matches("^[1-9]\\d*$")) {
						amountInt = Integer.valueOf(amountString);
					} else {
						errorInfo = errorInfo + ErrorType.AMOUNT_ERROR + " ";
					}

					// check the duration
					String durationString = values[4];
					int durationInt = 0;
					if (durationString != null && durationString.matches("^[1-9]\\d*$")) {
						durationInt = Integer.valueOf(durationString);
						if (durationInt%12==0) {
							
						}else{
							errorInfo = errorInfo+ErrorType.DURATION_ERROR+" ";
						}
					} else {
						errorInfo = errorInfo + ErrorType.DURATION_ERROR + " ";
					}

					// check the payments
					
					String paymentsString = values[5];
					int paymentsInt = 0;
					if (paymentsString != null && paymentsString.matches("^[1-9]\\d*$")) {
						paymentsInt = Integer.valueOf(paymentsString);
						if (amountInt!=paymentsInt*durationInt) {
							errorInfo = errorInfo +ErrorType.PAYMENTS_ERROR+" ";	
						}else{
							
						}
					} else {
						errorInfo = errorInfo+ErrorType.PAYMENTS_ERROR+" ";
					}

					// check the status
					String statusString = values[6];
					if (statusString != null && (statusString.equals(A) || statusString.equals(B)
							|| statusString.equals(C) || statusString.equals(D))) {

					}else {
						errorInfo = errorInfo +ErrorType.STATUS_ERROR+" ";
					}

					// check the payduration
					String paydurationString = values[7];
					int paydurationInt;
					if (paydurationString!=null&&paydurationString.matches("^[0-9]\\d*$")) {
						paydurationInt = Integer.valueOf(paydurationString);
						if (paydurationInt<=durationInt) {
							
						}else{
							errorInfo = errorInfo+ErrorType.PAYDUARTION_ERROR+" ";
						}
					}else{
						errorInfo = errorInfo +ErrorType.PAYDUARTION_ERROR+" ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				
				if (errorInfo.length()>0) {
					System.out.println("The error id is "+values[0]);
					values = Arrays.copyOf(values, values.length+1);
					values[values.length-1] = errorInfo;
					errorFileWriter.writeNext(values);
				}else{
					System.out.println("The clean id is "+values[0]);
					cleanFileWriter.writeNext(values);
				}

			}
			inputFileReader.close();
			accountFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
		} else {
			inputFileReader.close();
			accountFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema is not mapping");
		}

	}
	public static void main(String[] args) throws DataCleanException, IOException {
		clean(origin,clean,error);
	}
}
