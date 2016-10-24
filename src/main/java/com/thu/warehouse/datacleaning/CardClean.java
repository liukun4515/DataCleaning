package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CardClean {

	enum ErrorType {
		CARD_ID_ERROR, DISP_ID_ERROR, TYPE_ERROR, ISSUED_ERROR, LENGTH_ERROR, CARD_ID_UNIQUE_ERROR
	}

	private static final String CLASSIC = "classic";
	private static final String JUNIOR = "junior";
	private static final String GOLD = "gold";

	// schema
	private static final String[] schema = { "card_id", "disp_id", "disp_id", "issued" };
	private static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.STRING, DataType.DATA };

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {
		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		if (!inputFile.exists()) {
			throw new DataCleanException("The input file is not exist");
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}

		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
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

			Set<Integer> card_id_set = new HashSet<>();
			Set<Integer> disp_id_set = new HashSet<>();
			errorFileWriter.writeNext(errorSchema);
			cleanFileWriter.writeNext(schema);

			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				// check the value lengthe
				if (values.length != schema.length) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the cardid
				String cardidString = values[0];
				if (cardidString != null && cardidString.matches("^[1-9]\\d*$")) {
					int cardidInt = Integer.valueOf(cardidString);
					if (cardidInt >= 0 && !card_id_set.contains(cardidInt)) {
						card_id_set.add(cardidInt);
					} else {
						errorInfo = errorInfo + ErrorType.CARD_ID_UNIQUE_ERROR + " ";
					}
				} else {
					System.out.println("Not match id is " + cardidString);
					errorInfo = errorInfo + ErrorType.CARD_ID_ERROR + " ";
				}
				// check the disp id, read the data from clean disp table
				/*
				 * 
				 * 
				 */
				// check the type, this is the type of enum
				
				
				
				String typeString = values[2];
				if (typeString != null && CLASSIC.equals(typeString) && JUNIOR.equals(typeString)
						&& GOLD.equals(typeString)) {
					// do nothing
				} else {
					errorInfo = errorInfo + ErrorType.TYPE_ERROR + " ";
				}
				/*
				 * 
				 * 
				 */
				// check the issued, the type is data
				String issuedString = values[3];
				if (issuedString != null) {
					// check the data
				} else {
					errorInfo = errorInfo + ErrorType.ISSUED_ERROR + " ";
				}

			}

		} else {
			inputFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema is not mapping");

		}
	}

	public static void main(String[] args) {

	}

}
