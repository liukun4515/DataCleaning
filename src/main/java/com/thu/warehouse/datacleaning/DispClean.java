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

public class DispClean {

	enum ErrorType {
		DISP_ID_ERROR, CLIENT_ID_ERROR, ACCOUNT_ID_ERROR, TYPE_ERROR, LENGTH_ERROR, DISP_ID_UNIQUE_ERROR, CLIENT_ID_NOT_FOUNT_ERROR, ACCOUNT_ID_NOT_FOUNT_ERROR, TYPE_NOT_FOUNT_ERROR
	}

	private static final String OWNER = "OWNER";
	private static final String DISPONENT = "DISPONENT";

	private static final String[] schema = { "disp_id", "client_id", "account_id", "type" };
	private static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.STRING };

	private static final String origin = "disp.csv";
	private static final String client = "clientClean.csv";
	private static final String account = "accountClean.csv";
	private static final String clean = "dispClean.csv";
	private static final String error = "dispError.csv";

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {
		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		File clientFile = new File(client);
		File accountFile = new File(account);

		if (!inputFile.exists() || !clientFile.exists() || !accountFile.exists()) {
			System.out.println("The input file or client file or account file is not exits");
			throw new DataCleanException("The input file or client file or account file is not exits");
		}
		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader clientFileReader = new CSVReader(new FileReader(clientFile));
		CSVReader accountFileReader = new CSVReader(new FileReader(accountFile));
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);

		String[] values;
		String[] errorSchema = new String[schema.length + 1];
		Set<Integer> disp_id_set = new HashSet<>();
		Set<Integer> client_id_set = new HashSet<>();
		Set<Integer> account_id_set = new HashSet<>();

		// add client id set
		clientFileReader.readNext();
		List<String[]> list = clientFileReader.readAll();
		for (String[] strings : list) {
			client_id_set.add(Integer.valueOf(strings[0]));
		}
		// add account id set
		accountFileReader.readNext();
		List<String[]> list1 = accountFileReader.readAll();
		for (String[] strings : list1) {
			account_id_set.add(Integer.valueOf(strings[0]));
		}
		// the schema
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
			// read value
			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				if (values.length != schema.length) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the disp id
				String disp_idString = values[0];
				if (disp_idString != null && disp_idString.matches("^[1-9]\\d*$")) {
					int disp_idInt = Integer.valueOf(disp_idString);
					if (disp_id_set.contains(disp_idInt)) {
						errorInfo = errorInfo + ErrorType.DISP_ID_UNIQUE_ERROR + " ";
					} else {
						disp_id_set.add(disp_idInt);
					}
				} else {
					errorInfo = errorInfo + ErrorType.DISP_ID_ERROR + " ";
				}
				// check the client id
				String client_idString = values[1];
				if (client_idString != null && client_idString.matches("^[1-9]\\d*$")) {
					int client_idInt = Integer.valueOf(client_idString);
					if (client_id_set.contains(client_idInt)) {

					} else {
						errorInfo = errorInfo + ErrorType.CLIENT_ID_NOT_FOUNT_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.CLIENT_ID_ERROR + " ";
				}
				// check the account id
				String account_idString = values[2];
				if (account_idString != null && account_idString.matches("^[1-9]\\d*$")) {
					int account_idInt = Integer.valueOf(account_idString);
					if (account_id_set.contains(account_idInt)) {

					} else {
						errorInfo = errorInfo + ErrorType.ACCOUNT_ID_NOT_FOUNT_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.ACCOUNT_ID_ERROR + " ";
				}
				// check the type
				String typeString = values[3];
				if (typeString != null) {
					if (OWNER.equals(typeString) || DISPONENT.equals(typeString)) {

					} else {
						errorInfo = errorInfo + ErrorType.TYPE_NOT_FOUNT_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.TYPE_ERROR + " ";
				}

				if (errorInfo.length() > 0) {
					System.out.println("The error id " + values[0]);
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					System.out.println("The clean id " + values[0]);
					cleanFileWriter.writeNext(values);
				}
			}
			inputFileReader.close();
			clientFileReader.close();
			accountFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
		} else {
			inputFileReader.close();
			clientFileReader.close();
			accountFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema is not mapping");
		}
	}

	public static void main(String[] args) throws DataCleanException, IOException {
		clean(origin, clean, error);
	}

}
