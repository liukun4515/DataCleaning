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
import com.thu.warehouse.datacleaning.util.DataType;

public class AccountClean {
	/*
	 * 这个表主要清洗的内容： 1.account_id key value,not null, unique 2.district_id foreign
	 * key 3.frequency ,enum 类型:POPLATEK MESICNE,POPLATEK TYDNE,POPLATEK PO
	 * OBRATU 4.data Date格式??
	 */

	enum ErrorType {
		ACCOUNT_ID_ERROR, DISTRICT_ID_ERROR, FREQUENCY_ERROR, DATA_ERROR, LENGTH_ERROR, ACCOUNT_ID_UNIQUE_ERROR, DISTRICT_ID_NOT_FOUNT_ERROR
	}

	private static final String MESICNE = "POPLATEK MESICNE";
	private static final String TYDNE = "POPLATEK TYDNE";
	private static final String OBRATU = "POPLATEK PO OBRATU";
	private static final String originFile = "account.csv";
	private static final String cleanDistrictFile = "districtClean.csv";
	private static final String cleanFile = "accountClean.csv";
	private static final String errorFile = "accountError.csv";

	private static final String[] schema = { "account_id", "district_id", "frequency", "date" };
	private static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.STRING, DataType.DATA };

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {
		// construct the file
		File inputFile = new File(input);
		File districtFile = new File(cleanDistrictFile);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		if (!inputFile.exists() || !districtFile.exists()) {
			System.out.println("The input file or district file is not exist");
			throw new DataCleanException("The input file and district file is not exist");
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}
		// construct the csv reader and writer
		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader districtFileReader = new CSVReader(new FileReader(districtFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile));
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile));
		String[] values;
		String[] errorSchema = new String[schema.length + 1];
		// read the schema
		values = inputFileReader.readNext();
		if (values.length == schema.length) {
			// check the schema
			for (int i = 0; i < schema.length; i++) {
				errorSchema[i] = values[i];
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema name is not mapping: " + values[i] + " " + schema[i]);
				}
			}
			errorSchema[errorSchema.length - 1] = "error_info";
			// check the value
			// should check the null
			cleanFileWriter.writeNext(schema);
			errorFileWriter.writeNext(errorSchema);
			Set<Integer> account_id_set = new HashSet<>();
			Set<Integer> district_id_set = new HashSet<>();
			// read the district table add the district id into the set
			districtFileReader.readNext();// skip the schema
			List<String[]> list = districtFileReader.readAll();
			for (String[] strings : list) {
				district_id_set.add(Integer.valueOf(strings[0]));
			}
			// check the values
			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				if (values.length != schema.length) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the account id value
				String account_id = values[0];
				if (account_id != null && account_id.matches("^[1-9]\\d*$")) {
					System.out.println("Match id is " + account_id);
					int accountIdInt = Integer.valueOf(account_id);

					if (account_id_set.contains(accountIdInt)) {
						errorInfo = errorInfo + ErrorType.ACCOUNT_ID_UNIQUE_ERROR + " ";
					} else {
						account_id_set.add(accountIdInt);
					}
				} else {
					System.out.println("Not match is " + account_id);
					errorInfo = errorInfo + ErrorType.ACCOUNT_ID_ERROR + " ";
				}
				// check the district id is match the district file
				// read the district file and check the id is right
				String districtString = values[1];
				if (districtString != null && districtString.matches("^[1-9]\\d*$")) {
					int districtInt = Integer.valueOf(districtString);
					if (district_id_set.contains(districtInt)) {

					} else {
						errorInfo = errorInfo + ErrorType.DISTRICT_ID_NOT_FOUNT_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.DISTRICT_ID_ERROR + " ";
				}

				// check the frequency
				String freString = values[2];
				if (freString != null) {
					if (MESICNE.equals(freString) || OBRATU.equals(freString) || TYDNE.equals(freString)) {
						// do nothing
					} else {
						errorInfo = errorInfo + ErrorType.FREQUENCY_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.FREQUENCY_ERROR + " ";
				}

				// check the data
				String dataString = values[3];
				if (dataString != null) {
					// data type format check
					String dataFormat = "yyyy/MM/dd HH:mm";
					SimpleDateFormat format = new SimpleDateFormat(dataFormat);
					try {
						format.parse(dataString);
					} catch (ParseException e) {
						errorInfo = errorInfo + ErrorType.DATA_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.DATA_ERROR + " ";
				}

				// output the value and the error information
				if (errorInfo.length() > 0) {
					System.out.println("error " + values[0]);
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					System.out.println("clean " + values[0]);
					// do't write the clean data
					cleanFileWriter.writeNext(values);
				}

			}
			inputFileReader.close();
			cleanFileWriter.flush();
			cleanFileWriter.close();
			errorFileWriter.flush();
			errorFileWriter.close();
		} else {
			inputFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema size is not mapping");
		}

	}

	public static void main(String[] args) throws DataCleanException, IOException {
//		clean(originFile, cleanFile, errorFile);
	}

}
