package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.thu.warehouse.datacleaning.util.DataCleanException;
import com.thu.warehouse.datacleaning.util.DataType;

public class OrderClean {

	enum ErrorType {
		ORDER_ID_ERROR, ACCOUNT_ID_ERROR, BANK_TO_ERROR, ACCOUNT_TO_ERROR, AMOUNT_ERROR, K_SYMBOL_ERROR, LENGTH_ERROR, ORDER_ID_UNIQUE_ERROR, ACCOUNT_ID_NOT_FOUND_ERROR
	}

	private static final String[] schema = { "order_id", "account_id", "bank_to", "account_to", "amount", "k_symbol" };
	private static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.STRING, DataType.INT,
			DataType.INT, DataType.STRING };

	private static final String origin = "order.csv";
	private static final String clean = "orderClean.csv";
	private static final String error = "orderError.csv";
	private static final String account = "accountClean.csv";

	private static final String POJISTNE = "POJISTNE";
	private static final String SIPO = "SIPO";
	private static final String UVER = "UVER";
	private static final String LEASING = "LEASING";

	public static void clean(String input, String clean, String error) throws IOException, DataCleanException {
		File inputFile = new File(input);
		File accountFile = new File(account);
		File cleanFile = new File(clean);
		File errorFile = new File(error);

		if (!inputFile.exists() || !accountFile.exists()) {
			System.out.println(
					"The input file and account file is not exist" + inputFile.getName() + " " + accountFile.getName());
			throw new DataCleanException("The input file and account file is not exist");
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}
		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader accountFileReader = new CSVReader(new FileReader(accountFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile));
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile));

		String[] values;
		String[] errorSchema = new String[schema.length + 1];
		values = inputFileReader.readNext();

		if (values.length == schema.length) {
			for (int i = 0; i < schema.length; i++) {
				errorSchema[i] = schema[i];
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema is not mapping");
				}
			}
			errorSchema[errorSchema.length - 1] = "error_info";
			cleanFileWriter.writeNext(schema);
			errorFileWriter.writeNext(errorSchema);

			Set<Integer> account_id_set = new HashSet<>();
			Set<Integer> order_id_set = new HashSet<>();
			// get the account map
			accountFileReader.readNext();
			List<String[]> list = accountFileReader.readAll();
			for (String[] strings : list) {
				account_id_set.add(Integer.valueOf(strings[0]));
			}

			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				if (values.length != schema.length) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the order id
				String order_idString = values[0];
				if (order_idString != null && order_idString.matches("^[1-9]\\d*$")) {
					System.out.println("Math the id is " + order_idString);
					int order_idInt = Integer.valueOf(order_idString);
					if (order_id_set.contains(order_idInt)) {
						errorInfo = errorInfo + ErrorType.ORDER_ID_UNIQUE_ERROR + " ";
					} else {
						order_id_set.add(order_idInt);
					}
				} else {
					errorInfo = errorInfo + ErrorType.ORDER_ID_ERROR + " ";
				}
				// check the account id , foreign key
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
				// check the bank to check zimu
				String bank_toString = values[2];
				if (bank_toString != null && bank_toString.length() == 2 && bank_toString.matches("[A-Z][A-Z]")) {

				} else {
					errorInfo = errorInfo + ErrorType.BANK_TO_ERROR + " ";
				}
				// check account to
				String account_toString = values[3];
				if (account_toString != null && account_toString.matches("^[1-9]\\d*$")) {

				} else {
					errorInfo = errorInfo + ErrorType.ACCOUNT_TO_ERROR + " ";
				}
				// check amount to
				String amountString = values[4];
				if (amountString != null && amountString.matches("^[1-9]\\d*$")) {

				} else {
					errorInfo = errorInfo + ErrorType.AMOUNT_ERROR;
				}
				// check k_sybol
				String k_symbol = values[5];
				if (k_symbol != null) {
					if (k_symbol.equals(" ") || k_symbol.equals(LEASING) || k_symbol.equals(UVER) || k_symbol.equals(SIPO)
							|| k_symbol.equals(POJISTNE)) {
					} else {
						errorInfo = errorInfo + ErrorType.K_SYMBOL_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.K_SYMBOL_ERROR + " ";
				}
				if (errorInfo.length() > 0) {
					// write error values
					System.out.println("The error id is " + values[0]);
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					// write clean values
					System.out.println("The clean id is " + values[0]);
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

	public static void main(String[] args) throws IOException, DataCleanException {
		clean(origin, clean, error);
	}
}
