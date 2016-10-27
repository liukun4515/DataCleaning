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

public class CardClean {

	enum ErrorType {
		CARD_ID_ERROR, DISP_ID_ERROR, TYPE_ERROR, ISSUED_ERROR, LENGTH_ERROR, CARD_ID_UNIQUE_ERROR, DISP_ID_NOT_FOUND_ERROR
	}

	private static final String orgin = "card.csv";
	private static final String clean = "cardClean.csv";
	private static final String error = "cardError.csv";
	private static final String disp = "dispClean.csv";

	private static final String CLASSIC = "classic";
	private static final String JUNIOR = "junior";
	private static final String GOLD = "gold";

	// schema
	private static final String[] schema = { "card_id", "disp_id", "type", "issued" };
	private static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.STRING, DataType.DATA };

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {
		File inputFile = new File(input);
		File dispFile = new File(disp);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		if (!inputFile.exists() && !dispFile.exists()) {
			throw new DataCleanException("The input file is not exist");
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}

		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader dispFileReader = new CSVReader(new FileReader(dispFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);
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

			// get the disp_id_Set
			dispFileReader.readNext();
			List<String[]> list = dispFileReader.readAll();
			for (String[] strings : list) {
				disp_id_set.add(Integer.valueOf(strings[0]));
			}

			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				// check the value length
				if (values.length != schema.length) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the cardid
				String cardidString = values[0];
				if (cardidString != null && cardidString.matches("^[1-9]\\d*$")) {
					int cardidInt = Integer.valueOf(cardidString);
					if (!card_id_set.contains(cardidInt)) {
						card_id_set.add(cardidInt);
					} else {
						errorInfo = errorInfo + ErrorType.CARD_ID_UNIQUE_ERROR + " ";
					}
				} else {
					System.out.println("Not match id is " + cardidString);
					errorInfo = errorInfo + ErrorType.CARD_ID_ERROR + " ";
				}
				// check the disp id, read the data from clean disp table
				String disp_idString = values[1];
				if (disp_idString != null && disp_idString.matches("^[1-9]\\d*$")) {
					int disp_idInt = Integer.valueOf(disp_idString);
					if (disp_id_set.contains(disp_idInt)) {

					} else {
						errorInfo = errorInfo + ErrorType.DISP_ID_NOT_FOUND_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.DISP_ID_ERROR + " ";
				}
				// check the type, this is the type of enum
				String typeString = values[2];
				if (typeString != null &&( CLASSIC.equals(typeString) || JUNIOR.toString().equals(typeString)
						|| GOLD.toString().equals(typeString))) {
					// do nothing
				} else {
					errorInfo = errorInfo + ErrorType.TYPE_ERROR + " ";
				}

				// check the issued, the type is data
				String issuedString = values[3];
				if (issuedString != null) {
					// check the data
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					try {
						format.parse(issuedString);
					} catch (ParseException e) {
						errorInfo = errorInfo + ErrorType.ISSUED_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.ISSUED_ERROR + " ";
				}
				if (errorInfo.length() > 0) {
					System.out.println("The error id is " + values[0]);
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					// write the clean data
					System.out.println("The clean id is " + values[0]);
					cleanFileWriter.writeNext(values);
				}

			}
			inputFileReader.close();
			dispFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
		} else {
			inputFileReader.close();
			dispFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema is not mapping");

		}
	}

	public static void main(String[] args) throws DataCleanException, IOException {
		clean(orgin, clean, error);
	}

}
